package com.ewallet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }

    public String getDisplay() {
        String sign = amount >= 0 ? "+" : "-";
        return String.format("%-22s %s%.2f    (Balance: %.2f)    %s",
                type, sign, Math.abs(amount), balanceAfter, timestamp.format(FMT));
    }

    @Override
    public String toString() {
        return getDisplay();
    }
}
