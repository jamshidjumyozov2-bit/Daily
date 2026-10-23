package com.example.haftalikharajat;

import java.util.UUID;

public class Expense {
    public String id;
    public String name;
    public double amount;
    public String category;
    public long timestamp;

    public Expense(String name, double amount, String category, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.timestamp = timestamp;
    }
}