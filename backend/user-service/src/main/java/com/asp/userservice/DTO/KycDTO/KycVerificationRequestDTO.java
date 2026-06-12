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
package com.asp.userservice.DTO.KycDTO;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycVerificationRequestDTO {
    private Long kycId;
    private Boolean aadharVerified;
    private Boolean panVerified;
    private Boolean userPhotoVerified;
    private Boolean userSignatureVerified;
    private Boolean voterIdVerified;
    private Boolean passportVerified;
    private Boolean drivingLicenseVerified;
    private Long verifiedByEmployeeId;
    private String rejectionReason;
}
