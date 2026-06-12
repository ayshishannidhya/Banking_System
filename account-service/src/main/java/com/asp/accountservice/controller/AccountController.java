package com.asp.accountservice.controller;

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

import com.asp.accountservice.DTO.AccountDetailsDTO.AccountRequestDTO;
import com.asp.accountservice.DTO.AccountDetailsDTO.AccountResponseDTO;
import com.asp.accountservice.DTO.AccountDetailsDTO.AccountUpdateRequestDTO;
import com.asp.accountservice.security.AuthenticatedPrincipal;
import com.asp.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody AccountRequestDTO requestDTO) {
        AccountResponseDTO response = accountService.createAccount(requestDTO);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        AccountResponseDTO account = accountService.getAccountById(id);
        if (!account.isSuccess()) {
            return ResponseEntity.badRequest().body(account);
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        List<AccountResponseDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(@PathVariable Long userId) {
        List<AccountResponseDTO> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponseDTO account = accountService.getAccountByAccountNumber(accountNumber);
        if (!account.isSuccess()) {
            return ResponseEntity.badRequest().body(account);
        }
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account deleted successfully.");
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id,
                                           @Valid @RequestBody AccountUpdateRequestDTO dto,
                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) authentication.getPrincipal();
        Long requesterId = principal.userId();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        AccountResponseDTO resp = accountService.updateAccount(id, dto, requesterId, isAdmin);
        if (!resp.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        return ResponseEntity.ok(resp);
    }
}
