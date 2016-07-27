package fiskie.gonav;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import fiskie.gonav.daemon.GonavDaemon;
import fiskie.gonav.pokedex.Builder;
import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.scanner.DistanceFormatter;

public class MainActivity extends AppCompatActivity {
    SeekBar distanceFilter;
    Button filtersButton;
    FloatingActionButton fab;

    AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MainActivity activity = this;

        settings = new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        distanceFilter = (SeekBar) findViewById(R.id.distanceFilter);
        filtersButton = (Button) findViewById(R.id.filtersButton);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        final Intent gonavd = new Intent(this, GonavDaemon.class);
        gonavd.setAction("start");

        startService(gonavd);

        distanceFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView filterValue = (TextView) findViewById(R.id.distanceFilterValue);
                settings.setFilterRange(i);
                filterValue.setText(new DistanceFormatter().format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        filtersButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, FiltersActivity.class);
                activity.startActivity(intent);
            }
        });

        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(gonavd);
            }
        });

        distanceFilter.setProgress(settings.getFilterRange());
        filtersButton.setText(String.format(getString(R.string.pokemon_filters_button), settings.getEnabledPokemon().size(), settings.getPokedex().getList().size()));
    }
}
