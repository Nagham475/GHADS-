package com.ghads.dao;

import com.ghads.model.AidDistribution;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for AidDistribution CRUD and duplicate-check operations.
 */
public interface AidDistributionDAO {
    void               insert(AidDistribution dist);
    void               delete(int distributionId);
    Optional<AidDistribution> findById(int id);
    List<AidDistribution>     findAll();
    List<AidDistribution>     findByOrgId(int orgId);
    List<AidDistribution>     findByFamilyId(int familyId);

    /**
     * Duplicate check — basic rule:
     * Returns the most recent distribution for this family in the last 30 days,
     * regardless of aid type (for LOW/MEDIUM families).
     */
    Optional<AidDistribution> findRecentDistribution(int familyId, int daysBack);

    /**
     * Duplicate check — bonus rule (aid-type specific):
     * Returns the most recent distribution for this family AND same aidType
     * within the last 30 days.
     */
    Optional<AidDistribution> findRecentDistributionByType(int familyId, String aidType, int daysBack);

    int countByOrgId(int orgId);

    /** Families served by a specific organization. */
    int countServedByOrg(int orgId);
}
