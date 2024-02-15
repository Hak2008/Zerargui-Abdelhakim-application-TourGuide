package com.openclassrooms.tourguide.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.openclassrooms.tourguide.model.dtoResponse.AttractionInfo;
import com.openclassrooms.tourguide.model.dtoResponse.NearbyAttractionsResponse;
import com.openclassrooms.tourguide.service.RewardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import rewardCentral.RewardCentral;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

    @Autowired
    RewardsService rewardsService;

    @Autowired
    RewardCentral rewardCentral;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @GetMapping("/getNearbyAttractions")
    public NearbyAttractionsResponse getNearbyAttractions(@RequestParam String userName) {
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
            List<Attraction> nearbyAttractions = tourGuideService.getNearByAttractions(visitedLocation);

            List<AttractionInfo> attractionInfos = nearbyAttractions.stream()
                    .map(attraction -> {
                        AttractionInfo attractionInfo = new AttractionInfo();
                        attractionInfo.setAttractionName(attraction.attractionName);
                        attractionInfo.setLatitude(attraction.latitude);
                        attractionInfo.setLongitude(attraction.longitude);
                        double distance = rewardsService.getDistance(visitedLocation.location, attraction);
                        attractionInfo.setDistance(distance);
                        int rewardPoints = rewardCentral.getAttractionRewardPoints(attraction.attractionId,getUser(userName).getUserId());
                        attractionInfo.setRewardPoints(rewardPoints);
                        return attractionInfo;
                    })
                    .collect(Collectors.toList());

            NearbyAttractionsResponse nearbyAttractionsResponse = new NearbyAttractionsResponse();
            nearbyAttractionsResponse.setUserLocation(visitedLocation.location);
            nearbyAttractionsResponse.setNearbyAttractions(attractionInfos);

            return nearbyAttractionsResponse;
    }

    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }

}