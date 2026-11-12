package com.bankapp.controller;

import com.bankapp.dto.Dtos.*;
import com.bankapp.service.AccountService;
import com.bankapp.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accounts;
    private final LedgerService ledger;

    public AccountController(AccountService accounts, LedgerService ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal Long userId) {
        return accounts.account(userId);
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@AuthenticationPrincipal Long userId) {
        return accounts.balance(userId);
    }

    // Not in the original endpoint list; added because the CLI phase includes deposits/withdrawals.
    @PostMapping("/deposit")
    public BalanceResponse deposit(@AuthenticationPrincipal Long userId, @Valid @RequestBody AmountRequest req) {
        return ledger.deposit(userId, req.amount());
    }

    @PostMapping("/withdraw")
    public BalanceResponse withdraw(@AuthenticationPrincipal Long userId, @Valid @RequestBody AmountRequest req) {
        return ledger.withdraw(userId, req.amount());
    }
}
