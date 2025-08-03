// Package: validation
package validation;

public class TransactionValidator {
    private static final double MAX_CONTRIBUTION_LIMIT = 10000.0;
    private static final double MAX_WITHDRAWAL_LIMIT = 5000.0;
    private static final long MIN_MEMBERSHIP_DAYS_FOR_WITHDRAWAL = 30;
    private static final double MAX_LOAN_AMOUNT = 50000.0;
    private static final double MIN_LOAN_AMOUNT = 100.0;

    public boolean validateContribution(double amount) {
        if (amount <= 0) {
            System.err.println("Contribution amount must be positive");
            return false;
        }
        
        if (amount > MAX_CONTRIBUTION_LIMIT) {
            System.err.println("Contribution amount exceeds maximum limit of ₦" + 
                String.format("%.2f", MAX_CONTRIBUTION_LIMIT));
            return false;
        }
        
        return true;
    }

    public boolean validateWithdrawal(double amount, double currentBalance, long membershipDays) {
        if (amount <= 0) {
            System.err.println("Withdrawal amount must be positive");
            return false;
        }
        
        if (amount > MAX_WITHDRAWAL_LIMIT) {
            System.err.println("Withdrawal amount exceeds maximum limit of ₦" + 
                String.format("%.2f", MAX_WITHDRAWAL_LIMIT));
            return false;
        }
        
        if (membershipDays < MIN_MEMBERSHIP_DAYS_FOR_WITHDRAWAL) {
            System.err.println("Member must be registered for at least " + 
                MIN_MEMBERSHIP_DAYS_FOR_WITHDRAWAL + " days before making withdrawals");
            return false;
        }
        
        if (amount > currentBalance) {
            System.err.println("Insufficient funds. Current balance: ₦" + 
                String.format("%.2f", currentBalance));
            return false;
        }
        
        return true;
    }

    public boolean validateLoan(double amount, double memberBalance, long membershipDays) {
        if (amount < MIN_LOAN_AMOUNT) {
            System.err.println("Loan amount must be at least ₦" + MIN_LOAN_AMOUNT);
            return false;
        }

        if (amount > MAX_LOAN_AMOUNT) {
            System.err.println("Loan amount exceeds maximum limit of ₦" + MAX_LOAN_AMOUNT);
            return false;
        }

        if (membershipDays < 90) {
            System.err.println("Member must be registered for at least 90 days to apply for a loan");
            return false;
        }

        if (memberBalance < amount * 0.1) {
            System.err.println("Member must have savings worth at least 10% of loan amount");
            return false;
        }

        return true;
    }
}