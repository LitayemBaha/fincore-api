package com.example.demo.controller;

import com.example.demo.dto.insight.CategorySummaryResponse;
import com.example.demo.dto.insight.FinancialOverviewResponse;
import com.example.demo.dto.insight.MonthlySummaryResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Insights", description = "Aggregated financial insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Total balance, income, expense and per-account balances")
    public FinancialOverviewResponse overview(@AuthenticationPrincipal UserPrincipal user) {
        return insightService.overview(user.getId());
    }

    @GetMapping("/by-category")
    @Operation(summary = "Spend/income totals grouped by category, optionally within a date range")
    public List<CategorySummaryResponse> byCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return insightService.categoryBreakdown(user.getId(), from, to);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Monthly income vs expense trend")
    public List<MonthlySummaryResponse> monthly(@AuthenticationPrincipal UserPrincipal user) {
        return insightService.monthlyTrend(user.getId());
    }
}
