package com.expensetracker.controller;

import com.expensetracker.dto.BudgetDTO;
import com.expensetracker.service.interfaces.BudgetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:4200")
public class BudgetController {

    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(
            @AuthenticationPrincipal Long userId,
            @RequestBody BudgetDTO budgetDTO) {
        log.info("Create budget request for user id: {}, categoryId: {}, month: {}, year: {}",
                userId, budgetDTO.getCategoryId(), budgetDTO.getMonth(), budgetDTO.getYear());
        BudgetDTO created = budgetService.createBudget(userId, budgetDTO);
        log.info("Budget created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/budgets?month=10&year=2026  (both optional, defaults to the current month)
    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate today = LocalDate.now();
        int m = (month != null) ? month : today.getMonthValue();
        int y = (year != null) ? year : today.getYear();
        log.info("Fetch budgets request for user id: {}, month: {}, year: {}", userId, m, y);
        return ResponseEntity.ok(budgetService.getBudgets(userId, m, y));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody BudgetDTO budgetDTO) {
        log.info("Update budget id: {} for user id: {}", id, userId);
        BudgetDTO updated = budgetService.updateBudget(userId, id, budgetDTO);
        log.info("Budget id: {} updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("Delete budget id: {} for user id: {}", id, userId);
        budgetService.deleteBudget(userId, id);
        log.info("Budget id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}