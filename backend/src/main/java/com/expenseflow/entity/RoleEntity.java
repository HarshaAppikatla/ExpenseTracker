package com.expenseflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
