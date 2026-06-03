package com.ghads.model;

import java.time.LocalDate;

/**
 * Model class representing a single aid distribution event.
 */
public class AidDistribution {

    private int       distributionId;
    private int       familyId;
    private int       orgId;
    private int       distributedBy;    // user_id of coordinator
    private LocalDate distributionDate;
    private String    aidType;          // Bonus field
    private String    notes;

    // Transient fields populated by JOIN queries (for display)
    private String familyName;
    private String nationalId;
    private String vulnerabilityLevel;
    private String organizationName;
    private String coordinatorName;

    // ── Constructors ─────────────────────────────────────────────────────────

    public AidDistribution() {}

    public AidDistribution(int familyId, int orgId, int distributedBy,
                           LocalDate distributionDate, String aidType, String notes) {
        this.familyId         = familyId;
        this.orgId            = orgId;
        this.distributedBy    = distributedBy;
        this.distributionDate = distributionDate;
        this.aidType          = aidType;
        this.notes            = notes;
    }

    public AidDistribution(int distributionId, int familyId, int orgId,
                           int distributedBy, LocalDate distributionDate,
                           String aidType, String notes) {
        this.distributionId   = distributionId;
        this.familyId         = familyId;
        this.orgId            = orgId;
        this.distributedBy    = distributedBy;
        this.distributionDate = distributionDate;
        this.aidType          = aidType;
        this.notes            = notes;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int       getDistributionId()              { return distributionId; }
    public void      setDistributionId(int v)         { this.distributionId = v; }

    public int       getFamilyId()                    { return familyId; }
    public void      setFamilyId(int v)               { this.familyId = v; }

    public int       getOrgId()                       { return orgId; }
    public void      setOrgId(int v)                  { this.orgId = v; }

    public int       getDistributedBy()               { return distributedBy; }
    public void      setDistributedBy(int v)          { this.distributedBy = v; }

    public LocalDate getDistributionDate()            { return distributionDate; }
    public void      setDistributionDate(LocalDate v) { this.distributionDate = v; }

    public String    getAidType()                     { return aidType; }
    public void      setAidType(String v)             { this.aidType = v; }

    public String    getNotes()                       { return notes; }
    public void      setNotes(String v)               { this.notes = v; }

    // Transient display getters/setters
    public String getFamilyName()                       { return familyName; }
    public void   setFamilyName(String v)               { this.familyName = v; }

    public String getNationalId()                       { return nationalId; }
    public void   setNationalId(String v)               { this.nationalId = v; }

    public String getVulnerabilityLevel()               { return vulnerabilityLevel; }
    public void   setVulnerabilityLevel(String v)       { this.vulnerabilityLevel = v; }

    public String getOrganizationName()                 { return organizationName; }
    public void   setOrganizationName(String v)         { this.organizationName = v; }

    public String getCoordinatorName()                  { return coordinatorName; }
    public void   setCoordinatorName(String v)          { this.coordinatorName = v; }

    // ── Validation ───────────────────────────────────────────────────────────

    public boolean isValid() {
        return familyId > 0 &&
               orgId > 0 &&
               distributedBy > 0 &&
               distributionDate != null &&
               aidType != null && !aidType.isBlank();
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "AidDistribution{id=" + distributionId +
               ", familyId=" + familyId +
               ", orgId=" + orgId +
               ", date=" + distributionDate +
               ", type='" + aidType + "'}";
    }
}
