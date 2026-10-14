package com.certportal.web;

import com.certportal.dao.CertificateDao;
import com.certportal.dao.Db;
import com.certportal.dao.UserDao;
import com.certportal.model.Certificate;
import com.certportal.model.Role;
import com.certportal.model.Status;
import com.certportal.service.Config;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDate;

/** Creates the schema and demo data when the application starts. */
@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        ctx.setAttribute("appEnv", Config.appEnv());
        ctx.setAttribute("appVersion", Config.appVersion());
        try {
            Db.createSchema();
            seedUsers();
            seedCertificates();
            ctx.log("Certificate portal started: env=" + Config.appEnv() + ", version=" + Config.appVersion());
        } catch (Exception e) {
            throw new IllegalStateException("Database initialisation failed", e);
        }
    }

    /** Demo accounts for coursework. Change or remove them for any real use. */
    private void seedUsers() throws Exception {
        UserDao users = new UserDao();
        if (users.count() == 0) {
            users.create("operator", "Olivia Operator", Role.OPERATOR, "operator123");
            users.create("approver", "Arjun Approver", Role.APPROVER, "approver123");
            users.create("admin", "Asha Admin", Role.ADMIN, "admin123");
        }
    }

    private void seedCertificates() throws Exception {
        CertificateDao dao = new CertificateDao();
        if (!dao.search(null, null).isEmpty()) {
            return;
        }
        addSample(dao, "Priya Sharma", "priya.sharma@example.com", "Java Fundamentals", Status.ISSUED);
        addSample(dao, "Rahul Verma", "rahul.verma@example.com", "DevOps Essentials", Status.APPROVED);
        addSample(dao, "Neha Iyer", "neha.iyer@example.com", "Cloud Basics", Status.SUBMITTED);
        addSample(dao, "Karan Mehta", "karan.mehta@example.com", "Python for Data", Status.DRAFT);
    }

    private void addSample(CertificateDao dao, String name, String email, String course, Status status)
            throws Exception {
        Certificate c = new Certificate();
        c.setRecipientName(name);
        c.setRecipientEmail(email);
        c.setCourse(course);
        c.setIssueDate(LocalDate.now().minusDays(7));
        long id = dao.create(c, "admin");
        if (status != Status.DRAFT) {
            dao.updateStatus(id, status, "admin");
        }
    }
}
