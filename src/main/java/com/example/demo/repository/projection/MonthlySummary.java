package com.example.demo.repository.projection;

import java.math.BigDecimal;

/**
 * Aggregated income/expense totals grouped by month (YYYY-MM).
 */
public interface MonthlySummary {
    String getPeriod();

    BigDecimal getIncome();

    BigDecimal getExpense();
}
