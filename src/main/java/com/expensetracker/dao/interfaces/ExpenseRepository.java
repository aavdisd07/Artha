package com.expensetracker.dao.interfaces;

import com.expensetracker.entity.Expense;
import com.expensetracker.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    // ---------- CRUD with ownership check ----------

    @EntityGraph(attributePaths = "category")
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    // ---------- Filter + search + pagination ----------
    // Overrides the built-in method only to load the category together
    // (needed because category is LAZY and you map categoryName into the DTO)

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Expense> findAll(Specification<Expense> spec, Pageable pageable);

    // ---------- Dashboard ----------

    @EntityGraph(attributePaths = "category")
    List<Expense> findTop5ByUserIdOrderByDateDescCreatedAtDesc(Long userId);

    @Query("""
           SELECT COALESCE(SUM(e.amount), 0)
           FROM Expense e
           WHERE e.user.id = :userId
             AND e.type = :type
             AND e.date BETWEEN :fromDate AND :toDate
           """)
    BigDecimal sumAmountByType(@Param("userId") Long userId,
                               @Param("type") TransactionType type,
                               @Param("fromDate") LocalDate fromDate,
                               @Param("toDate") LocalDate toDate);

    // Rows: [categoryId, categoryName, totalAmount]
    @Query("""
           SELECT c.id, c.name, SUM(e.amount)
           FROM Expense e JOIN e.category c
           WHERE e.user.id = :userId
             AND e.type = :type
             AND e.date BETWEEN :fromDate AND :toDate
           GROUP BY c.id, c.name
           ORDER BY SUM(e.amount) DESC
           """)
    List<Object[]> sumByCategoryBreakdown(@Param("userId") Long userId,
                                          @Param("type") TransactionType type,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate);

    // Rows: [year, month, type, totalAmount]
    @Query("""
           SELECT YEAR(e.date), MONTH(e.date), e.type, SUM(e.amount)
           FROM Expense e
           WHERE e.user.id = :userId
             AND e.date BETWEEN :fromDate AND :toDate
           GROUP BY YEAR(e.date), MONTH(e.date), e.type
           ORDER BY YEAR(e.date), MONTH(e.date)
           """)
    List<Object[]> monthlyTotals(@Param("userId") Long userId,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    // ---------- Budget: how much was spent in one category ----------

    @Query("""
           SELECT COALESCE(SUM(e.amount), 0)
           FROM Expense e
           WHERE e.user.id = :userId
             AND e.category.id = :categoryId
             AND e.type = com.expensetracker.enums.TransactionType.EXPENSE
             AND e.date BETWEEN :fromDate AND :toDate
           """)
    BigDecimal sumByCategory(@Param("userId") Long userId,
                             @Param("categoryId") Long categoryId,
                             @Param("fromDate") LocalDate fromDate,
                             @Param("toDate") LocalDate toDate);
}