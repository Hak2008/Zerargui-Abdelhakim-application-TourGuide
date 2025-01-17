package com.openclassrooms.tourguide.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final ExecutorService executorService = Executors.newFixedThreadPool(150);
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		calculateRewardsAsync(user).join();
	}
	public CompletableFuture<Void> calculateRewardsAsync(User user) {
		CompletableFuture<Void> completableFuture = new CompletableFuture<>();
		executorService.submit(() -> {
			List<VisitedLocation> userLocations = user.getVisitedLocations();
			List<Attraction> attractions = gpsUtil.getAttractions();

			// Use an iterator to avoid concurrency issues
			Iterator<Attraction> attractionIterator = attractions.iterator();

			while (attractionIterator.hasNext()) {
				Attraction attraction = attractionIterator.next();

				// Check if the user does not already have a reward for this attraction
				boolean hasReward = user.getUserRewards().stream()
						.anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

				if (!hasReward) {
					for (VisitedLocation visitedLocation : userLocations) {
						if (nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));

							// Leave the loop to move on to the next attraction
							break;
						}
					}
				}
			}
			completableFuture.complete(null);
		});
		return completableFuture;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
