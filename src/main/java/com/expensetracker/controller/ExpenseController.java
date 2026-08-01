package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.TotalResponse;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository repository;

    public ExpenseController(ExpenseRepository repository) {
        this.repository = repository;
    }

    /** Add a new expense. POST /api/expenses */
    @PostMapping
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense expense = new Expense(
                null,
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getDate()
        );
        Expense saved = repository.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * View all expenses, or filter by category via a query param.
     * GET /api/expenses
     * GET /api/expenses?category=Food
     */
    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) String category) {
        List<Expense> result = (category == null || category.isBlank())
                ? repository.findAll()
                : repository.findByCategory(category);
        return ResponseEntity.ok(result);
    }

    /** Fetch a single expense by id. GET /api/expenses/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpense(@PathVariable Long id) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        return ResponseEntity.ok(expense);
    }

    /**
     * Calculate totals.
     * GET /api/expenses/total                 -> overall total
     * GET /api/expenses/total?category=Food    -> total for one category
     */
    @GetMapping("/total")
    public ResponseEntity<TotalResponse> getTotal(
            @RequestParam(required = false) String category) {
        List<Expense> expenses = (category == null || category.isBlank())
                ? repository.findAll()
                : repository.findByCategory(category);

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TotalResponse response = new TotalResponse(category, total, expenses.size());
        return ResponseEntity.ok(response);
    }

    /** Breakdown of totals grouped by every category. GET /api/expenses/total/by-category */
    @GetMapping("/total/by-category")
    public ResponseEntity<Map<String, BigDecimal>> getTotalsByCategory() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Expense e : repository.findAll()) {
            totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return ResponseEntity.ok(totals);
    }

    /** Delete an expense. DELETE /api/expenses/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new ExpenseNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }
}
