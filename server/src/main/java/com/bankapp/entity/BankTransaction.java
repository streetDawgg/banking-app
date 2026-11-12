package com.bankapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Immutable audit record (also enforced by a DB trigger). */
@Entity
@Table(name = "transactions")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", updatable = false)
    private Account sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", updatable = false)
    private Account receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(updatable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, updatable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected BankTransaction() {}

    public BankTransaction(Account sender, Account receiver, TransactionType type,
                           BigDecimal amount, String description, TransactionStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.status = status;
    }

    public Long getId() { return id; }
    public Account getSender() { return sender; }
    public Account getReceiver() { return receiver; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
