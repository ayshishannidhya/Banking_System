package com.asp.transactionservice.client;

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

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountValidationClient {

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

    public boolean isAccountExist(String accountNumber) {
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, validationRoutingKey, accountNumber);
        return Boolean.TRUE.equals(reply);
    }

    public boolean debitAccount(String accountNumber, BigDecimal amount) {
        Map<String, Object> request = Map.of(
                "accountNumber", accountNumber,
                "amount", amount.toPlainString()
        );
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, debitRoutingKey, request);
        return Boolean.TRUE.equals(reply);
    }

    public boolean creditAccount(String accountNumber, BigDecimal amount) {
        Map<String, Object> request = Map.of(
                "accountNumber", accountNumber,
                "amount", amount.toPlainString()
        );
        Boolean reply = (Boolean) rabbitTemplate.convertSendAndReceive(exchange, creditRoutingKey, request);
        return Boolean.TRUE.equals(reply);
    }
}
