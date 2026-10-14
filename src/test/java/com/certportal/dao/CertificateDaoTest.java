package com.certportal.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.certportal.model.Certificate;
import com.certportal.model.Role;
import com.certportal.model.Status;
import com.certportal.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CertificateDaoTest {

    private final CertificateDao dao = new CertificateDao();

    @BeforeAll
    static void useInMemoryDatabase() throws Exception {
        System.setProperty("db.url", "jdbc:h2:mem:dao-test;DB_CLOSE_DELAY=-1");
        Db.createSchema();
    }

    private Certificate sample(String name, String course) {
        Certificate c = new Certificate();
        c.setRecipientName(name);
        c.setRecipientEmail("someone@example.com");
        c.setCourse(course);
        c.setIssueDate(LocalDate.now());
        return c;
    }

    @Test
    void createStoresADraftWithACertificateNumber() throws Exception {
        long id = dao.create(sample("Create Test", "DevOps"), "operator");
        Certificate saved = dao.findById(id);
        assertNotNull(saved);
        assertEquals(Status.DRAFT, saved.getStatus());
        assertTrue(saved.getCertNo().startsWith("CERT-" + LocalDate.now().getYear() + "-"));
        assertEquals("operator", saved.getCreatedBy());
    }

    @Test
    void searchIsCaseInsensitiveAndPartial() throws Exception {
        String unique = "Zed" + UUID.randomUUID().toString().substring(0, 8);
        dao.create(sample(unique + " Finch", "Docker"), "operator");
        List<Certificate> found = dao.search(unique.toLowerCase(), null);
        assertEquals(1, found.size());
        assertEquals(unique + " Finch", found.get(0).getRecipientName());
    }

    @Test
    void searchCanFilterByStatus() throws Exception {
        String unique = "Stat" + UUID.randomUUID().toString().substring(0, 8);
        long id = dao.create(sample(unique, "Jenkins"), "operator");
        dao.updateStatus(id, Status.SUBMITTED, "operator");
        assertEquals(1, dao.search(unique, Status.SUBMITTED).size());
        assertEquals(0, dao.search(unique, Status.DRAFT).size());
    }

    @Test
    void updateChangesFieldsAndKeepsStatus() throws Exception {
        long id = dao.create(sample("Before Name", "Git"), "operator");
        Certificate edit = dao.findById(id);
        edit.setRecipientName("After Name");
        dao.update(edit, "admin");
        Certificate after = dao.findById(id);
        assertEquals("After Name", after.getRecipientName());
        assertEquals("admin", after.getUpdatedBy());
        assertEquals(Status.DRAFT, after.getStatus());
    }

    @Test
    void countByStatusIncludesAllStatuses() throws Exception {
        dao.create(sample("Counter", "Ansible"), "operator");
        assertEquals(Status.values().length, dao.countByStatus().size());
        assertTrue(dao.countByStatus().get(Status.DRAFT) >= 1);
    }

    @Test
    void findByIdReturnsNullForUnknownRecord() throws Exception {
        assertNull(dao.findById(987654321L));
    }

    @Test
    void usersAuthenticateWithTheirPasswordOnly() throws Exception {
        UserDao users = new UserDao();
        String name = "user" + UUID.randomUUID().toString().substring(0, 8);
        users.create(name, "Test User", Role.APPROVER, "s3cret-pass");
        User ok = users.authenticate(name, "s3cret-pass");
        assertNotNull(ok);
        assertEquals(Role.APPROVER, ok.getRole());
        assertNull(users.authenticate(name, "wrong"));
        assertNull(users.authenticate("nobody", "s3cret-pass"));
    }
}
