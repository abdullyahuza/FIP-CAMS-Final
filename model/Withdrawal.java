package model;

import java.time.LocalDate;

public class Withdrawal extends Transaction {
    public Withdrawal(String transactionId, String memberId, double amount, LocalDate date, String description) {
        super(transactionId, memberId, amount, date, description);
    }

    @Override
    public String getTransactionType() {
        return "WITHDRAWAL";
    }
}