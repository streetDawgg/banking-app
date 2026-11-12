package com.bankapp.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {}

    public User(String fullName, String phoneNumber, String pinHash) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.pinHash = pinHash;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPinHash() { return pinHash; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int n) { this.failedLoginAttempts = n; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant t) { this.lockedUntil = t; }
    public Instant getCreatedAt() { return createdAt; }
}
