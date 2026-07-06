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
public class Destination {

    @Column(name = "dest_city", nullable = false, length = 100)
    private String city;

    @Column(name = "dest_country", nullable = false, length = 100)
    private String country;

    @Column(name = "dest_display_name", nullable = false, length = 255)
    private String displayName;
}
