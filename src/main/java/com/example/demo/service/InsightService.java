package com.example.demo.service;

import com.example.demo.dto.insight.CategorySummaryResponse;
import com.example.demo.dto.insight.FinancialOverviewResponse;
import com.example.demo.dto.insight.MonthlySummaryResponse;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InsightService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public InsightService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> categoryBreakdown(Long userId, LocalDate from, LocalDate to) {
        return transactionRepository.summarizeByCategory(userId, from, to).stream()
                .map(cs -> new CategorySummaryResponse(
                        cs.getCategoryId(), cs.getCategoryName(), cs.getType(), cs.getTotal()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> monthlyTrend(Long userId) {
        return transactionRepository.summarizeByMonth(userId).stream()
                .map(ms -> {
                    BigDecimal income = ms.getIncome() != null ? ms.getIncome() : BigDecimal.ZERO;
                    BigDecimal expense = ms.getExpense() != null ? ms.getExpense() : BigDecimal.ZERO;
                    return new MonthlySummaryResponse(ms.getPeriod(), income, expense, income.subtract(expense));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialOverviewResponse overview(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId, Pageable.unpaged()).getContent();

        List<FinancialOverviewResponse.AccountBalanceResponse> balances = accounts.stream()
                .map(a -> new FinancialOverviewResponse.AccountBalanceResponse(
                        a.getId(), a.getName(), a.getCurrency(), a.getBalance()))
                .toList();

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (MonthlySummaryResponse m : monthlyTrend(userId)) {
            totalIncome = totalIncome.add(m.income());
            totalExpense = totalExpense.add(m.expense());
        }

        return new FinancialOverviewResponse(
                totalBalance, totalIncome, totalExpense,
                totalIncome.subtract(totalExpense), accounts.size(), balances);
    }
}
