package fiskie.gonav.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fiskie.gonav.R;
import fiskie.gonav.activities.MainActivity;
import fiskie.gonav.filters.PokemonFilters;
import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.scanner.Coordinates;
import fiskie.gonav.scanner.CoordinatesComparison;
import fiskie.gonav.scanner.Encounter;
import fiskie.gonav.scanner.LocationProvider;

class EncounterNotificationManager {
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    private Map<Integer, EncounterNotification> encounterNotifications;
    private Pokedex pokedex;
    private LocationProvider locationProvider;
    private PokemonFilters filters;
    private IntentService service;
    private NotificationManager mNotificationManager;
    private Thread thread;
    private NotificationRemovedListener removedListener;

    public EncounterNotificationManager(IntentService service, LocationProvider locationProvider, Pokedex pokedex, PokemonFilters filters) {
        this.encounterNotifications = new HashMap<>();
        this.locationProvider = locationProvider;
        this.pokedex = pokedex;
        this.filters = filters;
        this.service = service;

        mNotificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        thread = new ScanThread();

        service.registerReceiver(new NotificationRemovedListener(), new IntentFilter(NOTIFICATION_DELETED_ACTION));
    }

    private IntentService getService() {
        return service;
    }

    public void start() {
        try {
            thread.interrupt();
            thread.start();
        } catch (IllegalThreadStateException e) {
            thread = new ScanThread();
            thread.start();
        }
    }

    public void stop() {
        thread.interrupt();
    }

    public void register(Encounter encounter) {
        EncounterNotification notification = new EncounterNotification(encounter);
        this.encounterNotifications.put(notification.getNotificationId(), notification);
    }

    public int getNotificationId(Encounter encounter) {
        return (int) (encounter.getUid() % Integer.MAX_VALUE);
    }

    private void updateDisplay() {
        try {
            trim();

            if (encounterNotifications.size() > 0) {
                locationProvider.requestLocationUpdate();

                for (EncounterNotification encounter : encounterNotifications.values())
                    buildNotification(encounter);
            }
        } catch (ConcurrentModificationException ignored) {}
    }

    private void trim() {
        List<Integer> ids = new ArrayList<>();

        for (EncounterNotification encounterNotification : encounterNotifications.values()) {
            Encounter encounter = encounterNotification.getEncounter();
            long timeLeft = encounter.getExpirationTimestamp() - System.currentTimeMillis();

            if (timeLeft < 0) {
                mNotificationManager.cancel(encounterNotification.getNotificationId());
                ids.add(encounterNotification.getNotificationId());
            }
        }

        for (Integer id : ids)
            encounterNotifications.remove(id);
    }

    private LocationProvider getLocationProvider() {
        return locationProvider;
    }

    private void buildNotification(EncounterNotification encounterNotification) {
        Encounter encounter = encounterNotification.getEncounter();

        if (!filters.isEnabled(encounter.getId())) {
            // Not creating notification for filtered pokemon
            return;
        }

        encounterNotification.updateBuilder();
        NotificationCompat.Builder mBuilder = encounterNotification.getBuilder();

        mNotificationManager.notify(encounterNotification.getNotificationId(), mBuilder.build());
    }

    public class NotificationRemovedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer id = intent.getIntExtra("id", 0);
            Log.i("gonavd-notifications", "Notification removed: " + id);
            encounterNotifications.remove(id);
        }
    }

    public class EncounterNotification {
        private Encounter encounter;
        private NotificationCompat.Builder builder;
        private long timestamp;

        public EncounterNotification(Encounter encounter) {
            this.timestamp = System.currentTimeMillis();
            this.encounter = encounter;

            IntentService service = getService();

            Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
            deleteIntent.putExtra("id", getNotificationId());
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(service.getApplicationContext(), getNotificationId(), deleteIntent, 0);

            Intent resultIntent = new Intent(getService(), MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(service)
                    .addParentStack(MainActivity.class)
                    .addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            builder = new NotificationCompat.Builder(service)
                    .setSmallIcon(R.drawable.ic_pets_black_24dp)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setOnlyAlertOnce(true)
                    .setWhen(timestamp)
                    .setDeleteIntent(deletePendingIntent)
                    .setContentIntent(resultPendingIntent);

            updateBuilder();
        }

        public NotificationCompat.Builder getBuilder() {
            return builder;
        }

        public Encounter getEncounter() {
            return encounter;
        }

        public void updateBuilder() {
            builder.setContentTitle(getTitle())
                    .setContentText(getText())
                    .setWhen(timestamp);
        }

        public String getTitle() {
            return pokedex.getById(encounter.getId()).getName();
        }

        public String getText() {
            Location here = getLocationProvider().getLastLocation();
            Coordinates a = new Coordinates(here.getLatitude(), here.getLongitude());
            Coordinates b = new Coordinates(encounter.getLatitude(), encounter.getLongitude());
            CoordinatesComparison comparison = new CoordinatesComparison(a, b);

            String timeText = new SimpleDateFormat("mm:ss", Locale.US).format(encounter.getExpirationTimestamp() - System.currentTimeMillis());
            return String.format("%.0fm %s, %s remaining", comparison.getDistanceInMetres(), comparison.getCompassPoint(), timeText);
        }

        public int getNotificationId() {
            // This is a little hacky but it should suffice as a unique notif ID for encounters
            // Notifications can only have int32 ids...
            return (int) (encounter.getUid() % Integer.MAX_VALUE);
        }
    }

    private class ScanThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    updateDisplay();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
                Log.d("gonav-notifications", "Scanner thread interrupted");
            }
        }
    }
}
