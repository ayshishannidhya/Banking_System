/*
 * Copyright (c) 2025 Ayshi Shannidhya Panda. All rights reserved.
 *
 * This source code is confidential and intended solely for internal use.
 * Unauthorized copying, modification, distribution, or disclosure of this
 * file, via any medium, is strictly prohibited.
 *
 * Project: Neptune Bank
 * Author: Ayshi Shannidhya Panda
 * Created on: 20-06-2025
 */
package com.asp.userservice.models;


import com.asp.userservice.enumeration.Relationship;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NomineeDetails {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nomineeId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "userId", referencedColumnName = "userId", unique = true, updatable = false)
    private Users user;

    @Column(nullable = false)
    private String nomineeName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Relationship nomineeRelationship;

    @Column(nullable = false)
    @Past
    private LocalDate nomineeDateOfBirth;

    @Column(nullable = false, unique = true)
    private String nomineeMobileNumber;

    @Email
    @Column(nullable = false, unique = true)
    private String NomineeEmail;

    @Column(nullable = false, unique = true)
    private String nomineeAadhar;

    @Column(nullable = false, unique = true)
    private String nomineePan;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nomineeAddress;

    @Column(nullable = false, updatable = false)
    @PastOrPresent
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @PastOrPresent
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
