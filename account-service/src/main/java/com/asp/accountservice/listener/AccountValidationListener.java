package com.asp.accountservice.listener;

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

import com.asp.accountservice.service.AccountService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountValidationListener {

    private final AccountService accountService;

    @Autowired
    public AccountValidationListener(AccountService accountService) {
        this.accountService = accountService;
    }

    @RabbitListener(queues = "${rabbitmq.account.validation.queue:account-validation-queue}")
    public Boolean validateAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return false;
        return accountService.accountExists(accountNumber);
    }

    @RabbitListener(queues = "${rabbitmq.account.debit.queue:account-debit-queue}")
    public Boolean debitAccount(Map<String, Object> request) {
        try {
            String accountNumber = (String) request.get("accountNumber");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            return accountService.debitAccount(accountNumber, amount);
        } catch (Exception e) {
            return false;
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.credit.queue:account-credit-queue}")
    public Boolean creditAccount(Map<String, Object> request) {
        try {
            String accountNumber = (String) request.get("accountNumber");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            return accountService.creditAccount(accountNumber, amount);
        } catch (Exception e) {
            return false;
        }
    }
}
