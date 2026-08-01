package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory store for expenses that is mirrored to a local
 * JSON file (data/expenses.json) so data survives an application restart.
 * No external database is required.
 */
@Repository
public class ExpenseRepository {

    private static final Path DATA_FILE = Path.of("data", "expenses.json");

    private final ConcurrentHashMap<Long, Expense> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);
    private final ObjectMapper objectMapper;

    public ExpenseRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void loadFromDisk() {
        try {
            if (Files.exists(DATA_FILE)) {
                Expense[] loaded = objectMapper.readValue(DATA_FILE.toFile(), Expense[].class);
                long maxId = 0;
                for (Expense e : loaded) {
                    store.put(e.getId(), e);
                    maxId = Math.max(maxId, e.getId());
                }
                idSequence.set(maxId);
            }
        } catch (IOException e) {
            // Start with an empty store if the file is missing or unreadable.
            System.err.println("Could not load existing expenses.json, starting fresh: " + e.getMessage());
        }
    }

    private synchronized void persist() {
        try {
            File parent = DATA_FILE.getParent().toFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            List<Expense> all = new ArrayList<>(store.values());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(DATA_FILE.toFile(), all);
        } catch (IOException e) {
            System.err.println("Failed to persist expenses.json: " + e.getMessage());
        }
    }

    public Expense save(Expense expense) {
        if (expense.getId() == null) {
            expense.setId(idSequence.incrementAndGet());
        }
        store.put(expense.getId(), expense);
        persist();
        return expense;
    }

    public List<Expense> findAll() {
        return store.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public List<Expense> findByCategory(String category) {
        return store.values().stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public Optional<Expense> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean deleteById(Long id) {
        Expense removed = store.remove(id);
        if (removed != null) {
            persist();
            return true;
        }
        return false;
    }
}
