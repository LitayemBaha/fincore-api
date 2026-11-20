package com.example.demo.dto.insight;

import com.example.demo.model.TransactionType;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        Long categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal total
) {
}
