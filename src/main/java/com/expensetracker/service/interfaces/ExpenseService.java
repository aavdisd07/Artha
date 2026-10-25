package com.expensetracker.service.interfaces;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.enums.TransactionType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ExpenseService {

    ExpenseDTO addTransaction(Long userId, ExpenseDTO expenseDTO);

    ExpenseDTO getTransactionById(Long userId, Long transactionId);

    ExpenseDTO updateTransaction(Long userId, Long transactionId, ExpenseDTO expenseDTO);

    void deleteTransaction(Long userId, Long transactionId);

    // View all + filter + search + pagination. Every filter is optional (can be null).
    Page<ExpenseDTO> getTransactions(Long userId,
                                     TransactionType type,
                                     Long categoryId,
                                     LocalDate fromDate,
                                     LocalDate toDate,
                                     String search,
                                     int page,
                                     int size);
}