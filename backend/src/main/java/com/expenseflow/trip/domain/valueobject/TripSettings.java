package com.expenseflow.trip.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSettings {

    @Builder.Default
    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility = "GROUP";

    @Builder.Default
    @Column(name = "allow_invites", nullable = false)
    private boolean allowInvites = true;

    @Builder.Default
    @Column(name = "archived", nullable = false)
    private boolean archived = false;
}
