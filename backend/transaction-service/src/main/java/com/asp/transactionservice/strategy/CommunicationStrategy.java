package com.asp.transactionservice.strategy;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Communication Strategy Interface
 *
 * This interface defines the contract for inter-service communication
 * in the Transaction Service. Three implementations exist:
 *   - RestCommunicationStrategy (synchronous HTTP)
 *   - RabbitMQCommunicationStrategy (RabbitMQ RPC)
 *   - KafkaCommunicationStrategy (Kafka request-reply)
 *
 * The active implementation is selected at runtime via Spring Profiles:
 *   --spring.profiles.active={rest,rabbitmq,kafka}
 *
 * This design enables controlled A/B/C experiments for the IEEE paper
 * without modifying any business logic code.
 */

import java.math.BigDecimal;

/**
 * Strategy interface for inter-service communication between
 * Transaction Service and Account Service.
 *
 * <p>Follows the Strategy design pattern (GoF) to decouple the
 * communication mechanism from the business logic. Each implementation
 * provides identical semantics but uses a different transport layer.</p>
 *
 * @author Ayshi Shannidhya Panda
 * @see RestCommunicationStrategy
 * @see RabbitMQCommunicationStrategy
 * @see KafkaCommunicationStrategy
 */
public interface CommunicationStrategy {

    /**
     * Validates whether an account exists in the Account Service.
     *
     * @param accountNumber the account number to validate
     * @return {@code true} if the account exists, {@code false} otherwise
     */
    boolean validateAccount(String accountNumber);

    /**
     * Debits the specified amount from the given account.
     *
     * @param accountNumber the account number to debit
     * @param amount        the amount to debit (must be positive)
     * @return {@code true} if the debit was successful, {@code false} if
     *         insufficient funds or account not found
     */
    boolean debitAccount(String accountNumber, BigDecimal amount);

    /**
     * Credits the specified amount to the given account.
     *
     * @param accountNumber the account number to credit
     * @param amount        the amount to credit (must be positive)
     * @return {@code true} if the credit was successful, {@code false} otherwise
     */
    boolean creditAccount(String accountNumber, BigDecimal amount);

    /**
     * Returns the name of this communication strategy for logging and metrics.
     *
     * @return the strategy name (e.g., "REST", "RabbitMQ", "Kafka")
     */
    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }
}
