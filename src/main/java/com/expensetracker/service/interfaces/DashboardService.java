package com.expensetracker.service.interfaces;

import com.expensetracker.dto.DashboardDTO;
import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardDTO getSummary(Long userId, LocalDate fromDate, LocalDate toDate);

    // For the pie chart: categoryId, categoryName, total
    List<Map<String, Object>> getCategoryBreakdown(Long userId, TransactionType type,
                                                   LocalDate fromDate, LocalDate toDate);

    // For the bar/line chart: year, month, type, total
    List<Map<String, Object>> getMonthlyTrend(Long userId, LocalDate fromDate, LocalDate toDate);

    List<ExpenseDTO> getRecentTransactions(Long userId);
}