package com.ghads.service;

import com.ghads.dao.AidDistributionDAO;
import com.ghads.dao.FamilyDAO;
import com.ghads.dao.impl.AidDistributionDAOImpl;
import com.ghads.dao.impl.FamilyDAOImpl;
import com.ghads.model.AidDistribution;
import com.ghads.model.Family;

import java.util.Optional;


public class AidDistributionService {

    private static final int DUPLICATE_WINDOW_DAYS = 30;

    private final AidDistributionDAO distDAO;
    private final FamilyDAO          familyDAO;

    public AidDistributionService() {
        this.distDAO   = new AidDistributionDAOImpl();
        this.familyDAO = new FamilyDAOImpl();
    }

    // ── Duplicate check result carrier 

    public static class DuplicateCheckResult {
        private final boolean allowed;
        private final AidDistribution conflictingRecord;   // non-null when rejected

        private DuplicateCheckResult(boolean allowed, AidDistribution conflict) {
            this.allowed           = allowed;
            this.conflictingRecord = conflict;
        }

        public static DuplicateCheckResult allow()                               { return new DuplicateCheckResult(true, null); }
        public static DuplicateCheckResult reject(AidDistribution conflict)      { return new DuplicateCheckResult(false, conflict); }

        public boolean isAllowed()                         { return allowed; }
        public AidDistribution getConflictingRecord()      { return conflictingRecord; }
    }

    // ── Main entry point 

    
    public DuplicateCheckResult saveDistribution(AidDistribution distribution) {
        // 1. Fetch family to get vulnerability level
        Family family = familyDAO.findById(distribution.getFamilyId())
            .orElseThrow(() -> new RuntimeException("Family not found: id=" + distribution.getFamilyId()));

        // 2. HIGH vulnerability → always allow
        if ("HIGH".equalsIgnoreCase(family.getVulnerabilityLevel())) {
            persist(distribution, family);
            return DuplicateCheckResult.allow();
        }

        // 3. MEDIUM or LOW → run duplicate check (BONUS: type-specific)
        Optional<AidDistribution> existing = distDAO.findRecentDistributionByType(
            distribution.getFamilyId(),
            distribution.getAidType(),
            DUPLICATE_WINDOW_DAYS
        );

        if (existing.isPresent()) {
            // Rejected — populate display fields for the alert
            AidDistribution conflict = existing.get();
            conflict.setFamilyName(family.getHouseholdName());
            conflict.setVulnerabilityLevel(family.getVulnerabilityLevel());
            return DuplicateCheckResult.reject(conflict);
        }

        // 4. No duplicate found → allow
        persist(distribution, family);
        return DuplicateCheckResult.allow();
    }

    // ── Private helpers 

    private void persist(AidDistribution distribution, Family family) {
        distDAO.insert(distribution);
        familyDAO.updateLastAidDate(family.getFamilyId());
    }

   
    public static String buildRejectionMessage(AidDistribution conflict) {
        return String.format(
            "⚠ Aid Distribution Rejected\n\n" +
            "Family Name:        %s\n" +
            "Vulnerability Level: %s\n" +
            "Aid Type:           %s\n" +
            "Organization:       %s\n" +
            "Last Aid Date:      %s\n\n" +
            "This family already received '%s' aid within the last 30 days.\n" +
            "Distribution has been cancelled.",
            conflict.getFamilyName(),
            conflict.getVulnerabilityLevel(),
            conflict.getAidType(),
            conflict.getOrganizationName(),
            conflict.getDistributionDate(),
            conflict.getAidType()
        );
    }
}
