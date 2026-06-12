package com.asp.accountservice.service;

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
import com.asp.accountservice.DTO.BranchDTO.BranchResponseDTO;
import com.asp.accountservice.client.UserValidationClient;
import com.asp.accountservice.models.Account;
import com.asp.accountservice.models.Branch;
import com.asp.accountservice.repositories.AccountRepository;
import com.asp.accountservice.repositories.BranchRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final UserValidationClient userValidationClient;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public AccountService(UserValidationClient userValidationClient,
                          AccountRepository accountRepository,
                          BranchRepository branchRepository) {
        this.userValidationClient = userValidationClient;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
    }

    public AccountResponseDTO createAccount(AccountRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();

        if (userId == null) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("User ID is required.")
                    .build();
        }

        if (!userValidationClient.isUserExist(userId)) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("User not found with ID: " + userId)
                    .build();
        }

        Branch branch = null;
        if (requestDTO.getBranchCode() != null) {
            var byCode = branchRepository.findByBranchCode(requestDTO.getBranchCode());
            if (byCode.isEmpty()) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Branch not found with code: " + requestDTO.getBranchCode())
                        .build();
            }
            branch = byCode.get();
        } else {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Branch code is required.")
                    .build();
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .branch(branch)
                .modeOfOperation(requestDTO.getModeOfOperation())
                .accountType(requestDTO.getAccountType())
                .build();

        Account saved = accountRepository.save(account);

        return AccountResponseDTO.builder()
                .accountId(saved.getAccountId())
                .accountNumber(saved.getAccountNumber())
                .accountType(saved.getAccountType())
                .balance(saved.getBalance())
                .userId(saved.getUserId())
                .modeOfOperation(saved.getModeOfOperation())
                .branchId(saved.getBranch() != null ? saved.getBranch().getBranchId() : null)
                .branchCode(saved.getBranch() != null ? saved.getBranch().getBranchCode() : null)
                .createdAt(saved.getCreatedAt())
                .success(true)
                .message("Account created successfully for user " + userId)
                .build();
    }

    public String generateAccountNumber() {
        String candidate;
        int attempts = 0;
        do {
            candidate = "NB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            attempts++;
            if (attempts > 20) {
                throw new IllegalStateException("Unable to generate unique account number after " + attempts + " attempts");
            }
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    public AccountResponseDTO getAccountById(Long id) {
        if (id == null) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Account ID required.")
                    .build();
        }

        Account account = accountRepository.getAccountByAccountId(id);

        if (account == null) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Account not found with ID: " + id)
                    .build();
        }

        return AccountResponseDTO.builder()
                .success(true)
                .message("Account found")
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .modeOfOperation(account.getModeOfOperation())
                .branchId(account.getBranch() != null ? account.getBranch().getBranchId() : null)
                .branchCode(account.getBranch() != null ? account.getBranch().getBranchCode() : null)
                .createdAt(account.getCreatedAt())
                .build();
    }

    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::maptoDTO)
                .collect(Collectors.toList());
    }

    public List<AccountResponseDTO> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::maptoDTO)
                .collect(Collectors.toList());
    }

    public AccountResponseDTO getAccountByAccountNumber(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Account not found with number: " + accountNumber)
                    .build();
        }
        return maptoDTO(accountOpt.get());
    }

    private AccountResponseDTO maptoDTO(Account account) {
        BranchResponseDTO branchResponseDTO = null;
        Long branchId = null;
        String branchCode = null;
        if (account.getBranch() != null) {
            branchResponseDTO = BranchResponseDTO.builder()
                    .branchId(account.getBranch().getBranchId())
                    .branchCode(account.getBranch().getBranchCode())
                    .branchName(account.getBranch().getBranchName())
                    .branchAddress(account.getBranch().getBranchAddress())
                    .branchCity(account.getBranch().getBranchCity())
                    .branchState(account.getBranch().getBranchState())
                    .branchZip(account.getBranch().getBranchZip())
                    .success(true)
                    .message("Branch Found.")
                    .build();
            branchId = account.getBranch().getBranchId();
            branchCode = account.getBranch().getBranchCode();
        }
        return AccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .userId(account.getUserId())
                .modeOfOperation(account.getModeOfOperation())
                .branchCode(branchCode)
                .branchId(branchId)
                .createdAt(account.getCreatedAt())
                .branch(branchResponseDTO)
                .success(true)
                .message("Account Found.")
                .build();
    }

    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account not found: " + id);
        }
        accountRepository.deleteByAccountId(id);
    }

    public AccountResponseDTO updateAccount(Long id,
                                            AccountUpdateRequestDTO updatedAccount,
                                            Long requesterId,
                                            boolean isAdmin) {
        if (id == null) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Account ID required.")
                    .build();
        }

        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Account not found with ID: " + id)
                    .build();
        }
        Account account = accountOpt.get();

        boolean isOwner = account.getUserId() != null && account.getUserId().equals(requesterId);
        if (!isAdmin && !isOwner) {
            return AccountResponseDTO.builder()
                    .success(false)
                    .message("Access denied: not account owner or admin.")
                    .build();
        }

        if (updatedAccount.getUserId() != null && !updatedAccount.getUserId().equals(account.getUserId())) {
            if (!isAdmin) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Only admin can change account owner (userId).")
                        .build();
            }
            Long newUserId = updatedAccount.getUserId();
            if (!userValidationClient.isUserExist(newUserId)) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("User not found with ID: " + newUserId)
                        .build();
            }
            account.setUserId(newUserId);
        }

        if (updatedAccount.getBranchCode() != null) {
            if (!isAdmin) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Only admin can change branch.")
                        .build();
            }
            var byCode = branchRepository.findByBranchCode(updatedAccount.getBranchCode());
            if (byCode.isEmpty()) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Branch not found with code: " + updatedAccount.getBranchCode())
                        .build();
            }
            account.setBranch(byCode.get());
        }

        if (updatedAccount.getAccountType() != null) {
            if (!isAdmin) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Only admin can change account type.")
                        .build();
            }
            account.setAccountType(updatedAccount.getAccountType());
        }

        if (updatedAccount.getModeOfOperation() != null) {
            account.setModeOfOperation(updatedAccount.getModeOfOperation());
        }

        if (updatedAccount.getBalance() != null) {
            if (!isAdmin) {
                return AccountResponseDTO.builder()
                        .success(false)
                        .message("Only admin can change balance.")
                        .build();
            }
            account.setBalance(updatedAccount.getBalance());
        }

        Account saved = accountRepository.save(account);

        return AccountResponseDTO.builder()
                .accountId(saved.getAccountId())
                .accountNumber(saved.getAccountNumber())
                .accountType(saved.getAccountType())
                .balance(saved.getBalance())
                .userId(saved.getUserId())
                .modeOfOperation(saved.getModeOfOperation())
                .branchId(saved.getBranch() != null ? saved.getBranch().getBranchId() : null)
                .branchCode(saved.getBranch() != null ? saved.getBranch().getBranchCode() : null)
                .updatedAt(saved.getUpdatedAt())
                .success(true)
                .message("Account updated successfully for ID " + id)
                .build();
    }

    @Transactional
    public boolean debitAccount(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) return false;

        Account account = accountOpt.get();
        if (account.getBalance().compareTo(amount) < 0) return false;

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        return true;
    }

    @Transactional
    public boolean creditAccount(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) return false;

        Account account = accountOpt.get();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        return true;
    }

    public boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }
}
