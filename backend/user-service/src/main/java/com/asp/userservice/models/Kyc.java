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


import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kyc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long kycId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "userId", referencedColumnName = "userId", unique = true, updatable = false)
    private Users user;

    @Column(nullable = false, unique = true)
    private String aadharNumber;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] aadharImage;

    @Builder.Default
    private Boolean aadharVerified = false;

    @Column(nullable = false, unique = true)
    private String panNumber;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] panImage;

    @Builder.Default
    private Boolean panVerified = false;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] userPhoto;

    @Builder.Default
    private Boolean userPhotoVerified = false;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] userSignature;

    @Builder.Default
    private Boolean userSignatureVerified = false;

    @Column(unique = true)
    private String voterId;

    @Column(columnDefinition = "BYTEA")
    private byte[] voterIdImage;

    @Builder.Default
    private Boolean voterIdVerified = false;

    @Column(unique = true)
    private String passportNumber;

    @Column(columnDefinition = "BYTEA")
    private byte[] passportImage;

    @Builder.Default
    private Boolean passportVerified = false;

    @Column(unique = true)
    private String drivingLicenseNumber;

    @Column(columnDefinition = "BYTEA")
    private byte[] drivingLicenseImage;

    @Builder.Default
    private Boolean drivingLicenseVerified = false;

    @Builder.Default
    private Long verifiedByEmployeeId = null;

    @Builder.Default
    private String rejectionReason = null;

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
