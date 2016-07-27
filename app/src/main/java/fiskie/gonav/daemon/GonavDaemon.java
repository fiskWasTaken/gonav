package fiskie.gonav.daemon;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.os.Process;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAuthJson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleCredentialProvider;

import java.io.IOException;

import fiskie.gonav.AppSettings;
import fiskie.gonav.scanner.PoGoClientManager;
import fiskie.gonav.scanner.Scanner;

public class GonavDaemon extends Service implements GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener {
    LocationManager lm;
    AppSettings settings;
    Notification notification;

    private Looper mServiceLooper;
    private ScannerService mServiceHandler;

    public GonavDaemon() {
        notification = new Notification(0, "Daemon running", System.currentTimeMillis());
        notification.tickerText = "This notification keep the daemon running in the background.";

//        startForeground(8080, notification);
    }

    private class SetupClient extends AsyncTask<PoGoClientManager, Void, Void> {
        @Override
        protected Void doInBackground(PoGoClientManager... clientManager) {
            clientManager[0].getClient(new ICallback<PokemonGo>() {
                @Override
                public void callback(PokemonGo o) {
                    Scanner scanner = new Scanner(o, lm);

                    // start the service using the background handler
                    mServiceHandler = new ScannerService(mServiceLooper, scanner);
                }
            });

            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            // todo...
        }
    }

    @Override
    public void onCreate() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        settings = new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets());

        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread("GonavDaemon",
                Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        mServiceLooper = thread.getLooper();

        PoGoClientManager clientManager = new PoGoClientManager(mServiceLooper, this);

        if (settings.getRefreshToken() != null)
            clientManager.setRefreshToken(settings.getRefreshToken());

        new SetupClient().execute(clientManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i("gonavd", "null intent value received, doing nothing.");
            return 0;
        }

        String action = intent.getAction();

        if (mServiceHandler == null) {
            Log.e("gonavd", "Cannot start service handler if it is null...");
            // todo wait?
            return STOP_FOREGROUND_REMOVE;
        }

        Message message = mServiceHandler.obtainMessage();

        if (action.equals("stop")) {
            Log.i("gonavd", "stop invoked");
            message.arg1 = 0;
        } else if (action.equals("start")) {
            Log.i("gonavd", "start invoked");
            message.arg1 = 1;
        }

        mServiceHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onInitialOAuthComplete(GoogleAuthJson googleAuthJson) {
        Notification authNotification = new Notification(0, "GONav needs to authenticate with the Google", System.currentTimeMillis());
        Log.d("gonavd", "Auth callback received, need to do a thing.");
    }

    @Override
    public void onTokenIdReceived(GoogleAuthTokenJson googleAuthTokenJson) {
        Log.i("gonavd", "Saving refresh token");
        settings.setRefreshToken(googleAuthTokenJson.getRefreshToken());
    }
}
