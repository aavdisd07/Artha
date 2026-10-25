package com.expensetracker.service.impl;

import com.expensetracker.dao.interfaces.CategoryRepository;
import com.expensetracker.dto.CategoryDTO;
import com.expensetracker.entity.Category;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories(TransactionType type) {
        List<Category> categories = (type == null)
                ? categoryRepository.findAll()
                : categoryRepository.findByType(type);

        return categories.stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        if (dto.getName().trim().length() > 100) {
            throw new BadRequestException("Category name must be 100 characters or less");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("Category type (INCOME or EXPENSE) is required");
        }

        String name = dto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndType(name, dto.getType())) {
            throw new BadRequestException("Category '" + name + "' already exists for " + dto.getType());
        }

        Category category = new Category();
        category.setName(name);
        category.setType(dto.getType());
        category.setDefault(false); // user-created categories are never default

        return toDTO(categoryRepository.save(category));
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setDefault(category.isDefault());
        return dto;
    }
}