package com.expenseflow.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSettings {
    @Builder.Default
    @Column(name = "allow_join_code", nullable = false)
    private boolean allowJoinByCode = true;

    @Builder.Default
    @Column(name = "allow_join_link", nullable = false)
    private boolean allowJoinByLink = true;

    @Builder.Default
    @Column(name = "archived", nullable = false)
    private boolean archived = false;
}
