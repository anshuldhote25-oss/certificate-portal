package com.certportal.dao;

import com.certportal.model.Certificate;
import com.certportal.model.Status;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Database access for certificate records. */
public class CertificateDao {

    private static final String COLUMNS = "id, cert_no, recipient_name, recipient_email, course, issue_date, "
            + "status, remarks, created_by, updated_by, created_at, updated_at";

    /** Inserts a new DRAFT record and returns its generated id. The certificate number is assigned here. */
    public long create(Certificate cert, String username) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        try (Connection c = Db.get()) {
            long seq;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT NEXT VALUE FOR cert_seq")) {
                rs.next();
                seq = rs.getLong(1);
            }
            String certNo = "CERT-" + now.getYear() + "-" + seq;
            String sql = "INSERT INTO certificates (cert_no, recipient_name, recipient_email, course, issue_date, "
                    + "status, remarks, created_by, updated_by, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, certNo);
                ps.setString(2, cert.getRecipientName());
                ps.setString(3, cert.getRecipientEmail());
                ps.setString(4, cert.getCourse());
                ps.setDate(5, Date.valueOf(cert.getIssueDate()));
                ps.setString(6, Status.DRAFT.name());
                ps.setString(7, cert.getRemarks());
                ps.setString(8, username);
                ps.setString(9, username);
                ps.setTimestamp(10, Timestamp.valueOf(now));
                ps.setTimestamp(11, Timestamp.valueOf(now));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    return keys.getLong(1);
                }
            }
        }
    }

    public Certificate findById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM certificates WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Searches by recipient name, certificate number or course (case-insensitive, partial match)
     * and optionally by status. Newest records first.
     */
    public List<Certificate> search(String query, Status status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM certificates WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            String like = "%" + query.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(recipient_name) LIKE ? OR LOWER(cert_no) LIKE ? OR LOWER(course) LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY id DESC");

        List<Certificate> result = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    /** Updates the editable fields of a record. */
    public void update(Certificate cert, String username) throws SQLException {
        String sql = "UPDATE certificates SET recipient_name = ?, recipient_email = ?, course = ?, issue_date = ?, "
                + "remarks = ?, updated_by = ?, updated_at = ? WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cert.getRecipientName());
            ps.setString(2, cert.getRecipientEmail());
            ps.setString(3, cert.getCourse());
            ps.setDate(4, Date.valueOf(cert.getIssueDate()));
            ps.setString(5, cert.getRemarks());
            ps.setString(6, username);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(8, cert.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(long id, Status status, String username) throws SQLException {
        String sql = "UPDATE certificates SET status = ?, updated_by = ?, updated_at = ? WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, username);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    /** Number of records in each status (all statuses present, zero when empty). */
    public Map<Status, Integer> countByStatus() throws SQLException {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            counts.put(s, 0);
        }
        String sql = "SELECT status, COUNT(*) FROM certificates GROUP BY status";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Status s = Status.fromText(rs.getString(1));
                if (s != null) {
                    counts.put(s, rs.getInt(2));
                }
            }
        }
        return counts;
    }

    public List<Certificate> recent(int limit) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM certificates ORDER BY updated_at DESC, id DESC LIMIT ?";
        List<Certificate> result = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    private static Certificate map(ResultSet rs) throws SQLException {
        Certificate c = new Certificate();
        c.setId(rs.getLong("id"));
        c.setCertNo(rs.getString("cert_no"));
        c.setRecipientName(rs.getString("recipient_name"));
        c.setRecipientEmail(rs.getString("recipient_email"));
        c.setCourse(rs.getString("course"));
        c.setIssueDate(rs.getDate("issue_date").toLocalDate());
        c.setStatus(Status.valueOf(rs.getString("status")));
        c.setRemarks(rs.getString("remarks"));
        c.setCreatedBy(rs.getString("created_by"));
        c.setUpdatedBy(rs.getString("updated_by"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}
