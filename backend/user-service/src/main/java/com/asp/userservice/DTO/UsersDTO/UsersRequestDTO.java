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
package com.asp.userservice.DTO.UsersDTO;


import com.asp.userservice.DTO.ContactDetailsDTO.ContactDetailsRequestDTO;
import com.asp.userservice.DTO.KycDTO.KycRequestDTO;
import com.asp.userservice.DTO.NomineeDetailsDTO.NomineeRequestDTO;
import com.asp.userservice.enumeration.*;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "First name is required.")
    @Size(min = 3, message = "First name is too short.")
    private String firstName;

    @NotBlank(message = "First name is required.")
    @Size(min = 3, message = "First name is too short.")
    private String middleName;

    @NotBlank(message = "Lastname is required.")
    @Size(min = 3, message = "Last name is too short.")
    private String lastName;

    @NotNull(message = "Date of birth is required.")
    @Past
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required.")
    private Genders gender;

    @NotBlank(message = "Father name is required.")
    @Size(min = 8, message = "Father name is too short.")
    private String fatherName;

    @NotBlank(message = "Mother name is required.")
    @Size(min = 8, message = "Mother name is too short.")
    private String motherName;

    @NotNull(message = "Marital status is required.")
    private MaritalStatus maritalStatus;

    @Size(min = 5, message = "Spouse name is too short.")
    @Nullable
    private String spouseName;

    @NotNull(message = "Occupation required")
    private Occupation occupation;

    @NotBlank(message = "Occupation required")
    private String salary;

    @NotBlank(message = "Citizen is required.")
    private String citizen;

    @NotNull(message = "Category is required.")
    private Category category;

    @NotNull(message = "Religion is required.")
    private Religion religion;

    @Valid
    private ContactDetailsRequestDTO contactDetails;

    @Valid
    private NomineeRequestDTO nominee;

    @Valid
    private KycRequestDTO kyc;


}
