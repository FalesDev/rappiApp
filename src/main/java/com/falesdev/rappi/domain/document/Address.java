package com.falesdev.rappi.domain.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    @EqualsAndHashCode.Include
    private String addressLine;

    private String latitude;
    private String longitude;
    private String tag;
    private String buildingName; // Name of the building or condominium
    private String unitNumber; // Apartment, office or house number

    @Builder.Default
    @Field("isSelected")
    private boolean isSelected = false;
}
