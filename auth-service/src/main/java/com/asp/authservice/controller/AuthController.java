package com.asp.authservice.controller;

import com.asp.authservice.model.Users;
import com.asp.authservice.records.LoginRecord;
import com.asp.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRecord loginRecord, HttpServletRequest request) {
        return authService.login(loginRecord, request);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateSession(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authService.validateToken(auth);
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody @Valid Users user) {
        return authService.registerUser(user);
    }
}
