package com.certportal.web;

import com.certportal.dao.Db;
import com.certportal.service.Config;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Health check used by Jenkins, Docker and Ansible: GET /health returns JSON and 200 when the database answers. */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbUp = false;
        try (Connection c = Db.get(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT 1")) {
            dbUp = rs.next();
        } catch (Exception e) {
            dbUp = false;
        }
        resp.setStatus(dbUp ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"status\":\"" + (dbUp ? "UP" : "DOWN") + "\","
                + "\"database\":\"" + (dbUp ? "UP" : "DOWN") + "\","
                + "\"env\":\"" + escape(Config.appEnv()) + "\","
                + "\"version\":\"" + escape(Config.appVersion()) + "\"}");
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
