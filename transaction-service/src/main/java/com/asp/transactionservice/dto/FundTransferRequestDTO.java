package com.asp.transactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransferRequestDTO {

    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigInteger amount;

    private String description;
    private String remarks;

    @NotNull(message = "Source/Destination bank details are required")
    private SourceOrDestinationRequestDto sourceOrDestinationBank;
}
