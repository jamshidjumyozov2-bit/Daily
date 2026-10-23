package com.example.haftalikharajat;

public class Income {
    public String source;
    public double amount;
    public long timestamp;

    public Income(String source, double amount, long timestamp) {
        this.source = source;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}