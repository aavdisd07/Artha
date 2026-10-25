package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.BudgetRepository;
import com.expensetracker.dao.interfaces.CategoryRepository;
import com.expensetracker.dao.interfaces.ExpenseRepository;
import com.expensetracker.dao.interfaces.UserRepository;
import com.expensetracker.dto.BudgetDTO;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.service.interfaces.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BudgetDTO createBudget(Long userId, BudgetDTO dto) {
        validateAmount(dto.getAmount());
        validateMonthYear(dto.getMonth(), dto.getYear());

        if (dto.getCategoryId() == null) {
            throw new BadRequestException("Category is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getType() != TransactionType.EXPENSE) {
            throw new BadRequestException("Budget can only be set for an EXPENSE category");
        }
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, category.getId(), dto.getMonth(), dto.getYear())) {
            throw new BadRequestException("A budget already exists for this category and month");
        }

        Budget budget = new Budget();
        budget.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setCategory(category);
        budget.setUser(user);

        return toDTO(budgetRepository.save(budget), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDTO> getBudgets(Long userId, int month, int year) {
        validateMonthYear(month, year);

        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(budget -> toDTO(budget, userId))
                .toList();
    }

    // Only the amount can be changed (category/month/year define the budget itself)
    @Override
    @Transactional
    public BudgetDTO updateBudget(Long userId, Long budgetId, BudgetDTO dto) {
        validateAmount(dto.getAmount());

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budget.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));

        return toDTO(budgetRepository.save(budget), userId);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    // ================= helpers =================

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Budget amount must be greater than zero");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BadRequestException("Budget amount is too large");
        }
    }

    private void validateMonthYear(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Year is not valid");
        }
    }

    // Fills spentAmount and remainingAmount by summing this month's expenses
    private BudgetDTO toDTO(Budget budget, Long userId) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal spent = expenseRepository.sumByCategory(
                userId, budget.getCategory().getId(), from, to);

        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setCategoryId(budget.getCategory().getId());
        dto.setCategoryName(budget.getCategory().getName());
        dto.setAmount(budget.getAmount());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setSpentAmount(spent);
        dto.setRemainingAmount(budget.getAmount().subtract(spent)); // negative = over budget
        return dto;
    }
}