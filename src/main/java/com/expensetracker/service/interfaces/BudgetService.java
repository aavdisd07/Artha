package com.expensetracker.service.interfaces;

import com.expensetracker.dto.BudgetDTO;

import java.util.List;

public interface BudgetService {

    BudgetDTO createBudget(Long userId, BudgetDTO budgetDTO);

    List<BudgetDTO> getBudgets(Long userId, int month, int year);

    BudgetDTO updateBudget(Long userId, Long budgetId, BudgetDTO budgetDTO);

    void deleteBudget(Long userId, Long budgetId);
}