package com.example.haftalikharajat;

public class CategorySummary {
    public String name;
    public double amount;
    public int percent;
    public int iconResId;
    public int color;

    public CategorySummary(String name, double amount, int percent, int iconResId, int color) {
        this.name = name;
        this.amount = amount;
        this.percent = percent;
        this.iconResId = iconResId;
        this.color = color;
    }
}
