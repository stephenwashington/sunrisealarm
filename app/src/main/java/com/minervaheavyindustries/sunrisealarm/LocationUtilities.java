package com.minervaheavyindustries.sunrisealarm;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import java.util.HashMap;

public class LocationUtilities{

    private static Location betterLocation = null;
    private static Location lastKnownLocation = null;

    @TargetApi(23)
    public static Location getLocation(Context context){

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        HashMap gpsStatus = isLocationEnabled(lm);
        boolean gpsEnabled = (Boolean)gpsStatus.get("gps");
        boolean networkEnabled = (Boolean)gpsStatus.get("network");

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isBetterLocation(location, lastKnownLocation)){
                    Log.d("LocationUtilities", "Location changed!");
                    betterLocation = location;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Log.d("LocationUtilities", "GPS: " + gpsEnabled + " Network: " + networkEnabled);

        if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            try {
                if (gpsEnabled){
                    lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (networkEnabled){
                    lastKnownLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                betterLocation = lastKnownLocation;
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            } catch (Exception ex){
                ex.printStackTrace();
            } finally {
                lm.removeUpdates(locationListener);
            }
        }


/*{
            if (lm != null ) {
                if (gpsEnabled) {
                    lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null){
                        return lastKnownLocation;
                    } else {
                        Log.d("LocationUtilities", "GPS Location came back null!");
                        return null;
                    }

                }

                if (networkEnabled) {
                    lastKnownLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (lastKnownLocation != null){
                        return lastKnownLocation;
                    } else {
                        Log.d("LocationUtilities", "Network Location came back null!");
                        return null;
                    }
                }
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                lm.removeUpdates(locationListener);
            } else {
                Log.d("LocationUtilities", "LocationManager is null!");
                return null;
            }

        } else {
            Log.d("LocationUtilities", "Permissions have not been granted!");
            return null;
        }
        Log.d("LocationUtilities", "Made it to the bottom of getLocation?!");
        return null;*/
        return betterLocation;
    }

    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        int TWO_MINUTES = 1000 * 60 * 2;
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static HashMap isLocationEnabled(LocationManager lm){
        HashMap locStatus = new HashMap();
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        locStatus.put("gps", gpsEnabled);
        locStatus.put("network", networkEnabled);

        return locStatus;
    }

}
