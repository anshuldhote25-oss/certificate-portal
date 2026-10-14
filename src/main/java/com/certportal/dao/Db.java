package com.certportal.dao;

import com.certportal.service.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens connections to the embedded H2 database and creates the schema. */
public final class Db {

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Db() {
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(Config.dbUrl(), "sa", "");
    }

    /** Creates tables if they do not exist yet. Safe to call on every start-up. */
    public static void createSchema() throws SQLException {
        try (Connection c = get(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "username VARCHAR(50) PRIMARY KEY,"
                    + "full_name VARCHAR(100) NOT NULL,"
                    + "role VARCHAR(20) NOT NULL,"
                    + "salt VARCHAR(100) NOT NULL,"
                    + "password_hash VARCHAR(200) NOT NULL)");
            s.execute("CREATE SEQUENCE IF NOT EXISTS cert_seq START WITH 1001");
            s.execute("CREATE TABLE IF NOT EXISTS certificates ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "cert_no VARCHAR(30) NOT NULL UNIQUE,"
                    + "recipient_name VARCHAR(100) NOT NULL,"
                    + "recipient_email VARCHAR(120) NOT NULL,"
                    + "course VARCHAR(120) NOT NULL,"
                    + "issue_date DATE NOT NULL,"
                    + "status VARCHAR(20) NOT NULL,"
                    + "remarks VARCHAR(255),"
                    + "created_by VARCHAR(50) NOT NULL,"
                    + "updated_by VARCHAR(50) NOT NULL,"
                    + "created_at TIMESTAMP NOT NULL,"
                    + "updated_at TIMESTAMP NOT NULL)");
        }
    }
}
