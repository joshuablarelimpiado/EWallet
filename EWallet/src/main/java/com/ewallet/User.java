package com.ewallet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private final String username;
    private final String password;
    private double balance;
    private final List<Transaction> history = new ArrayList<>();
    private final boolean admin;

    public User(String username, String password) {
        this(username, password, false);
    }

    public User(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.balance = 0.0;
        this.admin = admin;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public List<Transaction> getHistory() { return history; }
    public boolean isAdmin() { return admin; }

    public void deposit(double amount) {
        balance += amount;
        history.add(0, new Transaction("Deposit", amount, balance));
    }

    public boolean withdraw(double amount) {
        if (amount > balance) return false;
        balance -= amount;
        history.add(0, new Transaction("Withdraw", -amount, balance));
        return true;
    }

    public boolean transferOut(double amount, String toUser) {
        if (amount > balance) return false;
        balance -= amount;
        history.add(0, new Transaction("Sent to " + toUser, -amount, balance));
        return true;
    }

    public void transferIn(double amount, String fromUser) {
        balance += amount;
        history.add(0, new Transaction("Received from " + fromUser, amount, balance));
    }
}
