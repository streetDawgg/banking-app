package com.bankapp.exception;

/** Thrown inside a DB transaction to roll it back; the caller then logs a FAILED record. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient balance");
    }
}
