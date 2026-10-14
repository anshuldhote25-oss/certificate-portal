package com.certportal.web;

import com.certportal.dao.UserDao;
import com.certportal.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/login.jsp";

    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user;
        try {
            user = users.authenticate(req.getParameter("username"), req.getParameter("password"));
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (user == null) {
            req.setAttribute("error", "Invalid username or password.");
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("csrf", UUID.randomUUID().toString());
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
