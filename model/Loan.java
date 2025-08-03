package model;

import java.io.Serializable;
import java.time.LocalDate;

// Loan class
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String loanId;
    private String memberId;
    private double principalAmount;
    private double interestRate;
    private int termInMonths;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private LoanStatus status;
    private double outstandingBalance;
    private double monthlyPayment;
    private String purpose;

    public enum LoanStatus {
        PENDING, APPROVED, DISBURSED, ACTIVE, PAID_OFF, DEFAULTED, REJECTED
    }

    public Loan(String loanId, String memberId, double principalAmount, double interestRate, 
                int termInMonths, String purpose) {
        this.loanId = loanId;
        this.memberId = memberId;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termInMonths = termInMonths;
        this.purpose = purpose;
        this.applicationDate = LocalDate.now();
        this.status = LoanStatus.PENDING;
        this.outstandingBalance = principalAmount;
        this.monthlyPayment = calculateMonthlyPayment();
    }

    private double calculateMonthlyPayment() {
        double monthlyRate = interestRate / 100 / 12;
        if (monthlyRate == 0) return principalAmount / termInMonths;
        
        return principalAmount * (monthlyRate * Math.pow(1 + monthlyRate, termInMonths)) / 
               (Math.pow(1 + monthlyRate, termInMonths) - 1);
    }

    // Getters and Setters
    public String getLoanId() { return loanId; }
    public String getMemberId() { return memberId; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getInterestRate() { return interestRate; }
    public int getTermInMonths() { return termInMonths; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public LocalDate getDisbursementDate() { return disbursementDate; }
    public LoanStatus getStatus() { return status; }
    public double getOutstandingBalance() { return outstandingBalance; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public String getPurpose() { return purpose; }

    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public void setOutstandingBalance(double outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    @Override
    public String toString() {
        return String.format("Loan ID: %s | Amount: ₦%.2f | Rate: %.2f%% | Term: %d months | Status: %s | Balance: ₦%.2f",
            loanId, principalAmount, interestRate, termInMonths, status, outstandingBalance);
    }
}
