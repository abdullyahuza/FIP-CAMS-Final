package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.*;

import model.Account;
import model.Contribution;
import model.InterestTransaction;
import model.Loan;
import model.Member;
import model.Transaction;
import model.User;
import model.Withdrawal;
import service.AssociationService;

// GUI Implementation using Swing
public class ThriftAssociationGUI extends JFrame {
    private AssociationService associationService;
    private JTabbedPane tabbedPane;
    private JTextArea outputArea;

    public ThriftAssociationGUI(AssociationService associationService) {
        this.associationService = associationService;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Thrift Association Management System - " + 
                associationService.getCurrentUser().getUsername() + 
                " (" + associationService.getCurrentUser().getRole() + ")");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Add tabs based on user permissions
        tabbedPane.addTab("Members", createMembersPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Loans", createLoansPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        
        if (associationService.hasPermission("CREATE_USER")) {
            tabbedPane.addTab("Administration", createAdminPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Output area
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Output"));
        add(scrollPane, BorderLayout.SOUTH);

        // Set window properties
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Member list
        JList<String> memberList = new JList<>();
        updateMemberList(memberList);
        JScrollPane listScrollPane = new JScrollPane(memberList);
        listScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        
        JButton addMemberBtn = new JButton("Add Member");
        JButton viewMemberBtn = new JButton("View Details");
        JButton updateMemberBtn = new JButton("Update Info");
        JButton deactivateMemberBtn = new JButton("Deactivate");
        JButton refreshBtn = new JButton("Refresh");
        
        addMemberBtn.addActionListener(e -> showAddMemberDialog(memberList));
        viewMemberBtn.addActionListener(e -> showMemberDetails(memberList));
        updateMemberBtn.addActionListener(e -> showUpdateMemberDialog(memberList));
        refreshBtn.addActionListener(e -> updateMemberList(memberList));
        
        buttonPanel.add(addMemberBtn);
        buttonPanel.add(viewMemberBtn);
        buttonPanel.add(updateMemberBtn);
        buttonPanel.add(deactivateMemberBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(new JLabel()); // Empty space
        
        panel.add(listScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Contribution panel
        JPanel contributionPanel = new JPanel(new GridBagLayout());
        contributionPanel.setBorder(BorderFactory.createTitledBorder("Make Contribution"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField memberIdField = new JTextField(15);
        JTextField amountField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        JButton contributeBtn = new JButton("Process Contribution");
        
        gbc.gridx = 0; gbc.gridy = 0; contributionPanel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1; contributionPanel.add(memberIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; contributionPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; contributionPanel.add(amountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; contributionPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; contributionPanel.add(descriptionField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; contributionPanel.add(contributeBtn, gbc);
        
        contributeBtn.addActionListener(e -> {
            try {
                String memberId = memberIdField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                String description = descriptionField.getText().trim();
                if (description.isEmpty()) description = "GUI Contribution";
                
                if (associationService.deposit(memberId, amount, description)) {
                    appendOutput("Contribution processed successfully for " + memberId);
                    memberIdField.setText("");
                    amountField.setText("");
                    descriptionField.setText("");
                } else {
                    appendOutput("Failed to process contribution for " + memberId);
                }
            } catch (NumberFormatException ex) {
                appendOutput("Invalid amount entered");
            }
        });
        
        // Withdrawal panel
        JPanel withdrawalPanel = new JPanel(new GridBagLayout());
        withdrawalPanel.setBorder(BorderFactory.createTitledBorder("Make Withdrawal"));
        
        JTextField withdrawMemberIdField = new JTextField(15);
        JTextField withdrawAmountField = new JTextField(15);
        JTextField withdrawDescriptionField = new JTextField(15);
        JButton withdrawBtn = new JButton("Process Withdrawal");
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 0; withdrawalPanel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1; withdrawalPanel.add(withdrawMemberIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; withdrawalPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; withdrawalPanel.add(withdrawAmountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; withdrawalPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; withdrawalPanel.add(withdrawDescriptionField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; withdrawalPanel.add(withdrawBtn, gbc);
        
        withdrawBtn.addActionListener(e -> {
            try {
                String memberId = withdrawMemberIdField.getText().trim();
                double amount = Double.parseDouble(withdrawAmountField.getText().trim());
                String description = withdrawDescriptionField.getText().trim();
                if (description.isEmpty()) description = "GUI Withdrawal";
                
                if (associationService.withdraw(memberId, amount, description)) {
                    appendOutput("Withdrawal processed successfully for " + memberId);
                    withdrawMemberIdField.setText("");
                    withdrawAmountField.setText("");
                    withdrawDescriptionField.setText("");
                } else {
                    appendOutput("Failed to process withdrawal for " + memberId);
                }
            } catch (NumberFormatException ex) {
                appendOutput("Invalid amount entered");
            }
        });
        
        // Interest calculation panel
        JPanel interestPanel = new JPanel(new FlowLayout());
        interestPanel.setBorder(BorderFactory.createTitledBorder("Interest Management"));
        
        JButton calculateInterestBtn = new JButton("Calculate Interest");
        calculateInterestBtn.addActionListener(e -> {
            associationService.calculateAndApplyInterest();
            appendOutput("Interest calculation completed for all eligible accounts");
        });
        
        interestPanel.add(calculateInterestBtn);
        
        // Member statement panel
        JPanel statementPanel = new JPanel(new GridBagLayout());
        statementPanel.setBorder(BorderFactory.createTitledBorder("Member Statement"));
        
        JTextField statementMemberIdField = new JTextField(15);
        JButton generateStatementBtn = new JButton("Generate Statement");
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 0; statementPanel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1; statementPanel.add(statementMemberIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; statementPanel.add(generateStatementBtn, gbc);
        
        generateStatementBtn.addActionListener(e -> {
            String memberId = statementMemberIdField.getText().trim();
            if (!memberId.isEmpty()) {
                showMemberStatementDialog(memberId);
            }
        });
        
        panel.add(contributionPanel);
        panel.add(withdrawalPanel);
        panel.add(interestPanel);
        panel.add(statementPanel);
        
        return panel;
    }

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Loan list
        JList<String> loanList = new JList<>();
        updateLoanList(loanList);
        JScrollPane listScrollPane = new JScrollPane(loanList);
        listScrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        
        JButton applyLoanBtn = new JButton("Apply for Loan");
        JButton approveLoanBtn = new JButton("Approve Loan");
        JButton disburseLoanBtn = new JButton("Disburse Loan");
        JButton viewLoanBtn = new JButton("View Details");
        JButton refreshLoansBtn = new JButton("Refresh");
        
        applyLoanBtn.addActionListener(e -> showLoanApplicationDialog(loanList));
        approveLoanBtn.addActionListener(e -> approveLoan(loanList));
        disburseLoanBtn.addActionListener(e -> disburseLoan(loanList));
        refreshLoansBtn.addActionListener(e -> updateLoanList(loanList));
        
        buttonPanel.add(applyLoanBtn);
        buttonPanel.add(approveLoanBtn);
        buttonPanel.add(disburseLoanBtn);
        buttonPanel.add(viewLoanBtn);
        buttonPanel.add(refreshLoansBtn);
        buttonPanel.add(new JLabel()); // Empty space
        
        panel.add(listScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Summary report panel
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary Report"));
        
        JButton summaryBtn = new JButton("Generate Summary Report");
        summaryBtn.addActionListener(e -> showSummaryReport());
        summaryPanel.add(summaryBtn);
        
        // Monthly report panel
        JPanel monthlyPanel = new JPanel(new GridBagLayout());
        monthlyPanel.setBorder(BorderFactory.createTitledBorder("Monthly Report"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField yearField = new JTextField(10);
        JTextField monthField = new JTextField(10);
        JButton monthlyBtn = new JButton("Generate Monthly Report");
        
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        monthField.setText(String.valueOf(LocalDate.now().getMonthValue()));
        
        gbc.gridx = 0; gbc.gridy = 0; monthlyPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; monthlyPanel.add(yearField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; monthlyPanel.add(new JLabel("Month:"), gbc);
        gbc.gridx = 1; monthlyPanel.add(monthField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; monthlyPanel.add(monthlyBtn, gbc);
        
        monthlyBtn.addActionListener(e -> {
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                int month = Integer.parseInt(monthField.getText().trim());
                YearMonth yearMonth = YearMonth.of(year, month);
                AssociationService.MonthlyReport report = associationService.generateMonthlyReport(yearMonth);
                if (report != null) {
                    showMonthlyReportDialog(report);
                } else {
                    appendOutput("Unable to generate monthly report. Check permissions.");
                }
            } catch (Exception ex) {
                appendOutput("Invalid year/month entered");
            }
        });
        
        // Backup panel
        JPanel backupPanel = new JPanel(new FlowLayout());
        backupPanel.setBorder(BorderFactory.createTitledBorder("Data Management"));
        
        JButton backupBtn = new JButton("Create Backup");
        backupBtn.addActionListener(e -> {
            try {
                associationService.saveData();
                appendOutput("Backup created successfully");
            } catch (Exception ex) {
                appendOutput("Failed to create backup: " + ex.getMessage());
            }
        });
        
        backupPanel.add(backupBtn);
        
        panel.add(summaryPanel);
        panel.add(monthlyPanel);
        panel.add(backupPanel);
        panel.add(new JLabel()); // Empty space
        
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // User management panel
        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("User Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JComboBox<User.UserRole> roleComboBox = new JComboBox<>(User.UserRole.values());
        JButton createUserBtn = new JButton("Create User");
        
        gbc.gridx = 0; gbc.gridy = 0; userPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; userPanel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; userPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; userPanel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; userPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; userPanel.add(roleComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; userPanel.add(createUserBtn, gbc);
        
        createUserBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            User.UserRole role = (User.UserRole) roleComboBox.getSelectedItem();
            
            if (associationService.createUser(username, password, role)) {
                appendOutput("User created successfully: " + username);
                usernameField.setText("");
                passwordField.setText("");
            } else {
                appendOutput("Failed to create user. Username may already exist.");
            }
        });
        
        // System info panel
        JPanel systemPanel = new JPanel(new FlowLayout());
        systemPanel.setBorder(BorderFactory.createTitledBorder("System Information"));
        
        JButton systemInfoBtn = new JButton("Show System Info");
        systemInfoBtn.addActionListener(e -> showSystemInfo());
        
        systemPanel.add(systemInfoBtn);
        
        panel.add(userPanel);
        panel.add(systemPanel);
        
        return panel;
    }

    // Helper methods for GUI operations
    private void updateMemberList(JList<String> memberList) {
        List<Member> members = associationService.getAllMembers();
        String[] memberData = members.stream()
            .map(m -> String.format("%s - %s (Balance: ₦%.2f)", 
                 m.getMemberId(), m.getFullName(), m.getAccount().getBalance()))
            .toArray(String[]::new);
        memberList.setListData(memberData);
    }

    private void updateLoanList(JList<String> loanList) {
        List<Loan> loans = associationService.getAllLoans();
        String[] loanData = loans.stream()
            .map(l -> String.format("%s - %s (₦%.2f) - %s", 
                 l.getLoanId(), l.getMemberId(), l.getPrincipalAmount(), l.getStatus()))
            .toArray(String[]::new);
        loanList.setListData(loanData);
    }

    private void showAddMemberDialog(JList<String> memberList) {
        JDialog dialog = new JDialog(this, "Add New Member", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField firstNameField = new JTextField(15);
        JTextField lastNameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField addressField = new JTextField(15);
        JTextField occupationField = new JTextField(15);
        JTextField dobField = new JTextField(15);
        dobField.setToolTipText("Format: YYYY-MM-DD");
        
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; dialog.add(firstNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; dialog.add(lastNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; dialog.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; dialog.add(addressField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; dialog.add(new JLabel("Occupation:"), gbc);
        gbc.gridx = 1; dialog.add(occupationField, gbc);
        gbc.gridx = 0; gbc.gridy = 6; dialog.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1; dialog.add(dobField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            try {
                LocalDate dob = LocalDate.parse(dobField.getText().trim());
                if (associationService.addMember(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    addressField.getText().trim(),
                    dob,
                    occupationField.getText().trim())) {
                    
                    appendOutput("Member added successfully");
                    updateMemberList(memberList);
                    dialog.dispose();
                } else {
                    appendOutput("Failed to add member");
                }
            } catch (DateTimeParseException ex) {
                appendOutput("Invalid date format. Use YYYY-MM-DD");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showMemberDetails(JList<String> memberList) {
        String selected = memberList.getSelectedValue();
        if (selected == null) {
            appendOutput("Please select a member");
            return;
        }
        
        String memberId = selected.split(" - ")[0];
        java.util.Optional<Member> memberOpt = associationService.findMember(memberId);
        
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            showMemberDetailsDialog(member);
        }
    }

    private void showMemberDetailsDialog(Member member) {
        JDialog dialog = new JDialog(this, "Member Details - " + member.getFullName(), true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea detailsArea = new JTextArea(20, 50);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        StringBuilder details = new StringBuilder();
        details.append("MEMBER DETAILS\n");
        details.append("=".repeat(50)).append("\n");
        details.append("Member ID: ").append(member.getMemberId()).append("\n");
        details.append("Name: ").append(member.getFullName()).append("\n");
        details.append("Email: ").append(member.getEmail()).append("\n");
        details.append("Phone: ").append(member.getPhoneNumber()).append("\n");
        details.append("Address: ").append(member.getAddress() != null ? member.getAddress() : "Not provided").append("\n");
        details.append("Occupation: ").append(member.getOccupation() != null ? member.getOccupation() : "Not provided").append("\n");
        details.append("Date of Birth: ").append(member.getDateOfBirth() != null ? member.getDateOfBirth() : "Not provided").append("\n");
        details.append("Join Date: ").append(member.getJoinDate()).append("\n");
        details.append("Active: ").append(member.isActive() ? "Yes" : "No").append("\n");
        details.append("Credit Score: ").append(String.format("%.1f", member.getCreditScore())).append("\n");
        details.append("\nACCOUNT SUMMARY\n");
        details.append("=".repeat(50)).append("\n");
        Account account = member.getAccount();
        details.append("Current Balance: ₦").append(String.format("%.2f", account.getBalance())).append("\n");
        details.append("Total Contributions: ₦").append(String.format("%.2f", account.getTotalContributions())).append("\n");
        details.append("Total Withdrawals: ₦").append(String.format("%.2f", account.getTotalWithdrawals())).append("\n");
        details.append("Total Interest: ₦").append(String.format("%.2f", account.getTotalInterest())).append("\n");
        details.append("Interest Rate: ").append(String.format("%.2f", account.getInterestRate())).append("%\n");
        
        detailsArea.setText(details.toString());
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showUpdateMemberDialog(JList<String> memberList) {
        String selected = memberList.getSelectedValue();
        if (selected == null) {
            appendOutput("Please select a member");
            return;
        }
        
        String memberId = selected.split(" - ")[0];
        
        JDialog dialog = new JDialog(this, "Update Member Information", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("New Email:"), gbc);
        gbc.gridx = 1; dialog.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("New Phone:"), gbc);
        gbc.gridx = 1; dialog.add(phoneField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");
        
        updateBtn.addActionListener(e -> {
            if (associationService.updateMemberInfo(memberId, emailField.getText().trim(), phoneField.getText().trim())) {
                appendOutput("Member information updated successfully");
                updateMemberList(memberList);
                dialog.dispose();
            } else {
                appendOutput("Failed to update member information");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(updateBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showLoanApplicationDialog(JList<String> loanList) {
        JDialog dialog = new JDialog(this, "Loan Application", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField memberIdField = new JTextField(15);
        JTextField amountField = new JTextField(15);
        JTextField interestRateField = new JTextField(15);
        JTextField termField = new JTextField(15);
        JTextField purposeField = new JTextField(15);
        
        interestRateField.setText("12.0"); // Default rate
        termField.setText("12"); // Default term
        
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1; dialog.add(memberIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Loan Amount:"), gbc);
        gbc.gridx = 1; dialog.add(amountField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Interest Rate (%):"), gbc);
        gbc.gridx = 1; dialog.add(interestRateField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Term (months):"), gbc);
        gbc.gridx = 1; dialog.add(termField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; dialog.add(new JLabel("Purpose:"), gbc);
        gbc.gridx = 1; dialog.add(purposeField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");
        
        applyBtn.addActionListener(e -> {
            try {
                String memberId = memberIdField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                double interestRate = Double.parseDouble(interestRateField.getText().trim());
                int term = Integer.parseInt(termField.getText().trim());
                String purpose = purposeField.getText().trim();
                
                if (associationService.applyForLoan(memberId, amount, interestRate, term, purpose)) {
                    appendOutput("Loan application submitted successfully");
                    updateLoanList(loanList);
                    dialog.dispose();
                } else {
                    appendOutput("Failed to submit loan application");
                }
            } catch (NumberFormatException ex) {
                appendOutput("Invalid numeric values entered");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void approveLoan(JList<String> loanList) {
        String selected = loanList.getSelectedValue();
        if (selected == null) {
            appendOutput("Please select a loan");
            return;
        }
        
        String loanId = selected.split(" - ")[0];
        if (associationService.approveLoan(loanId)) {
            appendOutput("Loan approved: " + loanId);
            updateLoanList(loanList);
        } else {
            appendOutput("Failed to approve loan: " + loanId);
        }
    }

    private void disburseLoan(JList<String> loanList) {
        String selected = loanList.getSelectedValue();
        if (selected == null) {
            appendOutput("Please select a loan");
            return;
        }
        
        String loanId = selected.split(" - ")[0];
        if (associationService.disburseLoan(loanId)) {
            appendOutput("Loan disbursed: " + loanId);
            updateLoanList(loanList);
        } else {
            appendOutput("Failed to disburse loan: " + loanId);
        }
    }

    private void showMemberStatementDialog(String memberId) {
        java.util.Optional<Member> memberOpt = associationService.findMember(memberId);
        if (!memberOpt.isPresent()) {
            appendOutput("Member not found: " + memberId);
            return;
        }

        Member member = memberOpt.get();
        
        JDialog dialog = new JDialog(this, "Member Statement - " + member.getFullName(), true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea statementArea = new JTextArea(25, 60);
        statementArea.setEditable(false);
        statementArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        
        StringBuilder statement = new StringBuilder();
        Account account = member.getAccount();

        statement.append("MEMBER STATEMENT\n");
        statement.append("=".repeat(80)).append("\n");
        statement.append("Member: ").append(member.getFullName()).append("\n");
        statement.append("Member ID: ").append(member.getMemberId()).append("\n");
        statement.append("Email: ").append(member.getEmail()).append("\n");
        statement.append("Phone: ").append(member.getPhoneNumber()).append("\n");
        statement.append("Address: ").append(member.getAddress() != null ? member.getAddress() : "Not provided").append("\n");
        statement.append("Join Date: ").append(member.getJoinDate()).append("\n");
        statement.append("Current Balance: ₦").append(String.format("%.2f", account.getBalance())).append("\n");
        statement.append("Total Contributions: ₦").append(String.format("%.2f", account.getTotalContributions())).append("\n");
        statement.append("Total Withdrawals: ₦").append(String.format("%.2f", account.getTotalWithdrawals())).append("\n");
        statement.append("Total Interest Earned: ₦").append(String.format("%.2f", account.getTotalInterest())).append("\n");
        statement.append("Interest Rate: ").append(String.format("%.2f", account.getInterestRate())).append("% APR\n\n");
        
        statement.append("TRANSACTION HISTORY\n");
        statement.append("-".repeat(80)).append("\n");
        statement.append(String.format("%-12s %-12s %-10s %-12s %-30s%n", 
                        "Date", "Type", "Amount", "Balance", "Description"));
        statement.append("-".repeat(80)).append("\n");
        
        if (account.getTransactionHistory().isEmpty()) {
            statement.append("No transactions found.\n");
        } else {
            double runningBalance = 0;
            for (Transaction transaction : account.getTransactionHistory()) {
                if (transaction instanceof Contribution || transaction instanceof InterestTransaction) {
                    runningBalance += transaction.getAmount();
                } else if (transaction instanceof Withdrawal) {
                    runningBalance -= transaction.getAmount();
                }
                
                statement.append(String.format("%-12s %-12s ₦%-9.2f ₦%-11.2f %-30s%n",
                    transaction.getDate(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    runningBalance,
                    transaction.getDescription()));
            }
        }
        statement.append("=".repeat(80));
        
        statementArea.setText(statement.toString());
        
        JScrollPane scrollPane = new JScrollPane(statementArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");
        
        printBtn.addActionListener(e -> {
            try {
                statementArea.print();
            } catch (Exception ex) {
                appendOutput("Print failed: " + ex.getMessage());
            }
        });
        
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSummaryReport() {
        List<Member> members = associationService.getAllMembers();
        List<Loan> loans = associationService.getAllLoans();
        
        if (members.isEmpty()) {
            appendOutput("No data available or insufficient permissions.");
            return;
        }

        JDialog dialog = new JDialog(this, "Association Summary Report", true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea reportArea = new JTextArea(20, 50);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        StringBuilder report = new StringBuilder();
        
        report.append("ASSOCIATION SUMMARY REPORT\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("=".repeat(60)).append("\n");
        
        long activeMembers = members.stream().filter(Member::isActive).count();
        double totalBalance = members.stream().mapToDouble(m -> m.getAccount().getBalance()).sum();
        double totalContributions = members.stream().mapToDouble(m -> m.getAccount().getTotalContributions()).sum();
        double totalWithdrawals = members.stream().mapToDouble(m -> m.getAccount().getTotalWithdrawals()).sum();
        double totalInterest = members.stream().mapToDouble(m -> m.getAccount().getTotalInterest()).sum();
        
        report.append("MEMBERSHIP SUMMARY\n");
        report.append("Total Members: ").append(members.size()).append("\n");
        report.append("Active Members: ").append(activeMembers).append("\n");
        report.append("Inactive Members: ").append(members.size() - activeMembers).append("\n\n");
        
        report.append("FINANCIAL SUMMARY\n");
        report.append("Total Association Balance: ₦").append(String.format("%.2f", totalBalance)).append("\n");
        report.append("Total Contributions: ₦").append(String.format("%.2f", totalContributions)).append("\n");
        report.append("Total Withdrawals: ₦").append(String.format("%.2f", totalWithdrawals)).append("\n");
        report.append("Total Interest Paid: ₦").append(String.format("%.2f", totalInterest)).append("\n");
        report.append("Net Cash Flow: ₦").append(String.format("%.2f", totalContributions - totalWithdrawals)).append("\n\n");
        
        report.append("LOAN SUMMARY\n");
        report.append("Total Loans: ").append(loans.size()).append("\n");
        
        java.util.Map<Loan.LoanStatus, Long> loansByStatus = loans.stream()
            .collect(java.util.stream.Collectors.groupingBy(Loan::getStatus, 
                     java.util.stream.Collectors.counting()));
        
        for (java.util.Map.Entry<Loan.LoanStatus, Long> entry : loansByStatus.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        double totalOutstandingLoans = loans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.DISBURSED || 
                          loan.getStatus() == Loan.LoanStatus.ACTIVE)
            .mapToDouble(Loan::getOutstandingBalance)
            .sum();
        
        report.append("Total Outstanding Loans: ₦").append(String.format("%.2f", totalOutstandingLoans)).append("\n");
        report.append("=".repeat(60));
        
        reportArea.setText(report.toString());
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");
        
        printBtn.addActionListener(e -> {
            try {
                reportArea.print();
            } catch (Exception ex) {
                appendOutput("Print failed: " + ex.getMessage());
            }
        });
        
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showMonthlyReportDialog(AssociationService.MonthlyReport report) {
        JDialog dialog = new JDialog(this, "Monthly Report - " + report.getMonth(), true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea reportArea = new JTextArea(15, 40);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setText(report.toString());
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");
        
        printBtn.addActionListener(e -> {
            try {
                reportArea.print();
            } catch (Exception ex) {
                appendOutput("Print failed: " + ex.getMessage());
            }
        });
        
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSystemInfo() {
        User currentUser = associationService.getCurrentUser();
        List<Member> members = associationService.getAllMembers();
        
        JDialog dialog = new JDialog(this, "System Information", true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea infoArea = new JTextArea(15, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        StringBuilder info = new StringBuilder();
        info.append("SYSTEM INFORMATION\n");
        info.append("=".repeat(40)).append("\n");
        info.append("Current User: ").append(currentUser.getUsername()).append("\n");
        info.append("User Role: ").append(currentUser.getRole()).append("\n");
        info.append("User Created: ").append(currentUser.getCreatedDate()).append("\n");
        info.append("Last Login: ").append(currentUser.getLastLoginDate()).append("\n\n");
        
        info.append("APPLICATION STATS\n");
        info.append("=".repeat(40)).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Operating System: ").append(System.getProperty("os.name")).append("\n");
        info.append("Total Members: ").append(members.size()).append("\n");
        info.append("Data Directory: data/\n");
        info.append("Backup Available: Yes\n");
        
        infoArea.setText(info.toString());
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void appendOutput(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append("[" + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
}

