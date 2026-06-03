package com.ghads.dao;

import com.ghads.model.User;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for User CRUD operations.
 */
public interface UserDAO {
    void           insert(User user);
    void           update(User user);
    void           delete(int userId);
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    boolean        existsByUsername(String username);
    boolean        existsByEmail(String email);
    boolean        existsByUsernameExcluding(String username, int excludeId);
    boolean        existsByEmailExcluding(String email, int excludeId);
    List<User>     findAll();
    List<User>     findByOrgId(int orgId);
    Optional<User> authenticate(String username, String password);
    void           updatePassword(int userId, String newPassword);
    void           updatePhoto(int userId, String photoPath);
    int            countCoordinators();
}
