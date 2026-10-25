package com.expensetracker.controller;

import com.expensetracker.dto.DashboardDTO;
import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.service.interfaces.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/dashboard/summary?fromDate=2026-10-01&toDate=2026-10-31
    // Dates are optional: the default range is the current month.
    @GetMapping("/summary")
    public ResponseEntity<DashboardDTO> getSummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = (toDate != null) ? toDate : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        log.info("Dashboard summary request for user id: {}, from: {}, to: {}", userId, from, to);
        return ResponseEntity.ok(dashboardService.getSummary(userId, from, to));
    }

    // GET /api/dashboard/category-breakdown?type=EXPENSE   (pie chart data)
    @GetMapping("/category-breakdown")
    public ResponseEntity<List<Map<String, Object>>> getCategoryBreakdown(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "EXPENSE") TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = (toDate != null) ? toDate : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        log.info("Category breakdown request for user id: {}, type: {}, from: {}, to: {}", userId, type, from, to);
        return ResponseEntity.ok(dashboardService.getCategoryBreakdown(userId, type, from, to));
    }

    // GET /api/dashboard/monthly-trend   (bar/line chart data, default: last 6 months)
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate from = (fromDate != null) ? fromDate : LocalDate.now().minusMonths(5).withDayOfMonth(1);
        LocalDate to = (toDate != null) ? toDate : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        log.info("Monthly trend request for user id: {}, from: {}, to: {}", userId, from, to);
        return ResponseEntity.ok(dashboardService.getMonthlyTrend(userId, from, to));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseDTO>> getRecentTransactions(
            @AuthenticationPrincipal Long userId) {
        log.info("Recent transactions request for user id: {}", userId);
        return ResponseEntity.ok(dashboardService.getRecentTransactions(userId));
    }
}