package com.asp.transactionservice.controller;

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

import com.asp.transactionservice.dto.FundTransferRequestDTO;
import com.asp.transactionservice.dto.TransactionRequestDto;
import com.asp.transactionservice.dto.TransactionResponseDTO;
import com.asp.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transactions")
public class TransactionController {

    private TransactionService transactionService;

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequestDto dto) {
        return transactionService.createTransact(dto);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable String transactionId) {
        TransactionResponseDTO response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccount(@PathVariable Long accountId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccount(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> fundTransfer(@Valid @RequestBody FundTransferRequestDTO dto) {
        return transactionService.fundTransfer(dto);
    }
}
