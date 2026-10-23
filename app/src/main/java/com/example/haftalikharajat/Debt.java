package com.example.haftalikharajat;

import java.util.UUID;

public class Debt {
    public String id;
    public String personName;
    public double amount;
    public String reason;
    public String phone;
    public long dateTaken;
    public long dateDue;
    public boolean isOwedToMe;
    public boolean isPaid;

    public Debt(String personName, double amount, String reason, String phone, long dateTaken, long dateDue, boolean isOwedToMe) {
        this.id = UUID.randomUUID().toString();
        this.personName = personName;
        this.amount = amount;
        this.reason = reason;
        this.phone = phone;
        this.dateTaken = dateTaken;
        this.dateDue = dateDue;
        this.isOwedToMe = isOwedToMe;
        this.isPaid = false;
    }
}