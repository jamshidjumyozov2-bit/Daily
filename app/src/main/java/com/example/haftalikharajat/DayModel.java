package com.example.haftalikharajat;

public class DayModel {
    public int dayOfMonth;
    public double amount;
    public boolean isCurrentMonth;
    public long timestamp;
    public boolean isSelected;

    public DayModel(int dayOfMonth, double amount, boolean isCurrentMonth, long timestamp) {
        this.dayOfMonth = dayOfMonth;
        this.amount = amount;
        this.isCurrentMonth = isCurrentMonth;
        this.timestamp = timestamp;
    }
}
