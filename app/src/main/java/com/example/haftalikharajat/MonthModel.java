package com.example.haftalikharajat;

public class MonthModel {
    public String name;
    public double amount;
    public int color;
    public int monthIndex; // 0-11

    public MonthModel(String name, double amount, int color, int monthIndex) {
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.monthIndex = monthIndex;
    }
}
