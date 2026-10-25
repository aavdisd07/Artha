package com.expensetracker.dao.interfaces;

import com.expensetracker.entity.Category;
import com.expensetracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(TransactionType type);
    Optional<Category> findByNameIgnoreCaseAndType(String name, TransactionType type);
    boolean existsByNameIgnoreCaseAndType(String name, TransactionType type);
}