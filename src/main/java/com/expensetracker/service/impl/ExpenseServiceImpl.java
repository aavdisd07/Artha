package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.CategoryRepository;
import com.expensetracker.dao.interfaces.ExpenseRepository;
import com.expensetracker.dao.interfaces.UserRepository;
import com.expensetracker.dao.specification.ExpenseSpecification;
import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.service.interfaces.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99"); // column is precision 12, scale 2
    private static final int MAX_PAGE_SIZE = 100;

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ---------- Add (#1, #5, #7) ----------
    @Override
    @Transactional
    public ExpenseDTO addTransaction(Long userId, ExpenseDTO dto) {
        validate(dto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = getMatchingCategory(dto);

        Expense expense = new Expense();
        copyFields(dto, expense, category);
        expense.setUser(user);

        return toDTO(expenseRepository.save(expense));
    }

    // ---------- View one ----------
    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO getTransactionById(Long userId, Long transactionId) {
        return toDTO(findOwned(userId, transactionId));
    }

    // ---------- Update (#3) ----------
    @Override
    @Transactional
    public ExpenseDTO updateTransaction(Long userId, Long transactionId, ExpenseDTO dto) {
        validate(dto);

        Expense expense = findOwned(userId, transactionId);
        Category category = getMatchingCategory(dto);

        copyFields(dto, expense, category);

        return toDTO(expenseRepository.save(expense));
    }

    // ---------- Delete (#4) ----------
    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Expense expense = findOwned(userId, transactionId);
        expenseRepository.delete(expense);
    }

    // ---------- View all + filter + search + pagination (#2, #8, #9, #10) ----------
    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO> getTransactions(Long userId,
                                            TransactionType type,
                                            Long categoryId,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            String search,
                                            int page,
                                            int size) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after To date");
        }

        int safePage = Math.max(page, 0);
        int safeSize = (size < 1) ? 10 : Math.min(size, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "date")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt")));

        return expenseRepository
                .findAll(ExpenseSpecification.filter(userId, type, categoryId, fromDate, toDate, search), pageable)
                .map(this::toDTO);
    }

    // ================= helpers =================

    private Expense findOwned(Long userId, Long transactionId) {
        // Searching by id AND userId means a user can never touch someone else's data
        return expenseRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void validate(ExpenseDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Title is required");
        }
        if (dto.getTitle().trim().length() > 150) {
            throw new BadRequestException("Title must be 150 characters or less");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        if (dto.getAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new BadRequestException("Amount is too large");
        }
        if (dto.getDate() == null) {
            throw new BadRequestException("Date is required");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("Type (INCOME or EXPENSE) is required");
        }
        if (dto.getCategoryId() == null) {
            throw new BadRequestException("Category is required");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new BadRequestException("Description must be 1000 characters or less");
        }
    }

    // The category must exist and its type must match the transaction type
    private Category getMatchingCategory(ExpenseDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getType() != dto.getType()) {
            throw new BadRequestException("Category '" + category.getName()
                    + "' is an " + category.getType() + " category and cannot be used for " + dto.getType());
        }
        return category;
    }

    private void copyFields(ExpenseDTO dto, Expense expense, Category category) {
        expense.setTitle(dto.getTitle().trim());
        expense.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));
        expense.setDescription(dto.getDescription() == null ? null : dto.getDescription().trim());
        expense.setDate(dto.getDate());
        expense.setType(dto.getType());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setCategory(category);
    }

    private ExpenseDTO toDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setDate(expense.getDate());
        dto.setType(expense.getType());
        dto.setPaymentMethod(expense.getPaymentMethod());
        dto.setCategoryId(expense.getCategory().getId());
        dto.setCategoryName(expense.getCategory().getName());
        return dto;
    }
}