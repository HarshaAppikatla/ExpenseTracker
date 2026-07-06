package com.expenseflow.repository;

import com.expenseflow.entity.SecurityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, String> {
}
