package com.bankapp.service;

import com.bankapp.dto.Dtos.*;
import com.bankapp.entity.Account;
import com.bankapp.exception.ApiException;
import com.bankapp.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public UserResponse profile(Long userId) {
        var user = find(userId).getUser();
        return new UserResponse(user.getFullName(), user.getPhoneNumber(), user.getCreatedAt());
    }

    public AccountResponse account(Long userId) {
        Account a = find(userId);
        return new AccountResponse(a.getUser().getFullName(), a.getUser().getPhoneNumber(),
                a.getAccountNumber(), a.getBalance(), a.getAccountStatus());
    }

    public BalanceResponse balance(Long userId) {
        return new BalanceResponse(find(userId).getBalance());
    }

    /** Lets the sender confirm the recipient's name before sending money. */
    public RecipientResponse findRecipient(Long requesterId, String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{11}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Phone number must be exactly 11 digits");
        }
        Account recipient = accounts.findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No registered user with that phone number"));
        if (recipient.getUser().getId().equals(requesterId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot transfer money to your own account");
        }
        return new RecipientResponse(recipient.getUser().getFullName(), recipient.getUser().getPhoneNumber());
    }

    private Account find(Long userId) {
        return accounts.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
