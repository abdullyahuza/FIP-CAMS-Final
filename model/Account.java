package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accountId;
    private Member member;
    private double balance;
    private java.util.List<Transaction> transactionHistory;
    private LocalDate lastInterestDate;
    private double interestRate;
    private boolean interestEnabled;

    public Account(String accountId, Member member) {
        this.accountId = accountId;
        this.member = member;
        this.balance = 0.0;
        this.transactionHistory = new java.util.ArrayList<>();
        this.lastInterestDate = LocalDate.now();
        this.interestRate = 3.5; // 3.5% annual interest rate
        this.interestEnabled = true;
    }

    public String getAccountId() { return accountId; }
    public Member getMember() { return member; }
    public double getBalance() { return balance; }
    public java.util.List<Transaction> getTransactionHistory() { return transactionHistory; }
    public LocalDate getLastInterestDate() { return lastInterestDate; }
    public double getInterestRate() { return interestRate; }
    public boolean isInterestEnabled() { return interestEnabled; }

    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public void setInterestEnabled(boolean interestEnabled) { this.interestEnabled = interestEnabled; }
    public void setLastInterestDate(LocalDate lastInterestDate) { this.lastInterestDate = lastInterestDate; }

    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        if (transaction instanceof Contribution || transaction instanceof InterestTransaction) {
            balance += transaction.getAmount();
        } else if (transaction instanceof Withdrawal) {
            balance -= transaction.getAmount();
        }
    }

    public double getTotalContributions() {
        return transactionHistory.stream()
            .filter(t -> t instanceof Contribution)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    public double getTotalWithdrawals() {
        return transactionHistory.stream()
            .filter(t -> t instanceof Withdrawal)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    public double getTotalInterest() {
        return transactionHistory.stream()
            .filter(t -> t instanceof InterestTransaction)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}