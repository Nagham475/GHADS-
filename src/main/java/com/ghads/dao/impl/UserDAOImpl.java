package com.ghads.dao.impl;

import com.ghads.config.DatabaseConnection;
import com.ghads.dao.UserDAO;
import com.ghads.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDAO.
 */
public class UserDAOImpl implements UserDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, role, org_id, photo_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            if (user.getOrgId() > 0) ps.setInt(6, user.getOrgId()); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, user.getPhotoPath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setUserId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username=?, full_name=?, email=?, role=?, org_id=?, photo_path=? " +
                     "WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            if (user.getOrgId() > 0) ps.setInt(5, user.getOrgId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, user.getPhotoPath());
            ps.setInt(7, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT u.*, o.name AS org_name FROM users u " +
                     "LEFT JOIN organizations o ON u.org_id = o.org_id WHERE u.user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.*, o.name AS org_name FROM users u " +
                     "LEFT JOIN organizations o ON u.org_id = o.org_id WHERE u.username=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByUsernameExcluding(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username=? AND user_id!=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username uniqueness: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmailExcluding(String email, int excludeId) {
        String sql = "SELECT COUNT(*) FROM users WHERE email=? AND user_id!=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email uniqueness: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, o.name AS org_name FROM users u " +
                     "LEFT JOIN organizations o ON u.org_id = o.org_id ORDER BY u.full_name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<User> findByOrgId(int orgId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, o.name AS org_name FROM users u " +
                     "LEFT JOIN organizations o ON u.org_id = o.org_id WHERE u.org_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users by org: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT u.*, o.name AS org_name FROM users u " +
                     "LEFT JOIN organizations o ON u.org_id = o.org_id " +
                     "WHERE u.username=? AND u.password=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error authenticating: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePhoto(int userId, String photoPath) {
        String sql = "UPDATE users SET photo_path=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, photoPath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating photo: " + e.getMessage(), e);
        }
    }

    @Override
    public int countCoordinators() {
        String sql = "SELECT COUNT(*) FROM users WHERE role='COORDINATOR'";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting coordinators: " + e.getMessage(), e);
        }
        return 0;
    }

    // ── Row mapper ────────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getObject("org_id") != null ? rs.getInt("org_id") : 0,
            rs.getString("photo_path")
        );
        try { user.setOrganizationName(rs.getString("org_name")); } catch (SQLException ignored) {}
        return user;
    }
}
