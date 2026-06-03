package com.ghads.model;

/**
 * Model class representing a humanitarian organization.
 */
public class Organization {

    private int    orgId;
    private String name;
    private String type;
    private String contactInfo;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Organization() {}

    public Organization(String name, String type, String contactInfo) {
        this.name        = name;
        this.type        = type;
        this.contactInfo = contactInfo;
    }

    public Organization(int orgId, String name, String type, String contactInfo) {
        this.orgId       = orgId;
        this.name        = name;
        this.type        = type;
        this.contactInfo = contactInfo;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getOrgId()       { return orgId; }
    public void   setOrgId(int v)  { this.orgId = v; }

    public String getName()              { return name; }
    public void   setName(String v)      { this.name = v; }

    public String getType()              { return type; }
    public void   setType(String v)      { this.type = v; }

    public String getContactInfo()           { return contactInfo; }
    public void   setContactInfo(String v)   { this.contactInfo = v; }

    // ── Validation ───────────────────────────────────────────────────────────

    public boolean isValid() {
        return name        != null && !name.isBlank() &&
               type        != null && !type.isBlank() &&
               contactInfo != null && !contactInfo.isBlank();
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Organization{id=" + orgId + ", name='" + name + "', type='" + type + "'}";
    }
}
