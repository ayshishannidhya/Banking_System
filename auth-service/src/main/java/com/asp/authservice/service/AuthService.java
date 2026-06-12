package com.asp.authservice.service;

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

import com.asp.authservice.jwt.JwtUtil;
import com.asp.authservice.model.Users;
import com.asp.authservice.records.LoginRecord;
import com.asp.authservice.repository.AuthRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthRepo authRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(AuthRepo authRepo, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.authRepo = authRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> login(LoginRecord loginRecord, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        if (loginRecord.username().isBlank() ||
                loginRecord.password().isBlank() ||
                loginRecord.mode().isBlank()) {
            response.put("error", "Login failed.");
            response.put("status", "failed");
            response.put("code", "401");
            response.put("message", "Username, password or mode is empty.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Optional<Users> userOpt = authRepo.findByUsername(loginRecord.username());
        if (userOpt.isEmpty()) {
            response.put("error", "Authentication Failed");
            response.put("status", "failed");
            response.put("code", "401");
            response.put("message", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(loginRecord.password(), user.getPassword())) {
            response.put("error", "Authentication Failed");
            response.put("status", "failed");
            response.put("code", "401");
            response.put("message", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        if (!user.isAccountNonExpired()) {
            response.put("error", "Account is Expired");
            response.put("status", "failed");
            response.put("code", "403");
            response.put("message", "Your account has expired.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (!user.isAccountNonLocked()) {
            response.put("error", "Account is Locked");
            response.put("status", "failed");
            response.put("code", "403");
            response.put("message", "Your account is locked.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (!user.isCredentialsNonExpired()) {
            response.put("error", "Credentials Expired");
            response.put("status", "failed");
            response.put("code", "403");
            response.put("message", "Your credentials have expired.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (!user.isEnabled()) {
            response.put("error", "Account is Disabled");
            response.put("status", "failed");
            response.put("code", "403");
            response.put("message", "Your account is disabled.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (loginRecord.mode().equalsIgnoreCase("session")) {
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("username", user.getUsername());
            httpSession.setAttribute("role", user.getRole().name());
            httpSession.setMaxInactiveInterval(600 * 3);
            response.put("status", "success");
            response.put("code", "200");
            response.put("message", "Session login successful.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (loginRecord.mode().equalsIgnoreCase("jwt")) {
            request.getSession(false);
            String token = jwtUtil.generateToken(user);
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
            response.put("token", token);
            response.put("status", "success");
            response.put("code", "200");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("error", "Authentication Failed");
        response.put("status", "failed");
        response.put("code", "400");
        response.put("message", "Invalid authentication mode. Use 'jwt' or 'session'.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> registerUser(Users user) {
        Optional<Users> existingUser = authRepo.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Conflict");
            response.put("status", "failed");
            response.put("code", "409");
            response.put("message", "User already exists.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        authRepo.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("code", "201");
        response.put("message", "User created successfully.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<Map<String, String>> validateToken(String authHeader) {
        Map<String, String> response = new HashMap<>();

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            response.put("message", "Authorization header missing or invalid");
            response.put("code", "400");
            response.put("status", "Failure");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            var claims = jwtUtil.validateToken(authHeader.substring(7));
            response.put("message", "Successfully Validated");
            response.put("code", "200");
            response.put("status", "Success");
            response.put("username", claims.getSubject());
            response.put("role", claims.get("role", String.class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Invalid or expired token");
            response.put("code", "401");
            response.put("status", "Failure");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
