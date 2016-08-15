package fiskie.gonav.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pokegoapi.api.PokemonGo;

import fiskie.gonav.AppSettings;
import fiskie.gonav.auth.PoGoClientContext;
import fiskie.gonav.scanner.Encounter;
import fiskie.gonav.scanner.EncounterCallback;
import fiskie.gonav.scanner.LocationProvider;
import fiskie.gonav.scanner.Scanner;

public class BackgroundService extends IntentService {
    private AppSettings settings;
    private LocationProvider locationProvider;
    private EServiceState serviceState;
    private Scanner scanner;
    private EncounterCallback listener;
    private ScanThread scanThread;
    private EncounterNotificationManager notificationManager;

    public BackgroundService() {
        super("GonavScannerDaemon");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.updateServiceState(EServiceState.UNPREPARED);
    }

    private void prepare() {
        PoGoClientContext context = new PoGoClientContext(settings);
        final IntentService self = this;

        new ClientManager(context).prepare(new ClientManager.ClientManagerCallback() {
            @Override
            public void onStatusChange(EServiceState serviceState, String overrideMessage) {
                Log.i("gonavd", "Client manager message: " + serviceState);
                updateServiceState(serviceState, overrideMessage);
            }

            @Override
            public void onSuccess(PokemonGo pokemonGo) {
                scanner = new Scanner(pokemonGo, getLocationProvider());
                startScanning();
            }

            @Override
            public void onFailure() {
                Log.e("gonavd", "Cannot start scanner handler without pokemon go client.");

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(self)
                                .setContentTitle("Failed to start scanner service")
                                .setContentText("Issue with PoGo API? Try to start service manually");

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int mId = 0;
                mNotificationManager.notify(mId, mBuilder.build());
            }
        });
    }

    @Override
    public void onCreate() {
        SharedPreferences preferences = getSharedPreferences("gonav", MODE_PRIVATE);

        settings = new AppSettings(preferences, getAssets());
        listener = new EncounterListener();
        serviceState = EServiceState.UNPREPARED;
        scanThread = new ScanThread();
        notificationManager = new EncounterNotificationManager(this, getLocationProvider(), settings.getPokedex(), settings.getPokemonFilters());
    }

    public LocationProvider getLocationProvider() {
        return LocationProvider.getInstance((LocationManager) getSystemService(Context.LOCATION_SERVICE), settings.getPreferredProvider());
    }

    private void startScanning() {
        notificationManager.start();
        scanner.setLocationProvider(getLocationProvider());

        // Dealing with Android <24 where illegal thread states seem to be a thing
        try {
            scanThread.interrupt();
            scanThread.start();
        } catch (IllegalThreadStateException e) {
            scanThread = new ScanThread();
            scanThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: Delegate command actions to a pattern so we're not including code for every command here...
        // null intent seems to mean that the process was asked to stop
        if (intent == null) {
            scanThread.interrupt();
            return STOP_FOREGROUND_REMOVE;
        }

        Log.i("gonavd", "Received intent " + intent.toString());
        ECommand command = (ECommand) intent.getSerializableExtra("command");

        if (command == ECommand.BROADCAST_CURRENT_ENCOUNTERS) {
            broadcastAllCurrentEncounters();
            return START_STICKY;
        }

        if (command == ECommand.SERVICE_STATUS) {
            broadcastServiceState();
            return START_STICKY;
        }

        if (command == ECommand.INVOKE_SCAN) {
            // todo
            this.prepare();

            try {
                performScan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return START_STICKY;
        }


        if (command == ECommand.SERVICE_START) {
            if (this.serviceState == EServiceState.UNPREPARED || scanner == null) {
                Log.i("gonavd", "start invoked, launching unprepared service");
                this.prepare();
            } else {
                Log.i("gonavd", "start invoked");
                updateServiceState(EServiceState.AVAILABLE);
                startScanning();
            }
        } else if (command == ECommand.SERVICE_STOP) {
            scanThread.interrupt();
            Log.i("gonavd", "pause invoked");
            updateServiceState(EServiceState.AVAILABLE);
        }

        return START_STICKY;
    }

    private void broadcastAllCurrentEncounters() {
        Log.d("gonavd", "Broadcasting current encounters");

        if (scanner != null) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

            for (Encounter encounter : scanner.getEncounters()) {
                Intent intent = new Intent(ReturnIntentType.POKEMON_BROADCAST);
                intent.putExtra("encounter", encounter);
                localBroadcastManager.sendBroadcast(intent);
            }
        }
    }

    private void updateServiceState(EServiceState serviceState) {
        this.serviceState = serviceState;
        this.broadcastServiceState();
    }

    private void updateServiceState(EServiceState serviceState, String overrideMessage) {
        this.serviceState = serviceState;
        this.broadcastServiceState(overrideMessage);
    }

    private void broadcastServiceState() {
        this.broadcastServiceState(null);
    }

    private void broadcastServiceState(String overrideMessage) {
        // todo also provide an enum
        Intent intent = new Intent(ReturnIntentType.SERVICE_STATE);
        intent.putExtra("state", this.serviceState);
        intent.putExtra("message", overrideMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("gonavd", "destroying handler");
        scanThread.interrupt();
        notificationManager.stop();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("gonavd", "unbind called");
        return super.onUnbind(intent);
    }

    private void performScan() throws InterruptedException {
        // todo: probably move the auto scanner state into a different enum, because it's now mixed in with network state and causing dumb cases like this one
        if (scanner != null && (serviceState == EServiceState.AVAILABLE || serviceState == EServiceState.ACTIVE)) {
            scanner.scan(listener);
        }
    }

    private class EncounterListener implements EncounterCallback {
        @Override
        public void onEncounterReceived(Encounter encounter) {
            notificationManager.register(encounter);
            broadcastAllCurrentEncounters();
        }
    }

    private class ScanThread extends Thread {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    updateServiceState(EServiceState.ACTIVE);
                    performScan();
                    Log.d("gonavd", "Scan thread is sleeping.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            updateServiceState(EServiceState.AVAILABLE);
        }
    }
}
