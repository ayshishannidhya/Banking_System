package com.asp.userservice.controller;

import com.asp.userservice.DTO.UsersDTO.UsersRequestDTO;
import com.asp.userservice.DTO.UsersDTO.UsersResponseDTO;
import com.asp.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestPart("user") @Valid UsersRequestDTO registrationRequest,
            @RequestPart("aadhaar") MultipartFile aadhaarFile,
            @RequestPart("pan") MultipartFile panFile,
            @RequestPart("photo") MultipartFile photoFile,
            @RequestPart("signature") MultipartFile signatureFile,
            @RequestPart(value = "voter", required = false) MultipartFile voterIdFile,
            @RequestPart(value = "passport", required = false) MultipartFile passportIdFile,
            @RequestPart(value = "driving", required = false) MultipartFile drivingLicenseFile
    ) throws Exception {
        userService.registerUser(registrationRequest, aadhaarFile, panFile, photoFile, signatureFile, voterIdFile, passportIdFile, drivingLicenseFile);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("status", "success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UsersResponseDTO> getUserById(@PathVariable Long id) {
        UsersResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UsersResponseDTO>> getAllUsers() {
        List<UsersResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UsersResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UsersRequestDTO dto) {
        UsersResponseDTO updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/exists/{id}")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        return ResponseEntity.ok(userService.userExists(id));
    }

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }
}
