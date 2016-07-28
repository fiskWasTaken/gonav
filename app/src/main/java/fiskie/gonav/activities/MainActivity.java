package fiskie.gonav.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;
import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.pokedex.PokedexEntry;
import fiskie.gonav.scanner.Coordinates;
import fiskie.gonav.scanner.CoordinatesComparison;
import fiskie.gonav.scanner.Encounter;
import fiskie.gonav.scanner.LocationProvider;
import fiskie.gonav.service.BackgroundService;
import fiskie.gonav.service.ECommand;
import fiskie.gonav.service.EServiceState;
import fiskie.gonav.service.ReturnIntentType;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private TextView serviceState;
    private AppSettings settings;
    private EServiceState currentState;
    private List<Encounter> encounterList;
    private Pokedex pokedex;
    private LocationProvider locationProvider;
    private EncounterAdapter encounterAdapter;
    private Handler handler;
    private Runnable updateEncounterList;
    private Runnable updateLocation;
    private RecyclerView recyclerView;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("gonav", "Resuming.");
        handler.post(updateEncounterList);
        handler.post(updateLocation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateEncounterList);
        handler.removeCallbacks(updateLocation);
    }

    /**
     * Trim the encounter store to remove expired entries that do not need to be retained anymore
     */
    private void trimEncounters() {
        long now = System.currentTimeMillis();
        List<Encounter> candidates = new ArrayList<>();

        for (Encounter encounter : encounterList) {
            if (encounter.getExpirationTimestamp() <= now) {
                candidates.add(encounter);
            }
        }

        encounterList.removeAll(candidates);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 1337);
        } catch (NoSuchMethodError e) {}

        settings = new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets());
        pokedex = settings.getPokedex();
        locationProvider = new LocationProvider((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        encounterList = new ArrayList<>();

        if (settings.getGoogleRefreshToken() == null && settings.getPTCCredentialsPair() == null)
            startActivity(new Intent(this, FirstRunActivity.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        serviceState = (TextView) findViewById(R.id.daemonStatus);

        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState == null || currentState != EServiceState.ACTIVE) {
                    sendServiceCommand(ECommand.SERVICE_START);
                } else {
                    sendServiceCommand(ECommand.SERVICE_STOP);
                }
            }
        });

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        manager.registerReceiver(new PokemonReceiver(), new IntentFilter(ReturnIntentType.POKEMON_BROADCAST));
        manager.registerReceiver(new ServiceStatusReceiver(), new IntentFilter(ReturnIntentType.SERVICE_STATE));

        sendServiceCommand(ECommand.SERVICE_STATUS);
        sendServiceCommand(ECommand.BROADCAST_CURRENT_ENCOUNTERS);

        encounterAdapter = new EncounterAdapter(encounterList);

        recyclerView = (RecyclerView) findViewById(R.id.encounterList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(encounterAdapter);

        handler = new Handler();

        updateEncounterList = new Runnable() {
            @Override
            public void run() {
                if (encounterList.size() > 0) {
                    trimEncounters();
                    encounterAdapter.notifyDataSetChanged();
                }

                handler.postDelayed(this, 1000);
            }
        };

        updateLocation = new Runnable() {
            @Override
            public void run() {
                if (encounterList.size() > 0) {
                    locationProvider.requestLocationUpdate();
                    Collections.sort(encounterList, new EncounterSort(locationProvider));
                }

                handler.postDelayed(this, locationProvider.getUpdateRateNanos() / 1000000);
            }
        };
    }

    private void sendServiceCommand(ECommand command) {
        Intent gonavd = new Intent(this, BackgroundService.class);
        gonavd.putExtra("command", command);
        startService(gonavd);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_filters:
                startActivity(new Intent(this, FiltersActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private int getResourceIdForState(EServiceState state) {
        if (state == EServiceState.ACTIVE) {
            return R.drawable.ic_stop_white_24dp;
        } else if (state == EServiceState.AVAILABLE || state == EServiceState.UNPREPARED) {
            return R.drawable.ic_play_arrow_white_24dp;
        } else if (state == EServiceState.CONNECTING) {
            return R.drawable.ic_cached_white_24dp;
        } else {
            return R.drawable.ic_error_white_24dp;
        }
    }

    public void openMapIntent(View v) {
        Encounter enc = (Encounter) v.getTag();
        PokedexEntry entry = pokedex.getById(enc.getId());

        Location here = locationProvider.getLastLocation();
        Coordinates a = new Coordinates(here.getLatitude(), here.getLongitude());
        Coordinates b = new Coordinates(enc.getLatitude(), enc.getLongitude());
        CoordinatesComparison comparison = new CoordinatesComparison(a, b);

        Log.i("coordsanity", String.format("%f %f: current location", a.getLatitude(), a.getLongitude()));
        Log.i("coordsanity", String.format("%f %f: pokemon", b.getLatitude(), b.getLongitude()));
        Log.i("coordsanity", String.format("%f: calculated angle", comparison.getAngle()));
        Log.i("coordsanity", String.format("%s: calculated compass point", comparison.getCompassPoint()));

        String urlPattern = "http://maps.google.com/maps?&z=%d&mrt=yp&t=m&q=%f+%f+(%s)";
        String url = String.format(urlPattern, 15, enc.getLatitude(), enc.getLongitude(), entry.getName());

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void invokeManualScan(View v) {
        Toast.makeText(this, "Running a manual scan...", Toast.LENGTH_SHORT).show();
        sendServiceCommand(ECommand.INVOKE_SCAN);
    }

    private class PokemonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Encounter encounter = (Encounter) intent.getSerializableExtra("encounter");

            if (!encounterList.contains(encounter)) {
                encounterList.add(encounter);
                encounterAdapter.notifyDataSetChanged();
            }
        }
    }

    private class ServiceStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final EServiceState state = (EServiceState) intent.getSerializableExtra("state");
            final String message = intent.getStringExtra("message");
            currentState = state;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String text = currentState.getDescriptor();

                    if (message != null)
                        text = message;

                    ((TextView) findViewById(R.id.daemonStatus)).setText(text);
                    fab.setImageResource(getResourceIdForState(state));

                    fab.setEnabled((state != EServiceState.CONNECTING));
                }
            });
        }
    }

    public class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.ViewHolder> {
        private List<Encounter> encounters;

        public EncounterAdapter(List<Encounter> encounterList) {
            this.encounters = encounterList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokemon_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Encounter encounter = this.encounters.get(position);

            Location here = locationProvider.getLastLocation();
            Coordinates a = new Coordinates(here.getLatitude(), here.getLongitude());
            Coordinates b = new Coordinates(encounter.getLatitude(), encounter.getLongitude());
            CoordinatesComparison comparison = new CoordinatesComparison(a, b);

            double metres = comparison.getDistanceInMetres();
            String compassStr = comparison.getCompassPoint();

            String time = new SimpleDateFormat("mm:ss").format(encounter.getExpirationTimestamp() - System.currentTimeMillis());
            String distStr = String.format("%.0fm %s", metres, compassStr);

            ((TextView) holder.layout.findViewById(R.id.pokemonName)).setText(pokedex.getById(encounter.getId()).getDexString());
            ((TextView) holder.layout.findViewById(R.id.pokemonLocation)).setText(distStr);
            ((TextView) holder.layout.findViewById(R.id.pokemonTime)).setText(time);

            holder.layout.setTag(encounter);
        }

        @Override
        public int getItemCount() {
            return this.encounters.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View layout;

            public ViewHolder(View layout) {
                super(layout);
                this.layout = layout;
            }
        }
    }

    public class EncounterSort implements Comparator<Encounter> {
        private LocationProvider locationProvider;

        public EncounterSort(LocationProvider locationProvider) {
            this.locationProvider = locationProvider;
        }

        @Override
        public int compare(Encounter a, Encounter b) {
            Location here = locationProvider.getLastLocation();
            Coordinates hereC = new Coordinates(here.getLatitude(), here.getLongitude());
            CoordinatesComparison aC = new CoordinatesComparison(hereC, new Coordinates(a.getLatitude(), a.getLongitude()));
            CoordinatesComparison bC = new CoordinatesComparison(hereC, new Coordinates(b.getLatitude(), b.getLongitude()));

            return aC.getDistanceInMetres() < bC.getDistanceInMetres() ? -1 : 1;
        }
    }
}
