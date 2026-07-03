package com.expenseflow.category.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(name = "uq_categories_user_name", columnNames = {"user_id", "name", "is_deleted"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "deletedFilter")
public class CategoryEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "icon", length = 50, nullable = false)
    private String icon;

    @Column(name = "color", length = 20, nullable = false)
    private String color;

    @Builder.Default
    @Column(name = "system_category", nullable = false)
    private boolean systemCategory = false;

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
