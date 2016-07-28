package fiskie.gonav.activities;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleAuthJson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;
import fiskie.gonav.auth.CredentialValidator;
import okhttp3.OkHttpClient;

public class SetupGoogleActivity extends AppCompatActivity implements GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener {
    private GoogleAuthJson googleAuthJson;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_google);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Authenticate with Google");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        final GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener parent = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    GoogleCredentialProvider provider = new GoogleCredentialProvider(new OkHttpClient(), parent);
                    (new CredentialValidator(provider)).test();
                } catch (RemoteServerException | LoginFailedException e) {
                    // todo handle error
                }

                return null;
            }
        }.execute();
    }

    @Override
    public void onInitialOAuthComplete(final GoogleAuthJson googleAuthJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((EditText) findViewById(R.id.authCode)).setText(googleAuthJson.getDeviceCode());
            }
        });

        this.googleAuthJson = googleAuthJson;

        Log.i("gonav", "user code " + googleAuthJson.getUserCode());
        Log.i("gonav", "ver url " + googleAuthJson.getVerificationUrl());
        Log.i("gonav", "exp in " + googleAuthJson.getExpiresIn());
        Log.i("gonav", "intval " + googleAuthJson.getInterval());
    }

    @Override
    public void onTokenIdReceived(GoogleAuthTokenJson googleAuthTokenJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
            }
        });

        new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets()).setGoogleRefreshToken(googleAuthTokenJson.getRefreshToken());

        OkHttpClient client = new OkHttpClient();

        try {
            CredentialProvider provider = new GoogleCredentialProvider(client, googleAuthTokenJson.getRefreshToken());
            (new CredentialValidator(provider)).test();
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    public void onVerify(View v) {

    }

    public void onCopy(View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Google auth code", ((EditText) findViewById(R.id.authCode)).getText().toString());
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
    }

    public void onOpenSignInModal(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(googleAuthJson.getVerificationUrl()));
        startActivity(intent);
    }

    public void launchPTCSSOIntent(View v) {
        startActivity(new Intent(this, SetupPTCActivity.class));
    }
}
