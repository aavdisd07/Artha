package com.expensetracker.controller;

import com.expensetracker.dto.CategoryDTO;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.service.interfaces.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories            -> all categories
    // GET /api/categories?type=EXPENSE -> only expense categories
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(
            @RequestParam(required = false) TransactionType type) {
        log.info("Fetch categories request received, type: {}", type);
        return ResponseEntity.ok(categoryService.getCategories(type));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("Create category request received, name: {}, type: {}",
                categoryDTO.getName(), categoryDTO.getType());
        CategoryDTO created = categoryService.createCategory(categoryDTO);
        log.info("Category created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}