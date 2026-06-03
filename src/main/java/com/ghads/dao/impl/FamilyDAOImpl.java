package com.ghads.dao.impl;

import com.ghads.config.DatabaseConnection;
import com.ghads.dao.FamilyDAO;
import com.ghads.model.Family;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FamilyDAO.
 */
public class FamilyDAOImpl implements FamilyDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insert(Family f) {
        String sql = "INSERT INTO families (household_name, phone, location, family_size, " +
                     "national_id, vulnerability_level, registration_date) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getHouseholdName());
            ps.setString(2, f.getPhone());
            ps.setString(3, f.getLocation());
            ps.setInt(4, f.getFamilySize());
            ps.setString(5, f.getNationalId());
            ps.setString(6, f.getVulnerabilityLevel());
            ps.setDate(7, Date.valueOf(f.getRegistrationDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) f.setFamilyId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting family: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Family f) {
        String sql = "UPDATE families SET household_name=?, phone=?, location=?, family_size=?, " +
                     "national_id=?, vulnerability_level=?, registration_date=? WHERE family_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, f.getHouseholdName());
            ps.setString(2, f.getPhone());
            ps.setString(3, f.getLocation());
            ps.setInt(4, f.getFamilySize());
            ps.setString(5, f.getNationalId());
            ps.setString(6, f.getVulnerabilityLevel());
            ps.setDate(7, Date.valueOf(f.getRegistrationDate()));
            ps.setInt(8, f.getFamilyId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating family: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int familyId) {
        String sql = "DELETE FROM families WHERE family_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting family: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Family> findById(int familyId) {
        String sql = "SELECT * FROM families WHERE family_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding family: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Family> findByNationalId(String nationalId) {
        String sql = "SELECT * FROM families WHERE national_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, nationalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding family by national ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByNationalId(String nationalId) {
        String sql = "SELECT COUNT(*) FROM families WHERE national_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, nationalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking national ID: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByNationalIdExcluding(String nationalId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM families WHERE national_id=? AND family_id!=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, nationalId);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking national ID uniqueness: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Family> findAll() {
        List<Family> list = new ArrayList<>();
        String sql = "SELECT * FROM families ORDER BY household_name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching families: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Family> findHighVulnerabilityFirst() {
        List<Family> list = new ArrayList<>();
        // HIGH first, then MEDIUM, then LOW — using FIELD() ordering
        String sql = "SELECT * FROM families ORDER BY FIELD(vulnerability_level,'HIGH','MEDIUM','LOW'), household_name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching families by vulnerability: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Family> findUnserved() {
        List<Family> list = new ArrayList<>();
        String sql = "SELECT * FROM families WHERE last_aid_date IS NULL ORDER BY vulnerability_level, household_name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching unserved families: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Family> findServed() {
        List<Family> list = new ArrayList<>();
        String sql = "SELECT * FROM families WHERE last_aid_date IS NOT NULL ORDER BY household_name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching served families: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM families";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting families: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countServed() {
        String sql = "SELECT COUNT(*) FROM families WHERE last_aid_date IS NOT NULL";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting served families: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countUnserved() {
        String sql = "SELECT COUNT(*) FROM families WHERE last_aid_date IS NULL";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unserved families: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void updateLastAidDate(int familyId) {
        String sql = "UPDATE families SET last_aid_date = CURDATE() WHERE family_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, familyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last aid date: " + e.getMessage(), e);
        }
    }

    // ── Row mapper ────────────────────────────────────────────────────────────

    private Family mapRow(ResultSet rs) throws SQLException {
        Date lastAidSql = rs.getDate("last_aid_date");
        return new Family(
            rs.getInt("family_id"),
            rs.getString("household_name"),
            rs.getString("phone"),
            rs.getString("location"),
            rs.getInt("family_size"),
            rs.getString("national_id"),
            rs.getString("vulnerability_level"),
            rs.getDate("registration_date").toLocalDate(),
            lastAidSql != null ? lastAidSql.toLocalDate() : null
        );
    }
}
