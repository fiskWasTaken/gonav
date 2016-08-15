package fiskie.gonav.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.HashMap;
import java.util.Map;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;
import fiskie.gonav.auth.PoGoClientContext;
import fiskie.gonav.auth.ToSAccepter;
import okhttp3.OkHttpClient;

public class SettingsActivity extends AppCompatActivity {
    private AppSettings settings;
    private Map<Integer, String> locationRadioMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        settings = (new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets()));

        locationRadioMap = new HashMap<>();
        locationRadioMap.put(R.id.locationProviderGPS, LocationManager.GPS_PROVIDER);
        locationRadioMap.put(R.id.locationProviderNetwork, LocationManager.NETWORK_PROVIDER);
        locationRadioMap.put(R.id.locationProviderPassive, LocationManager.PASSIVE_PROVIDER);

        String preferredProvider = settings.getPreferredProvider();

        for (Integer key : locationRadioMap.keySet()) {
            if (locationRadioMap.get(key).equals(preferredProvider))
                ((RadioButton) findViewById(key)).setChecked(true);
        }
    }

    public void resetAuthenticationCredentials(View v) {
        settings.removeCredentials();
        startActivity(new Intent(this, FirstRunActivity.class));
    }

    public void acceptToS(final View view) {
        final Activity activity = this;
        view.setEnabled(false);

        new Thread() {
            public void run() {
                PoGoClientContext context = new PoGoClientContext(settings);
                OkHttpClient okHttpClient = new OkHttpClient();

                try {
                    PokemonGo pokemonGo = new PokemonGo(context.getCredentialProvider(), okHttpClient);
                    ToSAccepter accepter = new ToSAccepter(pokemonGo);
                    accepter.accept();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "ToS accepted.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final LoginFailedException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Auth error while accepting ToS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final RemoteServerException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Server error while accepting ToS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setEnabled(true);
                        }
                    });
                }
            }
        }.start();
    }

    public void onLocationProviderChanged(View view) {
        settings.setPreferredProvider(locationRadioMap.get(view.getId()));
    }

    @Override
    public void onStop() {
        super.onStop();
        Toast.makeText(this, "Scanner preferences will take effect when scanning is restarted.", Toast.LENGTH_SHORT).show();
    }
}
