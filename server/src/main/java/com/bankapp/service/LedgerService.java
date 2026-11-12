package com.bankapp.service;

import com.bankapp.dto.Dtos.*;
import com.bankapp.entity.*;
import com.bankapp.exception.ApiException;
import com.bankapp.exception.InsufficientFundsException;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.BankTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

/**
 * All money movement lives here. Each operation runs inside ONE database transaction (via
 * TransactionTemplate), with the account rows locked (SELECT ... FOR UPDATE). If anything
 * throws, the whole operation rolls back.
 *
 * "FAILED" audit rows are written AFTER the rolled-back transaction has finished, so they don't
 * wait on the row locks that the failed transaction was holding.
 */
@Service
public class LedgerService {

    private final AccountRepository accounts;
    private final BankTransactionRepository transactions;
    private final TransactionTemplate tx;

    public LedgerService(AccountRepository accounts, BankTransactionRepository transactions,
                         PlatformTransactionManager txManager) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.tx = new TransactionTemplate(txManager);
    }

    public BalanceResponse deposit(Long userId, BigDecimal amount) {
        Long accountId = accountIdOf(userId);
        return tx.execute(status -> {
            Account account = lock(accountId);
            requireActive(account, "Your account is not active");
            account.credit(amount);
            transactions.save(new BankTransaction(null, account, TransactionType.DEPOSIT,
                    amount, "Cash deposit", TransactionStatus.SUCCESS));
            return new BalanceResponse(account.getBalance());
        });
    }

    public BalanceResponse withdraw(Long userId, BigDecimal amount) {
        Long accountId = accountIdOf(userId);
        try {
            return tx.execute(status -> {
                Account account = lock(accountId);
                requireActive(account, "Your account is not active");
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException();
                }
                account.debit(amount);
                transactions.save(new BankTransaction(account, null, TransactionType.WITHDRAWAL,
                        amount, "Cash withdrawal", TransactionStatus.SUCCESS));
                return new BalanceResponse(account.getBalance());
            });
        } catch (InsufficientFundsException e) {
            transactions.save(new BankTransaction(accounts.getReferenceById(accountId), null,
                    TransactionType.WITHDRAWAL, amount, "Cash withdrawal", TransactionStatus.FAILED));
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
    }

    public TransferResponse transfer(Long userId, TransferRequest req) {
        Long senderId = accountIdOf(userId);
        Account recipientLookup = accounts.findByUserPhoneNumber(req.recipientPhoneNumber())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No registered user with that phone number"));
        Long recipientId = recipientLookup.getId();

        if (senderId.equals(recipientId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot transfer money to your own account");
        }
        String description = (req.description() == null || req.description().isBlank())
                ? null : req.description().trim();
        BigDecimal amount = req.amount();

        try {
            return tx.execute(status -> {
                // Always lock the lower id first. Two people sending to each other at the same
                // moment then lock rows in the same order, so they can't deadlock.
                Account first = lock(Math.min(senderId, recipientId));
                Account second = lock(Math.max(senderId, recipientId));
                Account from = first.getId().equals(senderId) ? first : second;
                Account to = (from == first) ? second : first;

                requireActive(from, "Your account is not active");
                requireActive(to, "Recipient is not eligible to receive funds");
                if (from.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException();
                }
                from.debit(amount);
                to.credit(amount);
                BankTransaction saved = transactions.save(new BankTransaction(from, to,
                        TransactionType.TRANSFER, amount, description, TransactionStatus.SUCCESS));
                return new TransferResponse(saved.getId(), to.getUser().getFullName(), amount,
                        from.getBalance(), saved.getCreatedAt());
            });
        } catch (InsufficientFundsException e) {
            transactions.save(new BankTransaction(accounts.getReferenceById(senderId),
                    accounts.getReferenceById(recipientId), TransactionType.TRANSFER,
                    amount, description, TransactionStatus.FAILED));
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
    }

    private Long accountIdOf(Long userId) {
        return accounts.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"))
                .getId();
    }

    private Account lock(Long accountId) {
        return accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void requireActive(Account account, String message) {
        if (!account.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }
}
