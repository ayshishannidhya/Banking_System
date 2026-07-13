package com.asp.accountservice.controller;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: REST API endpoints for Account Service (synchronous baseline)
 *
 * These endpoints are consumed by the Transaction Service's
 * RestCommunicationStrategy during REST-mode experiments.
 * They delegate to the existing AccountService.
 */

import com.asp.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller providing account operations for the synchronous
 * communication baseline in the IEEE experiment.
 *
 * <p>These endpoints are called directly by the Transaction Service's
 * {@code RestCommunicationStrategy} via HTTP. All business logic is
 * delegated to {@link AccountService}.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {

    private static final Logger log = LoggerFactory.getLogger(AccountRestController.class);

    private final AccountService accountService;

    @Autowired
    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Checks whether an account exists.
     * Delegates to {@link AccountService#accountExists(String)}.
     *
     * @param accountNumber the account number to check
     * @return {@code true} if exists, {@code false} otherwise
     */
    @GetMapping("/exists/{accountNumber}")
    public ResponseEntity<Boolean> accountExists(@PathVariable String accountNumber) {
        log.debug("REST: Checking account existence: {}", accountNumber);
        boolean exists = accountService.accountExists(accountNumber);
        return ResponseEntity.ok(exists);
    }

    /**
     * Debits the specified amount from an account.
     * Delegates to {@link AccountService#debitAccount(String, BigDecimal)}.
     *
     * @param request map containing "accountNumber" and "amount"
     * @return {@code true} if debit successful
     */
    @PostMapping("/debit")
    public ResponseEntity<Boolean> debitAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount"));
        log.debug("REST: Debit request — account={}, amount={}", accountNumber, amount);
        boolean result = accountService.debitAccount(accountNumber, amount);
        return ResponseEntity.ok(result);
    }

    /**
     * Credits the specified amount to an account.
     * Delegates to {@link AccountService#creditAccount(String, BigDecimal)}.
     *
     * @param request map containing "accountNumber" and "amount"
     * @return {@code true} if credit successful
     */
    @PostMapping("/credit")
    public ResponseEntity<Boolean> creditAccount(@RequestBody Map<String, String> request) {
        String accountNumber = request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount"));
        log.debug("REST: Credit request — account={}, amount={}", accountNumber, amount);
        boolean result = accountService.creditAccount(accountNumber, amount);
        return ResponseEntity.ok(result);
    }
}
