package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * Domain entity representing a User in the system (e.g. Author, Reviewer, Admin, Teacher, Student).
 *
 * <p>Strictly independent of persistence frameworks.</p>
 */
public class User {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;

    public User() {
        this.active = true;
    }

    public User(Long id, String username, String fullName, String email, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public User(String username, String fullName, String email, Role role) {
        this(null, username, fullName, email, role, true);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                '}';
    }
}
