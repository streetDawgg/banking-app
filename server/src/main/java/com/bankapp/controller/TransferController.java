package com.bankapp.controller;

import com.bankapp.dto.Dtos.*;
import com.bankapp.service.AccountService;
import com.bankapp.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final AccountService accounts;
    private final LedgerService ledger;

    public TransferController(AccountService accounts, LedgerService ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @GetMapping("/recipient/{phoneNumber}")
    public RecipientResponse recipient(@AuthenticationPrincipal Long userId, @PathVariable String phoneNumber) {
        return accounts.findRecipient(userId, phoneNumber);
    }

    @PostMapping
    public TransferResponse transfer(@AuthenticationPrincipal Long userId, @Valid @RequestBody TransferRequest req) {
        return ledger.transfer(userId, req);
    }
}
