package com.certportal.web;

import com.certportal.dao.CertificateDao;
import com.certportal.model.Certificate;
import com.certportal.model.User;
import com.certportal.service.Validator;
import com.certportal.service.Workflow;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Certificate pages (feature 1: create, view, list).
 * <pre>
 * GET  /certificates            list
 * GET  /certificates/new        blank form
 * POST /certificates            create
 * GET  /certificates/{id}       view
 * </pre>
 */
@WebServlet("/certificates/*")
public class CertificateServlet extends HttpServlet {

    private static final String LIST = "/WEB-INF/jsp/list.jsp";
    private static final String FORM = "/WEB-INF/jsp/form.jsp";
    private static final String VIEW = "/WEB-INF/jsp/view.jsp";

    private final CertificateDao dao = new CertificateDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parts = pathParts(req);
        try {
            if (parts.length == 0) {
                showList(req, resp);
            } else if (parts.length == 1 && parts[0].equals("new")) {
                showNewForm(req, resp);
            } else if (parts.length == 1 && isNumber(parts[0])) {
                showOne(req, resp, Long.parseLong(parts[0]));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found.");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parts = pathParts(req);
        try {
            if (parts.length == 0) {
                create(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found.");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------- GET handlers ----------

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        req.setAttribute("certificates", dao.search(null, null));
        req.getRequestDispatcher(LIST).forward(req, resp);
    }

    private void showNewForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Workflow.canCreate(user(req).getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Your role cannot create certificates.");
            return;
        }
        req.setAttribute("certificate", new Certificate());
        req.setAttribute("formAction", req.getContextPath() + "/certificates");
        req.setAttribute("formTitle", "New certificate");
        req.getRequestDispatcher(FORM).forward(req, resp);
    }

    private void showOne(HttpServletRequest req, HttpServletResponse resp, long id)
            throws SQLException, ServletException, IOException {
        Certificate cert = dao.findById(id);
        if (cert == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }
        req.setAttribute("certificate", cert);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    // ---------- POST handlers ----------

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        User user = user(req);
        if (!Workflow.canCreate(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Your role cannot create certificates.");
            return;
        }
        Certificate cert = readForm(req);
        Map<String, String> errors = Validator.validate(req.getParameter("recipientName"),
                req.getParameter("recipientEmail"), req.getParameter("course"), req.getParameter("issueDate"));
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("certificate", cert);
            req.setAttribute("formAction", req.getContextPath() + "/certificates");
            req.setAttribute("formTitle", "New certificate");
            req.getRequestDispatcher(FORM).forward(req, resp);
            return;
        }
        long id = dao.create(cert, user.getUsername());
        req.getSession().setAttribute("flash", "Certificate created as a draft.");
        resp.sendRedirect(req.getContextPath() + "/certificates/" + id);
    }

    // ---------- helpers ----------

    private Certificate readForm(HttpServletRequest req) {
        Certificate cert = new Certificate();
        cert.setRecipientName(trim(req.getParameter("recipientName")));
        cert.setRecipientEmail(trim(req.getParameter("recipientEmail")));
        cert.setCourse(trim(req.getParameter("course")));
        cert.setIssueDate(Validator.parseDate(req.getParameter("issueDate")));
        cert.setRemarks(trim(req.getParameter("remarks")));
        return cert;
    }

    private static User user(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }

    private static String[] pathParts(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) {
            return new String[0];
        }
        return info.substring(1).split("/");
    }

    private static boolean isNumber(String s) {
        return s.matches("\\d{1,18}");
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
