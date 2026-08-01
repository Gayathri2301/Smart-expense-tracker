package com.expensetracker.dto;

import java.math.BigDecimal;

public class TotalResponse {

    private String category; // null when the total is across all categories
    private BigDecimal total;
    private long count;

    public TotalResponse() {
    }

    public TotalResponse(String category, BigDecimal total, long count) {
        this.category = category;
        this.total = total;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
