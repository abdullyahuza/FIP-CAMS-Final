package model;

import java.io.Serializable;
import java.time.LocalDate;

// User class for authentication
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private boolean isActive;
    private LocalDate createdDate;
    private LocalDate lastLoginDate;

    public enum UserRole {
        ADMIN, MANAGER, TELLER, MEMBER
    }

    public User(String userId, String username, String passwordHash, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.createdDate = LocalDate.now();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return isActive; }
    public LocalDate getCreatedDate() { return createdDate; }
    public LocalDate getLastLoginDate() { return lastLoginDate; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active) { isActive = active; }
    public void setLastLoginDate(LocalDate lastLoginDate) { this.lastLoginDate = lastLoginDate; }
}