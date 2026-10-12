package com.certportal.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Requires a login for every page except the login form, static files and the health check. Also checks the CSRF token on POST. */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding("UTF-8");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (isPublic(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Object expected = session.getAttribute("csrf");
            String actual = request.getParameter("_csrf");
            if (expected == null || !expected.equals(actual)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isPublic(String path) {
        return path.equals("/login") || path.equals("/health") || path.startsWith("/css/");
    }
}
