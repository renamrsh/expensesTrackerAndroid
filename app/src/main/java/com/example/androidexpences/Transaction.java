package com.example.androidexpences;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private double amount;
    private String comment;
    private int type;
    private String date;

    public Transaction(double amount, String comment, int type) {
        this.amount = amount;
        this.comment = comment;
        this.type = type;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public double getAmount() {
        return amount;
    }

    public String getComment() {
        return comment;
    }

    public int getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

} 