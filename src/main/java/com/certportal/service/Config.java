package com.certportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration. Lookup order for a key such as {@code app.env}:
 * JVM property (-Dapp.env=...), environment variable (APP_ENV), then app.properties (filled in by Maven).
 */
public final class Config {

    private static final Properties FILE = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                FILE.load(in);
            }
        } catch (IOException e) {
            // fall back to defaults
        }
    }

    private Config() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (isBlank(value)) {
            value = FILE.getProperty(key);
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    /** Values like ${app.env} are unresolved Maven placeholders (running outside Maven). */
    private static boolean isBlank(String v) {
        return v == null || v.isBlank() || v.startsWith("${");
    }

    public static String appEnv() {
        return get("app.env", "dev");
    }

    public static String appVersion() {
        return get("app.version", "dev");
    }

    public static String dataDir() {
        return get("data.dir", "./data");
    }

    public static String dbUrl() {
        return get("db.url", "jdbc:h2:file:" + dataDir() + "/certdb");
    }
}
