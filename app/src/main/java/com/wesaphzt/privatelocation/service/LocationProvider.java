package com.wesaphzt.privatelocation.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.Random;
import java.lang.Math;

public class LocationProvider {

    private String providerName;
    public Context context;
	private Location currentLocation;
	private Location jitteredLocation;

    public LocationProvider(String name, Context context) {
        this.providerName = name;
        this.context = context;
		this.currentLocation = new Location(name);
		this.jitteredLocation = new Location(name);
        jitteredLocation.setAltitude(3F);
        jitteredLocation.setTime(System.currentTimeMillis());
        jitteredLocation.setSpeed(0.01F);
        jitteredLocation.setBearing(1F);
        jitteredLocation.setAccuracy(3F);

        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        try
        {
            locationManager.addTestProvider(providerName, false, false, false, false, false,
                    true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(providerName, true);
        } catch (SecurityException e) {
            throw new SecurityException("Error applying mock location");
        }
    }

    void pushLocation(double lat, double lon) {
        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);

		currentLocation = new Location(providerName);
        currentLocation.setLatitude(lat);
        currentLocation.setLongitude(lon);
        currentLocation.setAltitude(3F);
        currentLocation.setTime(System.currentTimeMillis());
        currentLocation.setSpeed(0.01F);
        currentLocation.setBearing(1F);
        currentLocation.setAccuracy(3F);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentLocation.setBearingAccuracyDegrees(0.1F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentLocation.setVerticalAccuracyMeters(0.1F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentLocation.setSpeedAccuracyMetersPerSecond(0.01F);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            currentLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
		jitteredLocation = currentLocation;
        locationManager.setTestProviderLocation(providerName, currentLocation);
    }

	void jitterLocation(float deviation) {
        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
		Random rand = new Random();
		// Calculate the new position using polar coordinates
		float angle = rand.nextFloat() % 360f;
		float radius = rand.nextFloat() % deviation;
		float lat = (float) Math.sin(angle) * radius;
		float lon = (float) Math.cos(angle) * radius;
		jitteredLocation.setLatitude(lat);
		jitteredLocation.setLongitude(lon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            jitteredLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
		Log.v("LocationProvider", "Jittered location to " + lat + ":" + lon);
        locationManager.setTestProviderLocation(providerName, jitteredLocation);
	}

    public void shutdown() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeTestProvider(providerName);
    }
}
