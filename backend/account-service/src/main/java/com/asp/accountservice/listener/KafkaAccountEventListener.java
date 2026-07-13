package com.asp.accountservice.listener;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Kafka Event Listener for Account Service
 *
 * This listener handles incoming Kafka events for account validation,
 * debit, and credit operations. It delegates to the existing
 * AccountService for all business logic.
 *
 * Active only when the 'kafka' Spring Profile is enabled.
 */

import com.asp.accountservice.service.AccountService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Kafka event listener for the Account Service.
 *
 * <p>Listens on three topics:
 * <ul>
 *   <li>{@code account.validation} — Validates account existence</li>
 *   <li>{@code account.debit} — Debits an account</li>
 *   <li>{@code account.credit} — Credits an account</li>
 * </ul>
 *
 * <p>Delegates to {@link AccountService} for all business logic,
 * exactly mirroring the existing {@link AccountValidationListener}
 * (RabbitMQ) behavior.</p>
 *
 * <p>Replies are sent to the corresponding {@code .reply} topics using
 * Spring Kafka's {@link SendTo} annotation.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("kafka")
public class KafkaAccountEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaAccountEventListener.class);

    private final AccountService accountService;
    private final Timer validationTimer;
    private final Timer debitTimer;
    private final Timer creditTimer;
    private final Counter processedCounter;

    @Autowired
    public KafkaAccountEventListener(AccountService accountService, MeterRegistry meterRegistry) {
        this.accountService = accountService;

        this.validationTimer = Timer.builder("neptune.kafka.consumer.latency")
                .tag("operation", "validation")
                .register(meterRegistry);

        this.debitTimer = Timer.builder("neptune.kafka.consumer.latency")
                .tag("operation", "debit")
                .register(meterRegistry);

        this.creditTimer = Timer.builder("neptune.kafka.consumer.latency")
                .tag("operation", "credit")
                .register(meterRegistry);

        this.processedCounter = Counter.builder("neptune.kafka.consumer.processed")
                .description("Total Kafka messages processed by Account Service")
                .register(meterRegistry);

        log.info("Kafka Account Event Listener initialized — delegating to AccountService");
    }

    /**
     * Handles account validation requests.
     * Delegates to {@link AccountService#accountExists(String)}.
     *
     * @param record the Kafka consumer record containing the account number
     * @return "true" if the account exists, "false" otherwise
     */
    @KafkaListener(
            topics = "${kafka.topic.account.validation:account.validation}",
            groupId = "neptune-account-validation-group"
    )
    @SendTo
    public String handleAccountValidation(ConsumerRecord<String, String> record) {
        return validationTimer.record(() -> {
            processedCounter.increment();
            String accountNumber = record.value();
            log.debug("Kafka: Validating account {}", accountNumber);

            try {
                if (accountNumber == null || accountNumber.isBlank()) return "false";
                boolean exists = accountService.accountExists(accountNumber);
                return String.valueOf(exists);
            } catch (Exception e) {
                log.error("Kafka: Account validation error for {}: {}", accountNumber, e.getMessage());
                return "false";
            }
        });
    }

    /**
     * Handles account debit requests.
     * Delegates to {@link AccountService#debitAccount(String, BigDecimal)}.
     *
     * @param record the Kafka consumer record containing "accountNumber:amount"
     * @return "true" if the debit was successful, "false" otherwise
     */
    @KafkaListener(
            topics = "${kafka.topic.account.debit:account.debit}",
            groupId = "neptune-account-debit-group"
    )
    @SendTo
    public String handleAccountDebit(ConsumerRecord<String, String> record) {
        return debitTimer.record(() -> {
            processedCounter.increment();
            String payload = record.value();
            log.debug("Kafka: Processing debit request: {}", payload);

            try {
                String[] parts = payload.split(":");
                String accountNumber = parts[0];
                BigDecimal amount = new BigDecimal(parts[1]);
                boolean result = accountService.debitAccount(accountNumber, amount);
                return String.valueOf(result);
            } catch (Exception e) {
                log.error("Kafka: Debit error: {}", e.getMessage());
                return "false";
            }
        });
    }

    /**
     * Handles account credit requests.
     * Delegates to {@link AccountService#creditAccount(String, BigDecimal)}.
     *
     * @param record the Kafka consumer record containing "accountNumber:amount"
     * @return "true" if the credit was successful, "false" otherwise
     */
    @KafkaListener(
            topics = "${kafka.topic.account.credit:account.credit}",
            groupId = "neptune-account-credit-group"
    )
    @SendTo
    public String handleAccountCredit(ConsumerRecord<String, String> record) {
        return creditTimer.record(() -> {
            processedCounter.increment();
            String payload = record.value();
            log.debug("Kafka: Processing credit request: {}", payload);

            try {
                String[] parts = payload.split(":");
                String accountNumber = parts[0];
                BigDecimal amount = new BigDecimal(parts[1]);
                boolean result = accountService.creditAccount(accountNumber, amount);
                return String.valueOf(result);
            } catch (Exception e) {
                log.error("Kafka: Credit error: {}", e.getMessage());
                return "false";
            }
        });
    }
}
