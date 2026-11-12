package com.bankapp.controller;

import com.bankapp.dto.Dtos.UserResponse;
import com.bankapp.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AccountService accounts;

    public UserController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return accounts.profile(userId);
    }
}
