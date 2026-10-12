package com.certportal.web;

import com.certportal.dao.CertificateDao;
import com.certportal.model.Certificate;
import com.certportal.model.Status;
import com.certportal.model.User;
import com.certportal.service.Validator;
import com.certportal.service.Workflow;
import com.certportal.service.Workflow.Action;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Handles every certificate page:
 * <pre>
 * GET  /certificates                 list + search (q, status)
 * GET  /certificates/new             blank form
 * POST /certificates                 create
 * GET  /certificates/{id}            view
 * GET  /certificates/{id}/edit       edit form
 * POST /certificates/{id}            update
 * POST /certificates/{id}/{action}   submit | approve | reject | issue
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
            } else if (parts.length == 2 && isNumber(parts[0]) && parts[1].equals("edit")) {
                showEditForm(req, resp, Long.parseLong(parts[0]));
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
            } else if (parts.length == 1 && isNumber(parts[0])) {
                update(req, resp, Long.parseLong(parts[0]));
            } else if (parts.length == 2 && isNumber(parts[0])) {
                transition(req, resp, Long.parseLong(parts[0]), parts[1]);
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
        String q = trim(req.getParameter("q"));
        Status status = Status.fromText(req.getParameter("status"));
        req.setAttribute("certificates", dao.search(q, status));
        req.setAttribute("q", q);
        req.setAttribute("selectedStatus", status == null ? "" : status.name());
        req.setAttribute("statuses", Status.values());
        req.setAttribute("canCreate", Workflow.canCreate(user(req).getRole()));
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

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, long id)
            throws SQLException, ServletException, IOException {
        Certificate cert = dao.findById(id);
        if (cert == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }
        if (!Workflow.canEdit(user(req).getRole(), cert.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This record cannot be edited by your role or in its current status.");
            return;
        }
        req.setAttribute("certificate", cert);
        req.setAttribute("formAction", req.getContextPath() + "/certificates/" + id);
        req.setAttribute("formTitle", "Edit certificate " + cert.getCertNo());
        req.getRequestDispatcher(FORM).forward(req, resp);
    }

    private void showOne(HttpServletRequest req, HttpServletResponse resp, long id)
            throws SQLException, ServletException, IOException {
        Certificate cert = dao.findById(id);
        if (cert == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }
        User user = user(req);
        req.setAttribute("certificate", cert);
        req.setAttribute("actions", Workflow.availableActions(user.getRole(), cert.getStatus()));
        req.setAttribute("canEdit", Workflow.canEdit(user.getRole(), cert.getStatus()));
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
        Map<String, String> errors = validate(req);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("certificate", cert);
            req.setAttribute("formAction", req.getContextPath() + "/certificates");
            req.setAttribute("formTitle", "New certificate");
            req.getRequestDispatcher(FORM).forward(req, resp);
            return;
        }
        long id = dao.create(cert, user.getUsername());
        flash(req, "Certificate created as a draft.");
        resp.sendRedirect(req.getContextPath() + "/certificates/" + id);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp, long id)
            throws SQLException, ServletException, IOException {
        User user = user(req);
        Certificate existing = dao.findById(id);
        if (existing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }
        if (!Workflow.canEdit(user.getRole(), existing.getStatus())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This record cannot be edited by your role or in its current status.");
            return;
        }
        Certificate cert = readForm(req);
        cert.setId(id);
        cert.setCertNo(existing.getCertNo());
        Map<String, String> errors = validate(req);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("certificate", cert);
            req.setAttribute("formAction", req.getContextPath() + "/certificates/" + id);
            req.setAttribute("formTitle", "Edit certificate " + existing.getCertNo());
            req.getRequestDispatcher(FORM).forward(req, resp);
            return;
        }
        dao.update(cert, user.getUsername());
        flash(req, "Certificate updated.");
        resp.sendRedirect(req.getContextPath() + "/certificates/" + id);
    }

    private void transition(HttpServletRequest req, HttpServletResponse resp, long id, String actionPath)
            throws SQLException, IOException {
        Action action = Action.fromPath(actionPath);
        if (action == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action.");
            return;
        }
        User user = user(req);
        Certificate cert = dao.findById(id);
        if (cert == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Certificate not found.");
            return;
        }
        if (!Workflow.roleMayPerform(user.getRole(), action)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Your role is not allowed to " + action.getPath() + " certificates.");
            return;
        }
        Status next = Workflow.next(cert.getStatus(), action).orElse(null);
        if (next == null) {
            resp.sendError(HttpServletResponse.SC_CONFLICT,
                    "Cannot " + action.getPath() + " a certificate that is " + cert.getStatus().getLabel().toLowerCase() + ".");
            return;
        }
        dao.updateStatus(id, next, user.getUsername());
        flash(req, "Status changed to " + next.getLabel() + ".");
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

    private Map<String, String> validate(HttpServletRequest req) {
        return Validator.validate(req.getParameter("recipientName"), req.getParameter("recipientEmail"),
                req.getParameter("course"), req.getParameter("issueDate"));
    }

    private static User user(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }

    private static void flash(HttpServletRequest req, String message) {
        req.getSession().setAttribute("flash", message);
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
