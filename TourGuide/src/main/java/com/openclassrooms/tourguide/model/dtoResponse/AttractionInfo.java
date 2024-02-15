package com.openclassrooms.tourguide.model.dtoResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttractionInfo {
    private String attractionName;
    private double latitude;
    private double longitude;
    private double distance;
    private int rewardPoints;

}