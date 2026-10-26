package com.example.haftalikharajat;

public class Income {
    public String source;
    public double amount;
    public String accountType;
    public long timestamp;

    public Income(String source, double amount, String accountType, long timestamp) {
        this.source = source;
        this.amount = amount;
        this.accountType = accountType;
        this.timestamp = timestamp;
    }
}