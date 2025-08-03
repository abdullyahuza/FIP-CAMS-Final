package model;

import java.time.LocalDate;

public class InterestTransaction extends Transaction {
    private double interestRate;

    public InterestTransaction(String transactionId, String memberId, double amount, LocalDate date, double interestRate) {
        super(transactionId, memberId, amount, date, "Interest Payment - " + interestRate + "% APR");
        this.interestRate = interestRate;
    }

    public double getInterestRate() { return interestRate; }

    @Override
    public String getTransactionType() {
        return "INTEREST";
    }
}