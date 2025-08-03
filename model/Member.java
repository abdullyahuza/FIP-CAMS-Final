package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate joinDate;
    private Account account;
    private boolean isActive;
    private String address;
    private LocalDate dateOfBirth;
    private String occupation;
    private double creditScore;

    public Member(String memberId, String firstName, String lastName, String email, 
                  String phoneNumber, LocalDate joinDate) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.joinDate = joinDate;
        this.account = new Account(memberId + "_ACC", this);
        this.isActive = true;
        this.creditScore = 700.0; // Default credit score
    }

    // Getters and Setters
    public String getMemberId() { return memberId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getJoinDate() { return joinDate; }
    public Account getAccount() { return account; }
    public boolean isActive() { return isActive; }
    public String getAddress() { return address; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getOccupation() { return occupation; }
    public double getCreditScore() { return creditScore; }

    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setActive(boolean active) { isActive = active; }
    public void setAddress(String address) { this.address = address; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public void setCreditScore(double creditScore) { this.creditScore = creditScore; }

    public long getMembershipDurationInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(joinDate, LocalDate.now());
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Email: %s | Phone: %s | Join Date: %s | Balance: ₦%.2f | Active: %s",
            memberId, getFullName(), email, phoneNumber, 
            joinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
            account.getBalance(), isActive ? "Yes" : "No");
    }
}