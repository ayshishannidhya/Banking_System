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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Aadhar number is required.")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be a 12-digit number.")
    @Size(min = 12, max = 12, message = "Aadhar number must be 12 digits.")
    private String aadharNumber;

    @NotBlank(message = "PAN number is required.")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number must be a 12-digit number.")
    @Size(min = 10, max = 10, message = "PAN number must be 10 characters.")
    private String panNumber;

    @Size(min = 10, max = 10, message = "Voter ID number must be 10 characters.")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{7}$", message = "Voter ID number must start with 3 letters followed by 7 numbers")
    private String voterId;
    private String passportNumber;
    private String drivingLicenseNumber;

}
