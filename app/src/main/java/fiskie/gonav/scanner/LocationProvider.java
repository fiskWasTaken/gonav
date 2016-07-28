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
 * Controls the getting of a location
 */
public class LocationProvider {
    private LocationManager locationManager;
    private Location lastLocation;
    private String provider = LocationManager.GPS_PROVIDER;

    public LocationProvider(LocationManager locationManager) {
        this.locationManager = locationManager;
        this.lastLocation = locationManager.getLastKnownLocation(provider);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void requestLocationUpdate() {
        this.doRequestLocation(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("locationprovider", "Location updated. " + location.toString());
                lastLocation = locationManager.getLastKnownLocation(provider);
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
        return 3000000000L;
    }

    private void doRequestLocation(final LocationListener listener) {
        long now = SystemClock.elapsedRealtimeNanos();
        long lastUpdate = 0;

        // todo: fall back to "best" location, GPS keeps failing to respond sometimes...

        if (lastLocation != null)
            lastUpdate = lastLocation.getElapsedRealtimeNanos();

        Log.d("locationprovider", "Now: " + now);
        Log.d("locationprovider", "Last update: " + lastUpdate);

        if (now > lastUpdate + getUpdateRateNanos()) {
            Log.d("locationprovider", "Requesting location update");

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
}
