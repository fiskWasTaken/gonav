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

    public LocationProvider(LocationManager locationManager, String provider) {
        this.locationManager = locationManager;
        this.lastLocation = locationManager.getLastKnownLocation(provider);
        this.provider = provider;
        this.updateRateNanos = 3000000000L;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void requestLocationUpdate() {
        this.requestLocationUpdate(provider);
    }

    public void requestLocationUpdate(final String provider) {
        this.performLocationRequest(provider, new LocationListener() {
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
        });
    }

    public long getUpdateRateNanos() {
        return updateRateNanos;
    }

    public void setUpdateRateNanos(long updateRateNanos) {
        this.updateRateNanos = updateRateNanos;
    }

    private void performLocationRequest(final String provider, final LocationListener listener) {
        long now = SystemClock.elapsedRealtimeNanos();
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        long lastUpdate = 0;

        if (lastKnownLocation != null)
            lastUpdate = lastKnownLocation.getElapsedRealtimeNanos();

        if (now < lastUpdate + getUpdateRateNanos()) {
            Log.d("locationprovider", "Not updating, still under the update rate threshold.");
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Looper.prepare();
                } catch (RuntimeException e) {
                    // there has to be a better way to do this.
                }

                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
                return null;
            }
        }.execute();
    }
}
