package com.certportal.dao;

import com.certportal.model.Role;
import com.certportal.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/** User accounts with salted PBKDF2 password hashes. */
public class UserDao {

    private static final int ITERATIONS = 65536;

    /** Returns the user when the credentials match, otherwise null. */
    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }
        String sql = "SELECT username, full_name, role, salt, password_hash FROM users WHERE username = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String expected = rs.getString("password_hash");
                String actual = hash(password, rs.getString("salt"));
                boolean ok = MessageDigest.isEqual(expected.getBytes(), actual.getBytes());
                if (!ok) {
                    return null;
                }
                return new User(rs.getString("username"), rs.getString("full_name"),
                        Role.valueOf(rs.getString("role")));
            }
        }
    }

    public void create(String username, String fullName, Role role, String password) throws SQLException {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String sql = "INSERT INTO users (username, full_name, role, salt, password_hash) VALUES (?,?,?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, role.name());
            ps.setString(4, salt);
            ps.setString(5, hash(password, salt));
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    static String hash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256);
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        }
    }
}
