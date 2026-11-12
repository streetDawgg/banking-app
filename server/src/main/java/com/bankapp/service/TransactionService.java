package com.bankapp.service;

import com.bankapp.dto.Dtos.TransactionResponse;
import com.bankapp.entity.Account;
import com.bankapp.entity.BankTransaction;
import com.bankapp.exception.ApiException;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.BankTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final int RECENT_COUNT = 5;
    private static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accounts;
    private final BankTransactionRepository transactions;

    public TransactionService(AccountRepository accounts, BankTransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public List<TransactionResponse> recent(Long userId) {
        return history(userId, 0, RECENT_COUNT);
    }

    public List<TransactionResponse> history(Long userId, int page, int size) {
        Account account = accounts.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return transactions.findForAccount(account.getId(), PageRequest.of(safePage, safeSize)).stream()
                .map(t -> toResponse(t, account.getId()))
                .toList();
    }

    private TransactionResponse toResponse(BankTransaction t, Long viewerAccountId) {
        String type = switch (t.getType()) {
            case DEPOSIT -> "CASH_DEPOSIT";
            case WITHDRAWAL -> "CASH_WITHDRAWAL";
            case TRANSFER -> (t.getSender() != null && t.getSender().getId().equals(viewerAccountId))
                    ? "MONEY_SENT" : "MONEY_RECEIVED";
        };
        return new TransactionResponse(t.getId(), type, t.getAmount(),
                t.getSender() == null ? null : t.getSender().getAccountNumber(),
                t.getReceiver() == null ? null : t.getReceiver().getAccountNumber(),
                t.getDescription(), t.getStatus().name(), t.getCreatedAt());
    }
}
