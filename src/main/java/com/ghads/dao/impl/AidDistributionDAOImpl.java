package com.ghads.dao.impl;

import com.ghads.config.DatabaseConnection;
import com.ghads.dao.AidDistributionDAO;
import com.ghads.model.AidDistribution;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of AidDistributionDAO.
 * Includes the duplicate-check logic for 30-day window.
 */
public class AidDistributionDAOImpl implements AidDistributionDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insert(AidDistribution d) {
        String sql = "INSERT INTO aid_distributions (family_id, org_id, distributed_by, " +
                     "distribution_date, aid_type, notes) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getFamilyId());
            ps.setInt(2, d.getOrgId());
            ps.setInt(3, d.getDistributedBy());
            ps.setDate(4, Date.valueOf(d.getDistributionDate()));
            ps.setString(5, d.getAidType());
            ps.setString(6, d.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setDistributionId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting distribution: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int distributionId) {
        String sql = "DELETE FROM aid_distributions WHERE distribution_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, distributionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting distribution: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AidDistribution> findById(int id) {
        String sql = buildDetailSQL("ad.distribution_id=?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding distribution: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AidDistribution> findAll() {
        List<AidDistribution> list = new ArrayList<>();
        String sql = buildDetailSQL(null) + " ORDER BY ad.distribution_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching distributions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<AidDistribution> findByOrgId(int orgId) {
        List<AidDistribution> list = new ArrayList<>();
        String sql = buildDetailSQL("ad.org_id=?") + " ORDER BY ad.distribution_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching distributions by org: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<AidDistribution> findByFamilyId(int familyId) {
        List<AidDistribution> list = new ArrayList<>();
        String sql = buildDetailSQL("ad.family_id=?") + " ORDER BY ad.distribution_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching distributions by family: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Duplicate check (basic rule — any aid type):
     * Returns most recent distribution for this family within last N days.
     * Used for MEDIUM/LOW families to block any duplicate aid.
     */
    @Override
    public Optional<AidDistribution> findRecentDistribution(int familyId, int daysBack) {
        String sql = buildDetailSQL(
            "ad.family_id=? AND ad.distribution_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)"
        ) + " ORDER BY ad.distribution_date DESC LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            ps.setInt(2, daysBack);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in duplicate check: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Bonus duplicate check (aid-type specific):
     * Returns most recent distribution for this family AND same aid_type within last N days.
     */
    @Override
    public Optional<AidDistribution> findRecentDistributionByType(int familyId, String aidType, int daysBack) {
        String sql = buildDetailSQL(
            "ad.family_id=? AND ad.aid_type=? AND ad.distribution_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)"
        ) + " ORDER BY ad.distribution_date DESC LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            ps.setString(2, aidType);
            ps.setInt(3, daysBack);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in type-specific duplicate check: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public int countByOrgId(int orgId) {
        String sql = "SELECT COUNT(*) FROM aid_distributions WHERE org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting by org: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countServedByOrg(int orgId) {
        String sql = "SELECT COUNT(DISTINCT family_id) FROM aid_distributions WHERE org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting served by org: " + e.getMessage(), e);
        }
        return 0;
    }

    // ── SQL builder ───────────────────────────────────────────────────────────

    private String buildDetailSQL(String whereClause) {
        String sql = "SELECT ad.*, f.household_name AS family_name, f.national_id, " +
                     "f.vulnerability_level, o.name AS organization_name, " +
                     "u.full_name AS coordinator_name " +
                     "FROM aid_distributions ad " +
                     "JOIN families f ON ad.family_id = f.family_id " +
                     "JOIN organizations o ON ad.org_id = o.org_id " +
                     "JOIN users u ON ad.distributed_by = u.user_id";
        if (whereClause != null && !whereClause.isBlank()) {
            sql += " WHERE " + whereClause;
        }
        return sql;
    }

    // ── Row mapper ────────────────────────────────────────────────────────────

    private AidDistribution mapRow(ResultSet rs) throws SQLException {
        AidDistribution d = new AidDistribution(
            rs.getInt("distribution_id"),
            rs.getInt("family_id"),
            rs.getInt("org_id"),
            rs.getInt("distributed_by"),
            rs.getDate("distribution_date").toLocalDate(),
            rs.getString("aid_type"),
            rs.getString("notes")
        );
        try { d.setFamilyName(rs.getString("family_name")); }         catch (SQLException ignored) {}
        try { d.setNationalId(rs.getString("national_id")); }          catch (SQLException ignored) {}
        try { d.setVulnerabilityLevel(rs.getString("vulnerability_level")); } catch (SQLException ignored) {}
        try { d.setOrganizationName(rs.getString("organization_name")); }     catch (SQLException ignored) {}
        try { d.setCoordinatorName(rs.getString("coordinator_name")); }       catch (SQLException ignored) {}
        return d;
    }
}
