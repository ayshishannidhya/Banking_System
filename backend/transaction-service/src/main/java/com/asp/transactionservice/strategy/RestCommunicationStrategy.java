package com.asp.transactionservice.strategy;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: REST Communication Strategy (Synchronous Baseline)
 */

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Synchronous REST implementation of the communication strategy.
 *
 * <p>This implementation uses Spring's {@link RestTemplate} to make
 * synchronous HTTP calls to the Account Service REST API. The calling
 * thread blocks until a response is received or a timeout occurs.</p>
 *
 * <p>This serves as the synchronous baseline in the IEEE performance
 * comparison experiment.</p>
 *
 * <p>Activated by: {@code --spring.profiles.active=rest}</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("rest")
public class RestCommunicationStrategy implements CommunicationStrategy {

    private static final Logger log = LoggerFactory.getLogger(RestCommunicationStrategy.class);

    private static final String ACCOUNT_SERVICE_BASE_URL = "http://neptune-account-service:8083";

    private final RestTemplate restTemplate;
    private final Timer validationTimer;
    private final Timer debitTimer;
    private final Timer creditTimer;

    public RestCommunicationStrategy(MeterRegistry meterRegistry) {
        this.restTemplate = new RestTemplate();

        // Micrometer timers for precise latency measurement per operation
        this.validationTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "REST")
                .tag("operation", "validation")
                .description("Account validation latency via REST")
                .register(meterRegistry);

        this.debitTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "REST")
                .tag("operation", "debit")
                .description("Account debit latency via REST")
                .register(meterRegistry);

        this.creditTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "REST")
                .tag("operation", "credit")
                .description("Account credit latency via REST")
                .register(meterRegistry);

        log.info("REST Communication Strategy activated — synchronous baseline mode");
    }

    @Override
    public boolean validateAccount(String accountNumber) {
        return validationTimer.record(() -> {
            try {
                String url = ACCOUNT_SERVICE_BASE_URL + "/api/accounts/exists/" + accountNumber;
                ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
                return Boolean.TRUE.equals(response.getBody());
            } catch (RestClientException e) {
                log.error("REST account validation failed for {}: {}", accountNumber, e.getMessage());
                return false;
            }
        });
    }

    @Override
    public boolean debitAccount(String accountNumber, BigDecimal amount) {
        return debitTimer.record(() -> {
            try {
                String url = ACCOUNT_SERVICE_BASE_URL + "/api/accounts/debit";
                Map<String, Object> request = Map.of(
                        "accountNumber", accountNumber,
                        "amount", amount.toPlainString()
                );
                ResponseEntity<Boolean> response = restTemplate.postForEntity(url, request, Boolean.class);
                return Boolean.TRUE.equals(response.getBody());
            } catch (RestClientException e) {
                log.error("REST debit failed for {} amount {}: {}", accountNumber, amount, e.getMessage());
                return false;
            }
        });
    }

    @Override
    public boolean creditAccount(String accountNumber, BigDecimal amount) {
        return creditTimer.record(() -> {
            try {
                String url = ACCOUNT_SERVICE_BASE_URL + "/api/accounts/credit";
                Map<String, Object> request = Map.of(
                        "accountNumber", accountNumber,
                        "amount", amount.toPlainString()
                );
                ResponseEntity<Boolean> response = restTemplate.postForEntity(url, request, Boolean.class);
                return Boolean.TRUE.equals(response.getBody());
            } catch (RestClientException e) {
                log.error("REST credit failed for {} amount {}: {}", accountNumber, amount, e.getMessage());
                return false;
            }
        });
    }
}
