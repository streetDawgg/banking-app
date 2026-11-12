package com.bankapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

/** All request/response shapes of the API, so entities are never exposed directly. */
public final class Dtos {
    private Dtos() {}

    // ---- requests ----
    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name must be at most 100 characters")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "\\d{11}", message = "Phone number must be exactly 11 digits")
            String phoneNumber,

            @NotBlank(message = "PIN is required")
            @Pattern(regexp = "\\d{4,6}", message = "PIN must be 4 to 6 digits")
            String pin) {}

    public record LoginRequest(
            @NotBlank(message = "Phone number is required") String phoneNumber,
            @NotBlank(message = "PIN is required") String pin) {}

    public record AmountRequest(
            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
            @Digits(integer = 12, fraction = 2, message = "Amount must have at most 2 decimal places")
            BigDecimal amount) {}

    public record TransferRequest(
            @NotBlank(message = "Recipient phone number is required")
            @Pattern(regexp = "\\d{11}", message = "Recipient phone number must be exactly 11 digits")
            String recipientPhoneNumber,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
            @Digits(integer = 12, fraction = 2, message = "Amount must have at most 2 decimal places")
            BigDecimal amount,

            @Size(max = 255, message = "Description must be at most 255 characters")
            String description) {}

    // ---- responses ----
    public record LoginResponse(String token, String tokenType, long expiresInSeconds) {}

    public record MessageResponse(String message) {}

    public record ErrorResponse(int status, String message) {}

    public record UserResponse(String fullName, String phoneNumber, Instant createdAt) {}

    public record AccountResponse(String fullName, String phoneNumber, String accountNumber,
                                  BigDecimal balance, String status) {}

    public record BalanceResponse(BigDecimal balance) {}

    public record RecipientResponse(String fullName, String phoneNumber) {}

    public record TransferResponse(Long transactionId, String recipientName, BigDecimal amount,
                                   BigDecimal newBalance, Instant createdAt) {}

    /** type is relative to the viewer: MONEY_SENT, MONEY_RECEIVED, CASH_DEPOSIT, CASH_WITHDRAWAL. */
    public record TransactionResponse(Long id, String type, BigDecimal amount, String senderAccount,
                                      String receiverAccount, String description, String status,
                                      Instant createdAt) {}
}
