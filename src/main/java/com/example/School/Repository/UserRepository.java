package com.example.School.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.School.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // List<User> getAllUsers();
}

// Method Signature
// Optional<User> findByFieldName(Type fieldValue);
// List<User> findAll();
// void deleteByFieldName(Type fieldValue);
// boolean existsByFieldName(Type fieldValue);
// List<User> findByRolesContaining(String role); // Find users by role substring
