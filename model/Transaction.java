package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;

// Base Transaction class
public abstract class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String transactionId;
    protected String memberId;
    protected double amount;
    protected LocalDate date;
    protected String description;

    public Transaction(String transactionId, String memberId, double amount, LocalDate date, String description) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getMemberId() { return memberId; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    public abstract String getTransactionType();

    @Override
    public String toString() {
        return String.format("%s | %s | ₦%.2f | %s | %s", 
            transactionId, getTransactionType(), amount, 
            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), description);
    }
}