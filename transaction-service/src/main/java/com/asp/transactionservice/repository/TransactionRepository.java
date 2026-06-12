package com.asp.transactionservice.repository;

import com.asp.transactionservice.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Boolean existsByTransactionId(String transactionId);

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findBySourceAccountOrDestinationAccountOrderByTransactionDateDesc(Long sourceAccount, Long destinationAccount);

    List<Transaction> findAllByOrderByTransactionDateDesc();
}
