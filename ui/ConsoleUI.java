package ui;

import service.AssociationService;
import model.*;
import javax.swing.*;
import java.io.Console;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Enhanced Console UI with authentication
public class ConsoleUI {
    private AssociationService associationService;
    private Scanner scanner;

    public ConsoleUI() {
        this.associationService = new AssociationService();
        this.scanner = new java.util.Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Enhanced Cooperative/Thrift Association Management System");
        System.out.println("=".repeat(75));

        // Authentication required
        if (!authenticateUser()) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }

        System.out.println("Login successful! Welcome, " + associationService.getCurrentUser().getUsername());
        
        while (true) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1: addNewMember(); break;
                case 2: viewAllMembers(); break;
                case 3: updateMemberInfo(); break;
                case 4: makeContribution(); break;
                case 5: makeWithdrawal(); break;
                case 6: viewMemberStatement(); break;
                case 7: viewSummaryReport(); break;
                case 8: calculateInterest(); break;
                case 9: loanManagement(); break;
                case 10: generateMonthlyReport(); break;
                case 11: userManagement(); break;
                case 12: createBackup(); break;
                case 13: launchGUI(); break;
                case 14: 
                    associationService.logout();
                    System.out.println("Thank you for using the Association Management System!");
                    return;
                default:
                    System.err.println("Invalid choice. Please try again.");
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private boolean authenticateUser() {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.println("\n=== LOGIN REQUIRED ===");
            String username = getStringInput("Username: ");
            String password = getPasswordInput("Password: ");

            if (associationService.authenticateUser(username, password)) {
                return true;
            }

            attempts++;
            System.err.println("Invalid credentials. Attempts remaining: " + (MAX_ATTEMPTS - attempts));
        }

        return false;
    }

    private String getPasswordInput(String prompt) {
        System.out.print(prompt);
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword();
            return new String(password);
        } else {
            // Fallback for IDEs that don't support console
            return scanner.nextLine();
        }
    }

    private void displayMainMenu() {
        User currentUser = associationService.getCurrentUser();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("MAIN MENU - " + currentUser.getRole() + " (" + currentUser.getUsername() + ")");
        System.out.println("=".repeat(50));
        System.out.println("1. Add New Member");
        System.out.println("2. View All Members");
        System.out.println("3. Update Member Information");
        System.out.println("4. Make Contribution");
        System.out.println("5. Make Withdrawal");
        System.out.println("6. View Member Statement");
        System.out.println("7. View Summary Report");
        System.out.println("8. Calculate Interest");
        System.out.println("9. Loan Management");
        System.out.println("10. Generate Monthly Report");
        System.out.println("11. User Management");
        System.out.println("12. Create Backup");
        System.out.println("13. Launch GUI Interface");
        System.out.println("14. Exit");
        System.out.println("=".repeat(50));
    }

    private void addNewMember() {
        System.out.println("\n--- Add New Member ---");
        
        String firstName = getStringInput("Enter first name: ");
        String lastName = getStringInput("Enter last name: ");
        String email = getStringInput("Enter email: ");
        String phoneNumber = getStringInput("Enter phone number: ");
        String address = getStringInput("Enter address: ");
        String occupation = getStringInput("Enter occupation: ");
        
        LocalDate dateOfBirth = null;
        while (dateOfBirth == null) {
            try {
                String dobString = getStringInput("Enter date of birth (YYYY-MM-DD): ");
                dateOfBirth = LocalDate.parse(dobString);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        if (associationService.addMember(firstName, lastName, email, phoneNumber, address, dateOfBirth, occupation)) {
            System.out.println("Member added successfully!");
        } else {
            System.err.println("Failed to add member. Please check permissions or try again.");
        }
    }

    private void calculateInterest() {
        System.out.println("\n--- Calculate Interest ---");
        System.out.println("Calculating and applying interest for all eligible accounts...");
        
        associationService.calculateAndApplyInterest();
        System.out.println("Interest calculation completed!");
    }

    private void loanManagement() {
        while (true) {
            System.out.println("\n--- Loan Management ---");
            System.out.println("1. Apply for Loan");
            System.out.println("2. View All Loans");
            System.out.println("3. Approve Loan");
            System.out.println("4. Disburse Loan");
            System.out.println("5. View Member Loans");
            System.out.println("6. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1: applyForLoan(); break;
                case 2: viewAllLoans(); break;
                case 3: approveLoan(); break;
                case 4: disburseLoan(); break;
                case 5: viewMemberLoans(); break;
                case 6: return;
                default: System.err.println("Invalid choice.");
            }
        }
    }

    private void applyForLoan() {
        System.out.println("\n--- Apply for Loan ---");
        
        String memberId = getStringInput("Enter member ID: ");
        double amount = getDoubleInput("Enter loan amount: ₦");
        double interestRate = getDoubleInput("Enter interest rate (%): ");
        int termInMonths = getIntInput("Enter loan term (months): ");
        String purpose = getStringInput("Enter loan purpose: ");

        if (associationService.applyForLoan(memberId, amount, interestRate, termInMonths, purpose)) {
            System.out.println("Loan application submitted successfully!");
        } else {
            System.err.println("Failed to submit loan application.");
        }
    }

    private void viewAllLoans() {
        System.out.println("\n--- All Loans ---");
        List<Loan> loans = associationService.getAllLoans();
        
        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }

        for (Loan loan : loans) {
            System.out.println(loan);
        }
    }

    private void approveLoan() {
        System.out.println("\n--- Approve Loan ---");
        String loanId = getStringInput("Enter loan ID to approve: ");
        
        if (associationService.approveLoan(loanId)) {
            System.out.println("Loan approved successfully!");
        } else {
            System.err.println("Failed to approve loan.");
        }
    }

    private void disburseLoan() {
        System.out.println("\n--- Disburse Loan ---");
        String loanId = getStringInput("Enter loan ID to disburse: ");
        
        if (associationService.disburseLoan(loanId)) {
            System.out.println("Loan disbursed successfully!");
        } else {
            System.err.println("Failed to disburse loan.");
        }
    }

    private void viewMemberLoans() {
        System.out.println("\n--- Member Loans ---");
        String memberId = getStringInput("Enter member ID: ");
        
        List<Loan> memberLoans = associationService.getMemberLoans(memberId);
        if (memberLoans.isEmpty()) {
            System.out.println("No loans found for this member.");
            return;
        }

        for (Loan loan : memberLoans) {
            System.out.println(loan);
        }
    }

    private void generateMonthlyReport() {
        System.out.println("\n--- Monthly Report ---");
        
        int year = getIntInput("Enter year (e.g., 2025): ");
        int month = getIntInput("Enter month (1-12): ");
        
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            AssociationService.MonthlyReport report = associationService.generateMonthlyReport(yearMonth);
            
            if (report != null) {
                System.out.println(report);
            } else {
                System.err.println("Unable to generate report. Check permissions.");
            }
        } catch (Exception e) {
            System.err.println("Invalid date. Please try again.");
        }
    }

    private void userManagement() {
        while (true) {
            System.out.println("\n--- User Management ---");
            System.out.println("1. Create New User");
            System.out.println("2. View Current User Info");
            System.out.println("3. Back to Main Menu");
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1: createNewUser(); break;
                case 2: viewCurrentUserInfo(); break;
                case 3: return;
                default: System.err.println("Invalid choice.");
            }
        }
    }

    private void createNewUser() {
        System.out.println("\n--- Create New User ---");
        
        String username = getStringInput("Enter username: ");
        String password = getPasswordInput("Enter password: ");
        
        System.out.println("Select user role:");
        System.out.println("1. ADMIN");
        System.out.println("2. MANAGER");
        System.out.println("3. TELLER");
        System.out.println("4. MEMBER");
        
        int roleChoice = getIntInput("Enter role choice (1-4): ");
        User.UserRole role;
        
        switch (roleChoice) {
            case 1: role = User.UserRole.ADMIN; break;
            case 2: role = User.UserRole.MANAGER; break;
            case 3: role = User.UserRole.TELLER; break;
            case 4: role = User.UserRole.MEMBER; break;
            default:
                System.err.println("Invalid role choice.");
                return;
        }

        if (associationService.createUser(username, password, role)) {
            System.out.println("User created successfully!");
        } else {
            System.err.println("Failed to create user. Username may already exist or insufficient permissions.");
        }
    }

    private void viewCurrentUserInfo() {
        User currentUser = associationService.getCurrentUser();
        System.out.println("\n--- Current User Information ---");
        System.out.println("User ID: " + currentUser.getUserId());
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Created: " + currentUser.getCreatedDate());
        System.out.println("Last Login: " + currentUser.getLastLoginDate());
        System.out.println("Active: " + (currentUser.isActive() ? "Yes" : "No"));
    }

    private void createBackup() {
        System.out.println("\n--- Create Backup ---");
        try {
            associationService.saveData();
            System.out.println("Backup created successfully!");
        } catch (Exception e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }

    private void launchGUI() {
        System.out.println("\n--- Launching GUI Interface ---");
        SwingUtilities.invokeLater(() -> {
            new ThriftAssociationGUI(associationService).setVisible(true);
        });
        System.out.println("GUI interface launched in a separate window.");
    }

    // Enhanced methods with better error handling
    private void viewAllMembers() {
        System.out.println("\n--- All Members ---");
        List<Member> members = associationService.getAllMembers();
        
        if (members.isEmpty()) {
            System.out.println("No members found or insufficient permissions.");
            return;
        }

        System.out.printf("%-8s %-20s %-25s %-15s %-12s %-10s%n", 
                         "ID", "Name", "Email", "Phone", "Join Date", "Balance");
        System.out.println("-".repeat(100));
        
        for (Member member : members) {
            System.out.printf("%-8s %-20s %-25s %-15s %-12s ₦%-9.2f%n",
                member.getMemberId(),
                member.getFullName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getJoinDate(),
                member.getAccount().getBalance());
        }
    }

    private void updateMemberInfo() {
        System.out.println("\n--- Update Member Information ---");
        
        String memberId = getStringInput("Enter member ID: ");
        String email = getStringInput("Enter new email (or press Enter to skip): ");
        String phoneNumber = getStringInput("Enter new phone number (or press Enter to skip): ");

        if (associationService.updateMemberInfo(memberId, email, phoneNumber)) {
            System.out.println("Member information updated successfully!");
        } else {
            System.err.println("Failed to update member information. Please check the member ID and permissions.");
        }
    }

    private void makeContribution() {
        System.out.println("\n--- Make Contribution ---");
        
        String memberId = getStringInput("Enter member ID: ");
        double amount = getDoubleInput("Enter contribution amount: ₦");
        String description = getStringInput("Enter description (optional): ");

        if (description.trim().isEmpty()) {
            description = "Regular contribution";
        }

        associationService.deposit(memberId, amount, description);
    }

    private void makeWithdrawal() {
        System.out.println("\n--- Make Withdrawal ---");
        
        String memberId = getStringInput("Enter member ID: ");
        double amount = getDoubleInput("Enter withdrawal amount: ₦");
        String description = getStringInput("Enter description (optional): ");

        if (description.trim().isEmpty()) {
            description = "Regular withdrawal";
        }

        associationService.withdraw(memberId, amount, description);
    }

    private void viewMemberStatement() {
        System.out.println("\n--- Member Statement ---");
        
        String memberId = getStringInput("Enter member ID: ");
        generateMemberStatement(memberId);
    }

    private void generateMemberStatement(String memberId) {
        java.util.Optional<Member> memberOpt = associationService.findMember(memberId);
        if (!memberOpt.isPresent()) {
            System.err.println("Member not found: " + memberId);
            return;
        }

        Member member = memberOpt.get();
        Account account = member.getAccount();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("MEMBER STATEMENT");
        System.out.println("=".repeat(80));
        System.out.println("Member: " + member.getFullName());
        System.out.println("Member ID: " + member.getMemberId());
        System.out.println("Email: " + member.getEmail());
        System.out.println("Phone: " + member.getPhoneNumber());
        System.out.println("Address: " + (member.getAddress() != null ? member.getAddress() : "Not provided"));
        System.out.println("Occupation: " + (member.getOccupation() != null ? member.getOccupation() : "Not provided"));
        System.out.println("Join Date: " + member.getJoinDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        System.out.println("Current Balance: ₦" + String.format("%.2f", account.getBalance()));
        System.out.println("Total Contributions: ₦" + String.format("%.2f", account.getTotalContributions()));
        System.out.println("Total Withdrawals: ₦" + String.format("%.2f", account.getTotalWithdrawals()));
        System.out.println("Total Interest Earned: ₦" + String.format("%.2f", account.getTotalInterest()));
        System.out.println("Interest Rate: " + String.format("%.2f", account.getInterestRate()) + "% APR");
        System.out.println("\nTransaction History:");
        System.out.println("-".repeat(80));
        
        if (account.getTransactionHistory().isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.printf("%-12s %-12s %-10s %-12s %-30s%n", 
                             "Date", "Type", "Amount", "Balance", "Description");
            System.out.println("-".repeat(80));
            
            double runningBalance = 0;
            for (Transaction transaction : account.getTransactionHistory()) {
                if (transaction instanceof Contribution || transaction instanceof InterestTransaction) {
                    runningBalance += transaction.getAmount();
                } else if (transaction instanceof Withdrawal) {
                    runningBalance -= transaction.getAmount();
                }
                
                System.out.printf("%-12s %-12s ₦%-9.2f ₦%-11.2f %-30s%n",
                    transaction.getDate(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    runningBalance,
                    transaction.getDescription());
            }
        }
        System.out.println("=".repeat(80));
    }

    private void viewSummaryReport() {
        System.out.println("\n--- Association Summary Report ---");
        List<Member> members = associationService.getAllMembers();
        List<Loan> loans = associationService.getAllLoans();
        
        if (members.isEmpty()) {
            System.out.println("No data available or insufficient permissions.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ASSOCIATION SUMMARY REPORT");
        System.out.println("=".repeat(60));
        System.out.println("Total Members: " + members.size());
        
        long activeMembers = members.stream().filter(Member::isActive).count();
        System.out.println("Active Members: " + activeMembers);
        
        double totalBalance = members.stream()
            .mapToDouble(member -> member.getAccount().getBalance())
            .sum();
        
        double totalContributions = members.stream()
            .mapToDouble(member -> member.getAccount().getTotalContributions())
            .sum();
        
        double totalWithdrawals = members.stream()
            .mapToDouble(member -> member.getAccount().getTotalWithdrawals())
            .sum();

        double totalInterest = members.stream()
            .mapToDouble(member -> member.getAccount().getTotalInterest())
            .sum();

        System.out.println("Total Association Balance: ₦" + String.format("%.2f", totalBalance));
        System.out.println("Total Contributions: ₦" + String.format("%.2f", totalContributions));
        System.out.println("Total Withdrawals: ₦" + String.format("%.2f", totalWithdrawals));
        System.out.println("Total Interest Paid: ₦" + String.format("%.2f", totalInterest));
        
        // Loan statistics
        System.out.println("\nLoan Summary:");
        System.out.println("Total Loans: " + loans.size());
        
        Map<Loan.LoanStatus, Long> loansByStatus = loans.stream()
            .collect(java.util.stream.Collectors.groupingBy(Loan::getStatus, 
                     java.util.stream.Collectors.counting()));
        
        for (Map.Entry<Loan.LoanStatus, Long> entry : loansByStatus.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        double totalLoanAmount = loans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.DISBURSED || 
                          loan.getStatus() == Loan.LoanStatus.ACTIVE)
            .mapToDouble(Loan::getOutstandingBalance)
            .sum();
        
        System.out.println("Total Outstanding Loans: ₦" + String.format("%.2f", totalLoanAmount));
        System.out.println("=".repeat(60));
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < 0) {
                    System.err.println("Amount cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid amount.");
            }
        }
    }
}
