package com.certportal.model;

import java.io.Serializable;

/** Logged-in user stored in the HTTP session. */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String fullName;
    private final Role role;

    public User(String username, String fullName, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
