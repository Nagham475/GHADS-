package com.ghads.dao;

import com.ghads.model.Family;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Family CRUD operations.
 */
public interface FamilyDAO {
    void           insert(Family family);
    void           update(Family family);
    void           delete(int familyId);
    Optional<Family> findById(int familyId);
    Optional<Family> findByNationalId(String nationalId);
    boolean        existsByNationalId(String nationalId);
    boolean        existsByNationalIdExcluding(String nationalId, int excludeId);
    List<Family>   findAll();
    List<Family>   findHighVulnerabilityFirst();
    List<Family>   findUnserved();
    List<Family>   findServed();
    int            countAll();
    int            countServed();
    int            countUnserved();
    void           updateLastAidDate(int familyId);
}
