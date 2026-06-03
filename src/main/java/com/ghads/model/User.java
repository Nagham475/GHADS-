package com.ghads.model;

/**
 * Model class representing a system user (Admin or Coordinator).
 */
public class User {

    private int    userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;           // "ADMIN" or "COORDINATOR"
    private int    orgId;          // 0 for ADMIN
    private String photoPath;      // Bonus: profile photo path

    // Transient helper (populated by JOIN queries)
    private String organizationName;

    // ── Constructors ─────────────────────────────────────────────────────────

    public User() {}

    public User(String username, String password, String fullName,
                String email, String role, int orgId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email    = email;
        this.role     = role;
        this.orgId    = orgId;
    }

    public User(int userId, String username, String password, String fullName,
                String email, String role, int orgId, String photoPath) {
        this.userId    = userId;
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.email     = email;
        this.role      = role;
        this.orgId     = orgId;
        this.photoPath = photoPath;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getUserId()             { return userId; }
    public void   setUserId(int v)        { this.userId = v; }

    public String getUsername()           { return username; }
    public void   setUsername(String v)   { this.username = v; }

    public String getPassword()           { return password; }
    public void   setPassword(String v)   { this.password = v; }

    public String getFullName()           { return fullName; }
    public void   setFullName(String v)   { this.fullName = v; }

    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }

    public String getRole()               { return role; }
    public void   setRole(String v)       { this.role = v; }

    public int    getOrgId()              { return orgId; }
    public void   setOrgId(int v)         { this.orgId = v; }

    public String getPhotoPath()          { return photoPath; }
    public void   setPhotoPath(String v)  { this.photoPath = v; }

    public String getOrganizationName()           { return organizationName; }
    public void   setOrganizationName(String v)   { this.organizationName = v; }

    // ── Validation ───────────────────────────────────────────────────────────

    public boolean isValid() {
        return username != null && !username.isBlank() &&
               password != null && password.length() >= 8 &&
               fullName != null && !fullName.isBlank() &&
               email    != null && !email.isBlank() &&
               role     != null && (role.equals("ADMIN") || role.equals("COORDINATOR"));
    }

    public boolean isAdmin()       { return "ADMIN".equals(role); }
    public boolean isCoordinator() { return "COORDINATOR".equals(role); }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username +
               "', role='" + role + "', orgId=" + orgId + "}";
    }
}
