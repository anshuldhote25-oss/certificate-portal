package com.certportal.web;

import com.certportal.dao.CertificateDao;
import com.certportal.model.Status;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/** Summary dashboard: totals per status and the most recently updated records. */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final CertificateDao dao = new CertificateDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<Status, Integer> counts = dao.countByStatus();
            int total = 0;
            for (int n : counts.values()) {
                total += n;
            }
            req.setAttribute("counts", counts);
            req.setAttribute("total", total);
            req.setAttribute("recent", dao.recent(5));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
    }
}
