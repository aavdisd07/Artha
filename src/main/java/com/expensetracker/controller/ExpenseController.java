package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.service.interfaces.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200")
public class ExpenseController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> addTransaction(
            @AuthenticationPrincipal Long userId,
            @RequestBody ExpenseDTO expenseDTO) {
        log.info("Add transaction request received for user id: {}, type: {}", userId, expenseDTO.getType());
        ExpenseDTO created = expenseService.addTransaction(userId, expenseDTO);
        log.info("Transaction created with id: {} for user id: {}", created.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Example: GET /api/transactions?type=EXPENSE&categoryId=3&fromDate=2026-10-01&toDate=2026-10-31&search=rent&page=0&size=10
    // Every parameter is optional.
    @GetMapping
    public ResponseEntity<Page<ExpenseDTO>> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetch transactions request for user id: {}, type: {}, categoryId: {}, from: {}, to: {}, page: {}, size: {}",
                userId, type, categoryId, fromDate, toDate, page, size);
        return ResponseEntity.ok(expenseService.getTransactions(
                userId, type, categoryId, fromDate, toDate, search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getTransactionById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("Fetch transaction id: {} for user id: {}", id, userId);
        return ResponseEntity.ok(expenseService.getTransactionById(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody ExpenseDTO expenseDTO) {
        log.info("Update transaction id: {} for user id: {}", id, userId);
        ExpenseDTO updated = expenseService.updateTransaction(userId, id, expenseDTO);
        log.info("Transaction id: {} updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("Delete transaction id: {} for user id: {}", id, userId);
        expenseService.deleteTransaction(userId, id);
        log.info("Transaction id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}