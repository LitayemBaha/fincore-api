package com.example.demo.dto.insight;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        String period,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
