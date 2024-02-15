package com.openclassrooms.tourguide.model.dtoResponse;

import gpsUtil.location.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NearbyAttractionsResponse {
    private Location userLocation;
    private List<AttractionInfo> nearbyAttractions;

}
