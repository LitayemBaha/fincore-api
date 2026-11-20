package com.example.demo.dto.account;

import com.example.demo.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType type,
        @NotBlank @Size(min = 3, max = 3) String currency,
        BigDecimal openingBalance
) {
}
