package com.bankapp.controller;

import com.bankapp.dto.Dtos.TransactionResponse;
import com.bankapp.service.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactions;

    public TransactionController(TransactionService transactions) {
        this.transactions = transactions;
    }

    @GetMapping("/history")
    public List<TransactionResponse> history(@AuthenticationPrincipal Long userId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return transactions.history(userId, page, size);
    }

    @GetMapping("/recent")
    public List<TransactionResponse> recent(@AuthenticationPrincipal Long userId) {
        return transactions.recent(userId);
    }
}
