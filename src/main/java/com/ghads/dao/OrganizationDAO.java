package com.ghads.dao;

import com.ghads.model.Organization;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Organization CRUD operations.
 */
public interface OrganizationDAO {
    void             insert(Organization org);
    void             update(Organization org);
    void             delete(int orgId);
    Optional<Organization> findById(int orgId);
    Optional<Organization> findByName(String name);
    List<Organization>     findAll();
    int              count();
}
