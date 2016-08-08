package fiskie.gonav.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;
import fiskie.gonav.auth.PoGoClientContext;
import fiskie.gonav.auth.ToSAccepter;
import okhttp3.OkHttpClient;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void resetAuthenticationCredentials(View v) {
        AppSettings settings = (new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets()));
        settings.removeCredentials();
        startActivity(new Intent(this, FirstRunActivity.class));
    }

    public void acceptToS(final View view) {
        final Activity activity = this;
        view.setEnabled(false);

        new Thread() {
            public void run() {
                AppSettings settings = (new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets()));
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
}
