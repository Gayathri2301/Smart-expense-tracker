# Smart Expense Tracker API (Java / Spring Boot)

A simple REST API for tracking personal expenses. No database required —
data lives in memory while the app runs and is mirrored to a local JSON
file (`data/expenses.json`) so it survives a restart.

## Stack

- Java 17
- Spring Boot 3.3 (Spring Web, Bean Validation)
- Jackson (JSON + `LocalDate` support via `jackson-datatype-jsr310`)
- Maven

## Project structure

```
smart-expense-tracker/
├── pom.xml
├── data/                              # created automatically, holds expenses.json
└── src/main/java/com/expensetracker/
    ├── ExpenseTrackerApplication.java  # main entry point
    ├── model/Expense.java              # domain model
    ├── dto/
    │   ├── ExpenseRequest.java         # validated request body for POST
    │   └── TotalResponse.java          # response shape for totals
    ├── repository/ExpenseRepository.java  # in-memory store + JSON file persistence
    ├── controller/ExpenseController.java  # REST endpoints
    └── exception/
        ├── ExpenseNotFoundException.java
        └── GlobalExceptionHandler.java  # consistent JSON error responses
```

## Running it

You need JDK 17+ and Maven installed locally.

```bash
cd smart-expense-tracker
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

To build a runnable jar instead:

```bash
mvn clean package
java -jar target/smart-expense-tracker-1.0.0.jar
```

## Endpoints

| Method | Path                              | Description                                      |
|--------|-----------------------------------|---------------------------------------------------|
| POST   | `/api/expenses`                   | Add a new expense                                  |
| GET    | `/api/expenses`                   | List all expenses                                  |
| GET    | `/api/expenses?category=Food`     | Filter expenses by category                        |
| GET    | `/api/expenses/{id}`              | Get a single expense                               |
| GET    | `/api/expenses/total`             | Total of all expenses                              |
| GET    | `/api/expenses/total?category=Food` | Total for a single category                      |
| GET    | `/api/expenses/total/by-category` | Totals broken down for every category              |
| DELETE | `/api/expenses/{id}`              | Delete an expense                                  |

### Add an expense

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Groceries",
        "amount": 42.50,
        "category": "Food",
        "date": "2026-07-30"
      }'
```

Response (`201 Created`):

```json
{
  "id": 1,
  "title": "Groceries",
  "amount": 42.50,
  "category": "Food",
  "date": "2026-07-30"
}
```

### List all expenses

```bash
curl http://localhost:8080/api/expenses
```

### Filter by category

```bash
curl "http://localhost:8080/api/expenses?category=Food"
```

### Total overall

```bash
curl http://localhost:8080/api/expenses/total
```

```json
{ "category": null, "total": 42.50, "count": 1 }
```

### Total for one category

```bash
curl "http://localhost:8080/api/expenses/total?category=Food"
```

### Totals grouped by every category

```bash
curl http://localhost:8080/api/expenses/total/by-category
```

```json
{ "Food": 42.50, "Transport": 15.00 }
```

### Delete an expense

```bash
curl -X DELETE http://localhost:8080/api/expenses/1
```

Returns `204 No Content` on success, or a `404` JSON error if the id doesn't exist.

## Validation & error handling

- `title`, `amount` (must be > 0), `category`, and `date` (`yyyy-MM-dd`) are required on create;
  invalid input returns `400` with a descriptive message.
- Requesting or deleting an unknown id returns `404` with a JSON error body, e.g.:

```json
{
  "timestamp": "2026-07-31T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found with id: 99"
}
```

## Notes on persistence

Every write (add/delete) rewrites `data/expenses.json`, and the file is
reloaded automatically the next time the app starts — so it behaves like a
lightweight database without needing one. Delete that file (or the whole
`data/` folder) any time you want to reset to a clean state.
