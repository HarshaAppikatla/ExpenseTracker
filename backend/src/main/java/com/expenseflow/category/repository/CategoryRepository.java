package com.expenseflow.category.repository;

import com.expenseflow.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {

    @Query("SELECT c FROM CategoryEntity c WHERE c.systemCategory = true OR (c.user.id = :userId AND c.isDeleted = false)")
    List<CategoryEntity> findAllSystemAndUserCategories(@Param("userId") String userId);

    Optional<CategoryEntity> findByUserIdAndNameIgnoreCaseAndIsDeletedFalse(String userId, String name);

    Optional<CategoryEntity> findBySystemCategoryTrueAndNameIgnoreCase(String name);
}
