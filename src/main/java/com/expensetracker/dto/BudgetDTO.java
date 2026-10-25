package com.expensetracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BudgetDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private int month;
    private int year;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
}