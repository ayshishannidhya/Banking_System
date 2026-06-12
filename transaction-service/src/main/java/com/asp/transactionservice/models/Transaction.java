package com.asp.transactionservice.models;

import com.asp.transactionservice.enumeration.ModeOfTransaction;
import com.asp.transactionservice.enumeration.TransactionMedium;
import com.asp.transactionservice.enumeration.TransactionStatus;
import com.asp.transactionservice.enumeration.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(length = 64)
    private String transactionId;

    @Column(length = 64, nullable = false)
    private Long sourceAccount;

    @Column(length = 64, nullable = false)
    private Long destinationAccount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModeOfTransaction modeOfTransaction;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionMedium transactionMedium;

    @Column(nullable = false)
    private BigInteger amount;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private SourceOrDestinationBank sourceOrDestinationBankId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String remarks;

    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.transactionDate = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
