package com.expensetracker.dto;

import com.expensetracker.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {

    private Long id;

    private String name;

    private TransactionType type;

    private boolean isDefault;
}