package com.ghads.model;

import java.time.LocalDate;

/**
 * Model class representing a displaced family registered in GHADS.
 */
public class Family {

    private int        familyId;
    private String     householdName;
    private String     phone;
    private String     location;
    private int        familySize;
    private String     nationalId;
    private String     vulnerabilityLevel;   // HIGH, MEDIUM, LOW
    private LocalDate  registrationDate;
    private LocalDate  lastAidDate;          // null if never served

    // ── Constructors ─────────────────────────────────────────────────────────

    public Family() {}

    public Family(String householdName, String phone, String location,
                  int familySize, String nationalId, String vulnerabilityLevel,
                  LocalDate registrationDate) {
        this.householdName      = householdName;
        this.phone              = phone;
        this.location           = location;
        this.familySize         = familySize;
        this.nationalId         = nationalId;
        this.vulnerabilityLevel = vulnerabilityLevel;
        this.registrationDate   = registrationDate;
    }

    public Family(int familyId, String householdName, String phone, String location,
                  int familySize, String nationalId, String vulnerabilityLevel,
                  LocalDate registrationDate, LocalDate lastAidDate) {
        this.familyId           = familyId;
        this.householdName      = householdName;
        this.phone              = phone;
        this.location           = location;
        this.familySize         = familySize;
        this.nationalId         = nationalId;
        this.vulnerabilityLevel = vulnerabilityLevel;
        this.registrationDate   = registrationDate;
        this.lastAidDate        = lastAidDate;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int       getFamilyId()                    { return familyId; }
    public void      setFamilyId(int v)               { this.familyId = v; }

    public String    getHouseholdName()               { return householdName; }
    public void      setHouseholdName(String v)       { this.householdName = v; }

    public String    getPhone()                       { return phone; }
    public void      setPhone(String v)               { this.phone = v; }

    public String    getLocation()                    { return location; }
    public void      setLocation(String v)            { this.location = v; }

    public int       getFamilySize()                  { return familySize; }
    public void      setFamilySize(int v)             { this.familySize = v; }

    public String    getNationalId()                  { return nationalId; }
    public void      setNationalId(String v)          { this.nationalId = v; }

    public String    getVulnerabilityLevel()          { return vulnerabilityLevel; }
    public void      setVulnerabilityLevel(String v)  { this.vulnerabilityLevel = v; }

    public LocalDate getRegistrationDate()            { return registrationDate; }
    public void      setRegistrationDate(LocalDate v) { this.registrationDate = v; }

    public LocalDate getLastAidDate()                 { return lastAidDate; }
    public void      setLastAidDate(LocalDate v)      { this.lastAidDate = v; }

    // ── Computed helpers ─────────────────────────────────────────────────────

    public boolean isServed() {
        return lastAidDate != null;
    }

    public boolean isHighVulnerability() {
        return "HIGH".equalsIgnoreCase(vulnerabilityLevel);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    public boolean isValid() {
        return householdName      != null && !householdName.isBlank() &&
               phone              != null && !phone.isBlank() &&
               location           != null && !location.isBlank() &&
               nationalId         != null && !nationalId.isBlank() &&
               vulnerabilityLevel != null &&
               (vulnerabilityLevel.equals("HIGH") ||
                vulnerabilityLevel.equals("MEDIUM") ||
                vulnerabilityLevel.equals("LOW")) &&
               familySize > 0 &&
               registrationDate != null;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Family{id=" + familyId + ", name='" + householdName +
               "', nationalId='" + nationalId + "', vulnerability='" + vulnerabilityLevel + "'}";
    }
}
