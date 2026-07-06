package com.expenseflow.repository;

import com.expenseflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    // findByEmail is sufficient — soft-deleted rows are already excluded by the
    // Hibernate 'deletedFilter' enabled globally by HibernateFilterAspect.
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
