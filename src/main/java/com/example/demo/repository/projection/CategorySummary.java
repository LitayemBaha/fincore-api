package com.example.demo.repository.projection;

import com.example.demo.model.TransactionType;

import java.math.BigDecimal;

/**
 * Aggregated spend/income grouped by category.
 */
public interface CategorySummary {
    Long getCategoryId();

    String getCategoryName();

    TransactionType getType();

    BigDecimal getTotal();
}
