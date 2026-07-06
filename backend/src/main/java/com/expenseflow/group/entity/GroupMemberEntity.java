package com.expenseflow.group.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
    @UniqueConstraint(name = "uq_group_members_group_user", columnNames = {"group_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private GroupRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private GroupMemberStatus status = GroupMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
