package com.bankapp.service;

import com.bankapp.dto.Dtos.*;
import com.bankapp.entity.Account;
import com.bankapp.entity.User;
import com.bankapp.exception.ApiException;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.UserRepository;
import com.bankapp.security.JwtService;
import com.bankapp.security.TokenBlacklist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid phone number or PIN";

    private final UserRepository users;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenBlacklist blacklist;
    private final int maxFailedAttempts;
    private final long lockSeconds;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, AccountRepository accounts, PasswordEncoder encoder,
                       JwtService jwt, TokenBlacklist blacklist,
                       @Value("${bank.security.max-failed-attempts}") int maxFailedAttempts,
                       @Value("${bank.security.lock-seconds}") long lockSeconds) {
        this.users = users;
        this.accounts = accounts;
        this.encoder = encoder;
        this.jwt = jwt;
        this.blacklist = blacklist;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockSeconds = lockSeconds;
    }

    /** Creates the user profile and its bank account (balance 0.00) in one transaction. */
    @Transactional
    public AccountResponse register(RegisterRequest req) {
        if (users.existsByPhoneNumber(req.phoneNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone number is already registered");
        }
        User user = users.save(new User(req.fullName().trim(), req.phoneNumber(), encoder.encode(req.pin())));
        Account account = accounts.save(new Account(user, generateAccountNumber()));
        return new AccountResponse(user.getFullName(), user.getPhoneNumber(),
                account.getAccountNumber(), account.getBalance(), account.getAccountStatus());
    }

    /**
     * noRollbackFor: a wrong PIN throws an ApiException, but the incremented attempt counter /
     * lock timestamp must still be saved.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public LoginResponse login(LoginRequest req) {
        User user = users.findByPhoneNumber(req.phoneNumber())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS));
        Instant now = Instant.now();

        if (user.getLockedUntil() != null) {
            if (now.isBefore(user.getLockedUntil())) {
                long secs = (long) Math.ceil(Duration.between(now, user.getLockedUntil()).toMillis() / 1000.0);
                throw new ApiException(HttpStatus.LOCKED,
                        "Account is temporarily locked. Try again in " + secs + " second(s).");
            }
            user.setLockedUntil(null);          // lock expired
            user.setFailedLoginAttempts(0);
        }

        if (!encoder.matches(req.pin(), user.getPinHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= maxFailedAttempts) {
                user.setLockedUntil(now.plusSeconds(lockSeconds));
                user.setFailedLoginAttempts(0);
                throw new ApiException(HttpStatus.LOCKED,
                        "Too many failed attempts. Account locked for " + lockSeconds + " seconds.");
            }
            user.setFailedLoginAttempts(attempts);
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    BAD_CREDENTIALS + ". " + (maxFailedAttempts - attempts) + " attempt(s) remaining.");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return new LoginResponse(jwt.generate(user.getId()), "Bearer", jwt.lifetimeSeconds());
    }

    /** Stateless JWTs can't be "deleted", so the token id is put on a deny-list until it expires. */
    public MessageResponse logout(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                Claims claims = jwt.parse(authorizationHeader.substring(7));
                blacklist.revoke(claims.getId(), claims.getExpiration().toInstant());
            } catch (JwtException | IllegalArgumentException ignored) {
                // already invalid; nothing to revoke
            }
        }
        return new MessageResponse("Logged out");
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", random.nextLong(10_000_000_000L));
        } while (accounts.existsByAccountNumber(number));
        return number;
    }
}
