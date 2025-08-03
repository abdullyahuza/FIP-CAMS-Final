package service;

import model.*;
import validation.TransactionValidator;
import persistence.DataPersistence;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Enhanced Association Service
public class AssociationService {
    private List<Member> members;
    private List<Transaction> allTransactions;
    private List<Loan> loans;
    private List<User> users;
    private TransactionValidator validator;
    private DataPersistence dataPersistence;
    private int transactionCounter;
    private int loanCounter;
    private User currentUser;

    public AssociationService() {
        this.members = new ArrayList<>();
        this.allTransactions = new ArrayList<>();
        this.loans = new ArrayList<>();
        this.users = new ArrayList<>();
        this.validator = new TransactionValidator();
        this.dataPersistence = new DataPersistence();
        this.transactionCounter = 1;
        this.loanCounter = 1;
        
        // Load data from persistence
        loadData();
        
        // Create default admin user if no users exist
        if (users.isEmpty()) {
            createDefaultAdmin();
        }
    }

    // Authentication Methods
    public boolean authenticateUser(String username, String password) {
        String passwordHash = hashPassword(password);
        Optional<User> userOpt = users.stream()
            .filter(user -> user.getUsername().equals(username) && 
                          user.getPasswordHash().equals(passwordHash) && 
                          user.isActive())
            .findFirst();
        
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            currentUser.setLastLoginDate(LocalDate.now());
            saveData();
            return true;
        }
        return false;
    }

    public boolean createUser(String username, String password, User.UserRole role) {
        if (!hasPermission("CREATE_USER")) return false;
        
        // Check if username already exists
        boolean exists = users.stream().anyMatch(user -> user.getUsername().equals(username));
        if (exists) return false;
        
        String userId = "USR" + String.format("%04d", users.size() + 1);
        String passwordHash = hashPassword(password);
        User newUser = new User(userId, username, passwordHash, role);
        users.add(newUser);
        saveData();
        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void createDefaultAdmin() {
        String defaultPassword = "admin123";
        String userId = "USR0001";
        String passwordHash = hashPassword(defaultPassword);
        User admin = new User(userId, "admin", passwordHash, User.UserRole.ADMIN);
        users.add(admin);
        System.out.println("Default admin user created. Username: admin, Password: admin123");
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasPermission(String permission) {
        if (currentUser == null) return false;
        
        switch (currentUser.getRole()) {
            case ADMIN:
                return true; // Admin has all permissions
            case MANAGER:
                return !permission.equals("CREATE_USER"); // Manager can do most things except create users
            case TELLER:
                return permission.equals("VIEW_MEMBERS") || permission.equals("PROCESS_TRANSACTIONS");
            case MEMBER:
                return permission.equals("VIEW_OWN_ACCOUNT");
            default:
                return false;
        }
    }

    // Data Persistence Methods
    private void loadData() {
        try {
            members = dataPersistence.loadMembers();
            allTransactions = dataPersistence.loadTransactions();
            loans = dataPersistence.loadLoans();
            users = dataPersistence.loadUsers();
            
            // Update counters
            transactionCounter = allTransactions.size() + 1;
            loanCounter = loans.size() + 1;
            
            // Rebuild account transaction histories
            rebuildAccountHistories();
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void rebuildAccountHistories() {
        // Clear existing histories
        for (Member member : members) {
            member.getAccount().getTransactionHistory().clear();
        }
        
        // Rebuild from all transactions
        for (Transaction transaction : allTransactions) {
            Optional<Member> memberOpt = findMember(transaction.getMemberId());
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                member.getAccount().addTransaction(transaction);
            }
        }
    }

    public void saveData() {
        try {
            dataPersistence.saveMembers(members);
            dataPersistence.saveTransactions(allTransactions);
            dataPersistence.saveLoans(loans);
            dataPersistence.saveUsers(users);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // Enhanced Member Management
    public boolean addMember(String firstName, String lastName, String email, String phoneNumber, 
                           String address, LocalDate dateOfBirth, String occupation) {
        if (!hasPermission("ADD_MEMBER")) return false;
        
        try {
            String memberId = "MEM" + String.format("%04d", members.size() + 1);
            Member member = new Member(memberId, firstName, lastName, email, phoneNumber, LocalDate.now());
            member.setAddress(address);
            member.setDateOfBirth(dateOfBirth);
            member.setOccupation(occupation);
            members.add(member);
            saveData();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding member: " + e.getMessage());
            return false;
        }
    }

    public Optional<Member> findMember(String memberId) {
        return members.stream()
            .filter(member -> member.getMemberId().equals(memberId))
            .findFirst();
    }

    public boolean updateMemberInfo(String memberId, String email, String phoneNumber) {
        if (!hasPermission("EDIT_MEMBER")) return false;

        Optional<Member> memberOpt = findMember(memberId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            if (email != null && !email.trim().isEmpty()) {
                member.setEmail(email);
            }
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                member.setPhoneNumber(phoneNumber);
            }
            saveData(); // persist the changes
            return true;
        } else {
            System.err.println("Member with ID " + memberId + " not found.");
        }
        return false;
    }


    public List<Member> getAllMembers() {
        if (!hasPermission("VIEW_MEMBERS")) return new ArrayList<>();
        return new ArrayList<>(members);
    }

    // Interest Calculation
    public void calculateAndApplyInterest() {
        if (!hasPermission("PROCESS_INTEREST")) return;
        
        LocalDate today = LocalDate.now();
        
        for (Member member : members) {
            Account account = member.getAccount();
            if (!account.isInterestEnabled() || account.getBalance() <= 0) continue;
            
            LocalDate lastInterestDate = account.getLastInterestDate();
            long daysSinceLastInterest = ChronoUnit.DAYS.between(lastInterestDate, today);
            
            // Apply interest monthly
            if (daysSinceLastInterest >= 30) {
                double dailyRate = account.getInterestRate() / 100 / 365;
                double interestAmount = account.getBalance() * dailyRate * daysSinceLastInterest;
                
                if (interestAmount > 0.01) { // Only apply if interest is more than 1 cent
                    String transactionId = "TXN" + String.format("%06d", transactionCounter++);
                    InterestTransaction interestTransaction = new InterestTransaction(
                        transactionId, member.getMemberId(), interestAmount, today, account.getInterestRate());
                    
                    account.addTransaction(interestTransaction);
                    allTransactions.add(interestTransaction);
                    account.setLastInterestDate(today);
                }
            }
        }
        saveData();
    }

    // Enhanced Transaction Processing
    public boolean deposit(String memberId, double amount, String description) {
        if (!hasPermission("PROCESS_TRANSACTIONS")) return false;
        
        Optional<Member> memberOpt = findMember(memberId);
        if (!memberOpt.isPresent()) {
            System.err.println("Member not found: " + memberId);
            return false;
        }

        Member member = memberOpt.get();
        
        if (!validator.validateContribution(amount)) {
            System.err.println("Contribution validation failed.");
            return false;
        }

        try {
            String transactionId = "TXN" + String.format("%06d", transactionCounter++);
            Contribution contribution = new Contribution(transactionId, memberId, amount, LocalDate.now(), description);
            
            member.getAccount().addTransaction(contribution);
            allTransactions.add(contribution);
            saveData();
            
            System.out.println("Contribution successful! New balance: ₦" + 
                String.format("%.2f", member.getAccount().getBalance()));
            return true;
        } catch (Exception e) {
            System.err.println("Error processing contribution: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(String memberId, double amount, String description) {
        if (!hasPermission("PROCESS_TRANSACTIONS")) return false;
        
        Optional<Member> memberOpt = findMember(memberId);
        if (!memberOpt.isPresent()) {
            System.err.println("Member not found: " + memberId);
            return false;
        }

        Member member = memberOpt.get();
        
        if (!validator.validateWithdrawal(amount, member.getAccount().getBalance(), member.getMembershipDurationInDays())) {
            System.err.println("Withdrawal validation failed.");
            return false;
        }

        try {
            String transactionId = "TXN" + String.format("%06d", transactionCounter++);
            Withdrawal withdrawal = new Withdrawal(transactionId, memberId, amount, LocalDate.now(), description);
            
            member.getAccount().addTransaction(withdrawal);
            allTransactions.add(withdrawal);
            saveData();
            
            System.out.println("Withdrawal successful! New balance: ₦" + 
                String.format("%.2f", member.getAccount().getBalance()));
            return true;
        } catch (Exception e) {
            System.err.println("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    // Loan Management
    public boolean applyForLoan(String memberId, double amount, double interestRate, int termInMonths, String purpose) {
        Optional<Member> memberOpt = findMember(memberId);
        if (!memberOpt.isPresent()) {
            System.err.println("Member not found: " + memberId);
            return false;
        }

        Member member = memberOpt.get();
        
        // Check eligibility
        if (member.getMembershipDurationInDays() < 90) {
            System.err.println("Member must be registered for at least 90 days to apply for a loan.");
            return false;
        }

        if (member.getAccount().getBalance() < amount * 0.1) {
            System.err.println("Member must have at least 10% of loan amount as savings.");
            return false;
        }

        try {
            String loanId = "LOAN" + String.format("%04d", loanCounter++);
            Loan loan = new Loan(loanId, memberId, amount, interestRate, termInMonths, purpose);
            loans.add(loan);
            saveData();
            
            System.out.println("Loan application submitted successfully. Loan ID: " + loanId);
            return true;
        } catch (Exception e) {
            System.err.println("Error processing loan application: " + e.getMessage());
            return false;
        }
    }

    public boolean approveLoan(String loanId) {
        if (!hasPermission("APPROVE_LOANS")) return false;
        
        Optional<Loan> loanOpt = loans.stream()
            .filter(loan -> loan.getLoanId().equals(loanId))
            .findFirst();
        
        if (!loanOpt.isPresent()) {
            System.err.println("Loan not found: " + loanId);
            return false;
        }

        Loan loan = loanOpt.get();
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            System.err.println("Loan is not in pending status.");
            return false;
        }

        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        saveData();
        
        System.out.println("Loan approved successfully: " + loanId);
        return true;
    }

    public boolean disburseLoan(String loanId) {
        if (!hasPermission("DISBURSE_LOANS")) return false;
        
        Optional<Loan> loanOpt = loans.stream()
            .filter(loan -> loan.getLoanId().equals(loanId))
            .findFirst();
        
        if (!loanOpt.isPresent()) {
            System.err.println("Loan not found: " + loanId);
            return false;
        }

        Loan loan = loanOpt.get();
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            System.err.println("Loan must be approved before disbursement.");
            return false;
        }

        // Credit the loan amount to member's account
        boolean success = deposit(loan.getMemberId(), loan.getPrincipalAmount(), 
                                "Loan disbursement - " + loan.getLoanId());
        
        if (success) {
            loan.setStatus(Loan.LoanStatus.DISBURSED);
            loan.setDisbursementDate(LocalDate.now());
            saveData();
            System.out.println("Loan disbursed successfully: " + loanId);
            return true;
        }
        
        return false;
    }

    public List<Loan> getAllLoans() {
        if (!hasPermission("VIEW_LOANS")) return new ArrayList<>();
        return new ArrayList<>(loans);
    }

    public List<Loan> getMemberLoans(String memberId) {
        return loans.stream()
            .filter(loan -> loan.getMemberId().equals(memberId))
            .collect(Collectors.toList());
    }

    // Advanced Reporting
    public MonthlyReport generateMonthlyReport(YearMonth month) {
        if (!hasPermission("GENERATE_REPORTS")) return null;
        
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        
        List<Transaction> monthlyTransactions = allTransactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .collect(Collectors.toList());
        
        double totalContributions = monthlyTransactions.stream()
            .filter(t -> t instanceof Contribution)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double totalWithdrawals = monthlyTransactions.stream()
            .filter(t -> t instanceof Withdrawal)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double totalInterest = monthlyTransactions.stream()
            .filter(t -> t instanceof InterestTransaction)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        int newMembers = (int) members.stream()
            .filter(m -> !m.getJoinDate().isBefore(startDate) && !m.getJoinDate().isAfter(endDate))
            .count();
        
        double totalBalance = members.stream()
            .mapToDouble(m -> m.getAccount().getBalance())
            .sum();
        
        return new MonthlyReport(month, totalContributions, totalWithdrawals, totalInterest, 
                               newMembers, members.size(), totalBalance, monthlyTransactions.size());
    }

    // Monthly Report class
    public static class MonthlyReport {
        private YearMonth month;
        private double totalContributions;
        private double totalWithdrawals;
        private double totalInterest;
        private int newMembers;
        private int totalMembers;
        private double totalBalance;
        private int totalTransactions;

        public MonthlyReport(YearMonth month, double totalContributions, double totalWithdrawals,
                           double totalInterest, int newMembers, int totalMembers, 
                           double totalBalance, int totalTransactions) {
            this.month = month;
            this.totalContributions = totalContributions;
            this.totalWithdrawals = totalWithdrawals;
            this.totalInterest = totalInterest;
            this.newMembers = newMembers;
            this.totalMembers = totalMembers;
            this.totalBalance = totalBalance;
            this.totalTransactions = totalTransactions;
        }

        // Getters
        public YearMonth getMonth() { return month; }
        public double getTotalContributions() { return totalContributions; }
        public double getTotalWithdrawals() { return totalWithdrawals; }
        public double getTotalInterest() { return totalInterest; }
        public int getNewMembers() { return newMembers; }
        public int getTotalMembers() { return totalMembers; }
        public double getTotalBalance() { return totalBalance; }
        public int getTotalTransactions() { return totalTransactions; }
        public double getNetFlow() { return totalContributions - totalWithdrawals; }

        @Override
        public String toString() {
            return String.format(
                "\n=== MONTHLY REPORT - %s ===\n" +
                "Total Members: %d (New: %d)\n" +
                "Total Transactions: %d\n" +
                "Total Contributions: ₦%.2f\n" +
                "Total Withdrawals: ₦%.2f\n" +
                "Total Interest Paid: ₦%.2f\n" +
                "Net Cash Flow: ₦%.2f\n" +
                "Total Association Balance: ₦%.2f\n" +
                "================================",
                month.toString(), totalMembers, newMembers, totalTransactions,
                totalContributions, totalWithdrawals, totalInterest, getNetFlow(), totalBalance
            );
        }
    }
}
