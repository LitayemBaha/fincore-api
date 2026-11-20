package com.example.demo.dto.account;

import com.example.demo.model.Account;
import com.example.demo.model.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String name,
        AccountType type,
        String currency,
        BigDecimal balance,
        Instant createdAt
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getType(),
                a.getCurrency(), a.getBalance(), a.getCreatedAt());
    }
}
