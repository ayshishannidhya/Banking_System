package com.asp.transactionservice.dto;

/*
 * Copyright (c) 2025 Ayshi Shannidhya Panda. All rights reserved.
 *
 * This source code is confidential and intended solely for internal use.
 * Unauthorized copying, modification, distribution, or disclosure of this
 * file, via any medium, is strictly prohibited.
 *
 * Project: Neptune Bank
 * Author: Ayshi Shannidhya Panda
 * Created on: 12-06-2026
 */

import com.asp.transactionservice.enumeration.ModeOfTransaction;
import com.asp.transactionservice.enumeration.TransactionMedium;
import com.asp.transactionservice.enumeration.TransactionStatus;
import com.asp.transactionservice.enumeration.TransactionType;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private Long id;
    private String transactionId;
    private Long sourceAccount;
    private Long destinationAccount;
    private TransactionType transactionType;
    private ModeOfTransaction modeOfTransaction;
    private TransactionMedium transactionMedium;
    private BigInteger amount;
    private String description;
    private String remarks;
    private LocalDateTime transactionDate;
    private TransactionStatus transactionStatus;
    private SourceOrDestinationRequestDto sourceOrDestinationBank;
    private boolean success;
    private String message;
}
