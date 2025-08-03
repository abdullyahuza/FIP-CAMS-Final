package model;

import java.time.LocalDate;

// Contribution class
public class Contribution extends Transaction {
    public Contribution(String transactionId, String memberId, double amount, LocalDate date, String description) {
        super(transactionId, memberId, amount, date, description);
    }

    @Override
    public String getTransactionType() {
        return "CONTRIBUTION";
    }
}