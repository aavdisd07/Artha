package com.expensetracker.dto;

import com.expensetracker.enums.PaymentMethod;
import com.expensetracker.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseDTO {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private TransactionType type;
    private PaymentMethod paymentMethod;
    private Long categoryId;
    private String categoryName;
}