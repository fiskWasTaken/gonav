package fiskie.gonav.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fiskie.gonav.R;

public class FirstRunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("First-time setup");
    }

    public void launchGoogleSSOIntent(View v) {
//        startActivity(new Intent(this, SetupGoogleActivity.class));
    }

    public void launchPTCSSOIntent(View v) {
        startActivity(new Intent(this, SetupPTCActivity.class));
    }
}
