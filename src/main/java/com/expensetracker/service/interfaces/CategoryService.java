package com.expensetracker.service.interfaces;

import com.expensetracker.dto.CategoryDTO;
import com.expensetracker.enums.TransactionType;

import java.util.List;

public interface CategoryService {

    // type = null returns all categories
    List<CategoryDTO> getCategories(TransactionType type);

    CategoryDTO createCategory(CategoryDTO categoryDTO);
}