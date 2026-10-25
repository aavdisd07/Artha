package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.ExpenseRepository;
import com.expensetracker.dto.DashboardDTO;
import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;

    @Override
    public DashboardDTO getSummary(Long userId, LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);

        BigDecimal income = expenseRepository.sumAmountByType(userId, TransactionType.INCOME, fromDate, toDate);
        BigDecimal expense = expenseRepository.sumAmountByType(userId, TransactionType.EXPENSE, fromDate, toDate);
        BigDecimal balance = income.subtract(expense);

        // Savings rate = (income - expense) / income * 100, and 0 when there is no income
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance.multiply(BigDecimal.valueOf(100))
                    .divide(income, 2, RoundingMode.HALF_UP);
        }

        DashboardDTO dto = new DashboardDTO();
        dto.setTotalIncome(income);
        dto.setTotalExpense(expense);
        dto.setBalance(balance);
        dto.setSavingsRate(savingsRate);
        return dto;
    }

    @Override
    public List<Map<String, Object>> getCategoryBreakdown(Long userId, TransactionType type,
                                                          LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);
        if (type == null) {
            throw new BadRequestException("Type (INCOME or EXPENSE) is required");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : expenseRepository.sumByCategoryBreakdown(userId, type, fromDate, toDate)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryId", row[0]);
            item.put("categoryName", row[1]);
            item.put("total", row[2]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(Long userId, LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : expenseRepository.monthlyTotals(userId, fromDate, toDate)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("year", ((Number) row[0]).intValue());
            item.put("month", ((Number) row[1]).intValue());
            item.put("type", row[2]);
            item.put("total", row[3]);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<ExpenseDTO> getRecentTransactions(Long userId) {
        return expenseRepository.findTop5ByUserIdOrderByDateDescCreatedAtDesc(userId)
                .stream()
                .map(e -> {
                    ExpenseDTO dto = new ExpenseDTO();
                    dto.setId(e.getId());
                    dto.setTitle(e.getTitle());
                    dto.setAmount(e.getAmount());
                    dto.setDescription(e.getDescription());
                    dto.setDate(e.getDate());
                    dto.setType(e.getType());
                    dto.setPaymentMethod(e.getPaymentMethod());
                    dto.setCategoryId(e.getCategory().getId());
                    dto.setCategoryName(e.getCategory().getName());
                    return dto;
                })
                .toList();
    }

    private void checkDates(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BadRequestException("From date and To date are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after To date");
        }
    }
}