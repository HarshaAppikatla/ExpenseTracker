package com.expenseflow.budget.entity;

import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@SQLDelete(sql = "UPDATE budgets SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "budget_year", nullable = false)
    private int budgetYear;

    @Column(name = "budget_month", nullable = false)
    private int budgetMonth;

    @Column(name = "monthly_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Builder.Default
    @Column(name = "alert_percentage")
    private int alertPercentage = 80;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
