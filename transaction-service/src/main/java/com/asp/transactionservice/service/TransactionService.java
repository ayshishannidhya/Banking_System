package com.asp.transactionservice.service;

import com.asp.transactionservice.client.AccountValidationClient;
import com.asp.transactionservice.dto.FundTransferRequestDTO;
import com.asp.transactionservice.dto.SourceOrDestinationRequestDto;
import com.asp.transactionservice.dto.TransactionRequestDto;
import com.asp.transactionservice.dto.TransactionResponseDTO;
import com.asp.transactionservice.enumeration.ModeOfTransaction;
import com.asp.transactionservice.enumeration.TransactionMedium;
import com.asp.transactionservice.enumeration.TransactionStatus;
import com.asp.transactionservice.enumeration.TransactionType;
import com.asp.transactionservice.mapper.SourceOrDestinationMapper;
import com.asp.transactionservice.mapper.TransactionMapper;
import com.asp.transactionservice.models.Transaction;
import com.asp.transactionservice.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private TransactionRepository transactionRepository;
    private AccountValidationClient accountValidationClient;

    @Autowired
    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Autowired
    public void setAccountValidationClient(AccountValidationClient accountValidationClient) {
        this.accountValidationClient = accountValidationClient;
    }

    @Transactional
    public ResponseEntity<?> createTransact(@Valid TransactionRequestDto dto) {
        var transaction = TransactionMapper.toEntity(dto);
        transaction.setTransactionId(getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository.save(transaction);
        String transactionId = transaction.getTransactionId();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction created successfully");
        response.put("transactionId", transactionId);
        response.put("status", "success");
        response.put("code", "201");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public TransactionResponseDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + transactionId));
        return mapToResponseDTO(transaction);
    }

    public List<TransactionResponseDTO> getTransactionsByAccount(Long accountId) {
        List<Transaction> transactions = transactionRepository
                .findBySourceAccountOrDestinationAccountOrderByTransactionDateDesc(accountId, accountId);
        return transactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<?> fundTransfer(@Valid FundTransferRequestDTO dto) {
        Map<String, String> response = new HashMap<>();

        if (dto.getSourceAccountNumber().equals(dto.getDestinationAccountNumber())) {
            response.put("status", "failed");
            response.put("message", "Source and destination accounts cannot be the same.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!accountValidationClient.isAccountExist(dto.getSourceAccountNumber())) {
            response.put("status", "failed");
            response.put("message", "Source account not found: " + dto.getSourceAccountNumber());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!accountValidationClient.isAccountExist(dto.getDestinationAccountNumber())) {
            response.put("status", "failed");
            response.put("message", "Destination account not found: " + dto.getDestinationAccountNumber());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = Transaction.builder()
                .sourceAccount(Long.parseLong(dto.getSourceAccountNumber().replaceAll("[^0-9]", "").isEmpty() ? "0" : "0"))
                .destinationAccount(Long.parseLong(dto.getDestinationAccountNumber().replaceAll("[^0-9]", "").isEmpty() ? "0" : "0"))
                .transactionType(TransactionType.TRANSFER)
                .modeOfTransaction(ModeOfTransaction.ONLINE)
                .transactionMedium(TransactionMedium.NEFT)
                .amount(dto.getAmount())
                .sourceOrDestinationBankId(SourceOrDestinationMapper.toEntity(dto.getSourceOrDestinationBank()))
                .description(dto.getDescription())
                .remarks(dto.getRemarks())
                .transactionStatus(TransactionStatus.PENDING)
                .build();

        transaction.setTransactionId(getTransactionId());
        transactionRepository.save(transaction);

        BigDecimal transferAmount = new BigDecimal(dto.getAmount());

        boolean debited = accountValidationClient.debitAccount(dto.getSourceAccountNumber(), transferAmount);
        if (!debited) {
            transaction.setTransactionStatus(TransactionStatus.FAILED_INSUFFICIENT_FUNDS);
            transactionRepository.save(transaction);
            response.put("status", "failed");
            response.put("message", "Insufficient funds or debit failed.");
            response.put("transactionId", transaction.getTransactionId());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean credited = accountValidationClient.creditAccount(dto.getDestinationAccountNumber(), transferAmount);
        if (!credited) {
            accountValidationClient.creditAccount(dto.getSourceAccountNumber(), transferAmount);
            transaction.setTransactionStatus(TransactionStatus.FAILED_TECHNICAL_ERROR);
            transactionRepository.save(transaction);
            response.put("status", "failed");
            response.put("message", "Credit failed. Amount has been reversed.");
            response.put("transactionId", transaction.getTransactionId());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository.save(transaction);

        response.put("status", "success");
        response.put("message", "Fund transfer completed successfully.");
        response.put("transactionId", transaction.getTransactionId());
        response.put("amount", dto.getAmount().toString());
        response.put("code", "201");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction t) {
        SourceOrDestinationRequestDto bankDto = null;
        if (t.getSourceOrDestinationBankId() != null) {
            var bank = t.getSourceOrDestinationBankId();
            bankDto = SourceOrDestinationRequestDto.builder()
                    .bankName(bank.getBankName())
                    .bankIFSCCode(bank.getBankIFSCCode())
                    .bankBranchName(bank.getBankBranchName())
                    .bankBranchCity(bank.getBankBranchCity())
                    .bankBranchState(bank.getBankBranchState())
                    .bankBranchCountry(bank.getBankBranchCountry())
                    .bankBranchZipCode(bank.getBankBranchZipCode())
                    .build();
        }

        return TransactionResponseDTO.builder()
                .id(t.getId())
                .transactionId(t.getTransactionId())
                .sourceAccount(t.getSourceAccount())
                .destinationAccount(t.getDestinationAccount())
                .transactionType(t.getTransactionType())
                .modeOfTransaction(t.getModeOfTransaction())
                .transactionMedium(t.getTransactionMedium())
                .amount(t.getAmount())
                .description(t.getDescription())
                .remarks(t.getRemarks())
                .transactionDate(t.getTransactionDate())
                .transactionStatus(t.getTransactionStatus())
                .sourceOrDestinationBank(bankDto)
                .success(true)
                .message("Transaction found")
                .build();
    }

    private String getTransactionId() {
        String transactionId;
        do {
            transactionId = generateTransactionId();
        } while (transactionRepository.existsByTransactionId(transactionId));
        return transactionId;
    }

    private String generateTransactionId() {
        StringBuilder sb = new StringBuilder("NEFT");
        for (int i = 0; i < 60; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
