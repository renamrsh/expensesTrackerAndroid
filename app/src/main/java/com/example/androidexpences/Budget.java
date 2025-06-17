package com.example.androidexpences;

import java.util.ArrayList;

public class Budget {
    private String name;
    private double amount;
    private ArrayList<Transaction> transactions;

    public Budget(String name, double amount) {
        this.name = name;
        this.amount = amount;
        this.transactions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public String toString() {
        return name + ": " + amount + "$";
    }

}