package fiskie.gonav.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;

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
}
