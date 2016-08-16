package fiskie.gonav.scanner;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * Controls the retrieval of a location
 */
public class LocationProvider {
    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private long updateRateNanos;
    private LocationListener defaultListener;

    private static LocationProvider instance;

    public static LocationProvider getInstance(LocationManager manager, String provider) {
        if (instance == null) {
            instance = new LocationProvider(manager, provider);
        }

        instance.provider = provider;
        return instance;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    private LocationProvider(LocationManager locationManager, String provider) {
        this.locationManager = locationManager;
        this.lastLocation = locationManager.getLastKnownLocation(provider);
        this.provider = provider;
        this.updateRateNanos = 3000000000L;

        this.defaultListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("locationprovider", "Location updated. " + location.toString());
                lastLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("locationprovider", "Status changed: " + s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("locationprovider", "Provider enabled: " + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("locationprovider", "Provider disabled: " + s);
            }
        };
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public long getUpdateRateNanos() {
        return updateRateNanos;
    }

    public void setUpdateRateNanos(long updateRateNanos) {
        this.updateRateNanos = updateRateNanos;
    }

    public void requestLocationUpdate() {
        long now = SystemClock.elapsedRealtimeNanos();
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        long lastUpdate = 0;

        if (lastKnownLocation != null)
            lastUpdate = lastKnownLocation.getElapsedRealtimeNanos();

        if (now < lastUpdate + getUpdateRateNanos()) {
            Log.d("locationprovider", "Not updating, still under the update rate threshold.");
            return;
        }

        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Looper.prepare();
                    } catch (RuntimeException e) {
                        // there has to be a better way to do this.
                    }

                    Log.d("locationprovider", "Requesting location update for " + provider);
                    locationManager.requestSingleUpdate(provider, defaultListener, Looper.getMainLooper());
                    return null;
                }
            }.execute();
        } catch (NullPointerException e) {
            Log.w("locationprovider", e.getMessage());
        }
    }
}
