package com.asp.transactionservice.client;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank
 * Author: Ayshi Shannidhya Panda
 * Created on: 12-06-2026
 *
 * Modified for IEEE Research: This class now implements the
 * CommunicationStrategy interface to participate in the Strategy pattern.
 * Functionality is unchanged — it still delegates to RabbitMQ via
 * RabbitTemplate.convertSendAndReceive().
 */

import com.asp.transactionservice.strategy.CommunicationStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * RabbitMQ-based account validation client.
 *
 * <p>This is the original communication client, now implementing
 * {@link CommunicationStrategy} for seamless integration with the
 * IEEE research experiment framework. When the {@code rabbitmq}
 * profile is active, this bean is injected into TransactionService
 * as the active communication strategy.</p>
 *
 * <p>The {@code isAccountExist}, {@code debitAccount}, and
 * {@code creditAccount} methods from the original code are preserved
 * as-is. The {@link CommunicationStrategy} methods delegate to them.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("rabbitmq")
public class AccountValidationClient implements CommunicationStrategy {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.account.validation.exchange:account-exchange}")
    private String exchange;

    @Value("${rabbitmq.account.validation.routing:account-validation-routing}")
    private String validationRoutingKey;

    @Value("${rabbitmq.account.debit.routing:account-debit-routing}")
    private String debitRoutingKey;

    @Value("${rabbitmq.account.credit.routing:account-credit-routing}")
    private String creditRoutingKey;

    @Autowired
    public AccountValidationClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ===========================
    // Original methods (preserved for backward compatibility)
    // ===========================

    public boolean isAccountExist(String accountNumber) {
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, validationRoutingKey, accountNumber);
        return Boolean.TRUE.equals(reply);
    }

    // ===========================
    // CommunicationStrategy implementation
    // ===========================

    @Override
    public boolean validateAccount(String accountNumber) {
        return isAccountExist(accountNumber);
    }

    @Override
    public boolean debitAccount(String accountNumber, BigDecimal amount) {
        Map<String, Object> request = Map.of(
                "accountNumber", accountNumber,
                "amount", amount.toPlainString()
        );
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, debitRoutingKey, request);
        return Boolean.TRUE.equals(reply);
    }

    @Override
    public boolean creditAccount(String accountNumber, BigDecimal amount) {
        Map<String, Object> request = Map.of(
                "accountNumber", accountNumber,
                "amount", amount.toPlainString()
        );
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, creditRoutingKey, request);
        return Boolean.TRUE.equals(reply);
    }
}
