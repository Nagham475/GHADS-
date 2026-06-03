package com.ghads.dao.impl;

import com.ghads.config.DatabaseConnection;
import com.ghads.dao.OrganizationDAO;
import com.ghads.model.Organization;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of OrganizationDAO.
 */
public class OrganizationDAOImpl implements OrganizationDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Organization org) {
        String sql = "INSERT INTO organizations (name, type, contact_info) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, org.getName());
            ps.setString(2, org.getType());
            ps.setString(3, org.getContactInfo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) org.setOrgId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting organization: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Organization org) {
        String sql = "UPDATE organizations SET name=?, type=?, contact_info=? WHERE org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, org.getName());
            ps.setString(2, org.getType());
            ps.setString(3, org.getContactInfo());
            ps.setInt(4, org.getOrgId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating organization: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int orgId) {
        String sql = "DELETE FROM organizations WHERE org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting organization: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Organization> findById(int orgId) {
        String sql = "SELECT * FROM organizations WHERE org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding organization: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Organization> findByName(String name) {
        String sql = "SELECT * FROM organizations WHERE name=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding organization by name: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Organization> findAll() {
        List<Organization> list = new ArrayList<>();
        String sql = "SELECT * FROM organizations ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching organizations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM organizations";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting organizations: " + e.getMessage(), e);
        }
        return 0;
    }

    // ── Row mapper ────────────────────────────────────────────────────────────

    private Organization mapRow(ResultSet rs) throws SQLException {
        return new Organization(
            rs.getInt("org_id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getString("contact_info")
        );
    }
}
