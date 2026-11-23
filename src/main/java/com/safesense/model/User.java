package com.safesense.model;

/**
 * OOP: Encapsulation — demo user ready for a future registration page.
 * TODO: add registration (email uniqueness, password policy) in a later version.
 */
public class User {

    private String name;
    private String email;
    private String passwordHash;
    private String role;

    public User(String name, String email, String passwordHash, String role) {
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setRole(role);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        this.role = role.trim();
    }

    public String getFirstName() {
        int space = name.indexOf(' ');
        return space < 0 ? name : name.substring(0, space);
    }
}
