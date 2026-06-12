package com.asp.otpservice.controller;

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

import com.asp.otpservice.dto.OtpMultiChannelRequestDTO;
import com.asp.otpservice.dto.OtpVerifyRequestDTO;
import com.asp.otpservice.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }


    @PostMapping("/send")
    public ResponseEntity<String> sendOtpToBoth(@Valid @RequestBody OtpMultiChannelRequestDTO request) {
        StringBuilder status = new StringBuilder();

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            var smsResponse = otpService.sendSmsOtp(request.getPhone());
            status.append("SMS: ").append(smsResponse.getBody()).append("\n");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            var emailResponse = otpService.sendEmailOtp(request.getEmail());
            status.append("Email: ").append(emailResponse.getBody()).append("\n");
        }

        if (status.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one of phone or email is required.");
        }

        return ResponseEntity.ok(status.toString().trim());
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO request) {
        return otpService.verifyOtp(request.getIdentifier(), request.getOtp());
    }
}
