package com.example.demo.dto.insight;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated financial overview for the authenticated user.
 */
public record FinancialOverviewResponse(
        BigDecimal totalBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal net,
        int accountCount,
        List<AccountBalanceResponse> accountBalances
) {
    public record AccountBalanceResponse(
            Long accountId,
            String accountName,
            String currency,
            BigDecimal balance
    ) {
    }
}
