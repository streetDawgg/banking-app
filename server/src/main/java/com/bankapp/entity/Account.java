package com.bankapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);

    @Column(name = "account_status", nullable = false)
    private String accountStatus = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Account() {}

    public Account(User user, String accountNumber) {
        this.user = user;
        this.accountNumber = accountNumber;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getAccountStatus() { return accountStatus; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isActive() { return "ACTIVE".equals(accountStatus); }
    public void credit(BigDecimal amount) { this.balance = balance.add(amount); }
    public void debit(BigDecimal amount) { this.balance = balance.subtract(amount); }
}
