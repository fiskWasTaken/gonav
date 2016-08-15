package fiskie.gonav.scanner;

import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fiskie.gonav.scanner.strategies.IScanStrategy;
import fiskie.gonav.scanner.strategies.WideStrategy;

public class Scanner {
    private LocationProvider locationProvider;
    private IScanStrategy strategy;
    private Map<Long, Encounter> encounters;

    public Scanner(PokemonGo pogo, LocationProvider locationProvider) {
        PokemonGo pogo1 = pogo;
        this.locationProvider = locationProvider;
        this.strategy = new WideStrategy(locationProvider, pogo);
        encounters = new HashMap<>();
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public List<Encounter> getEncounters() {
        this.trim();
        return new ArrayList<>(encounters.values());
    }

    /**
     * Trim the encounter store to remove expired entries that do not need to be retained anymore
     */
    private void trim() {
        long now = System.currentTimeMillis();

        for (Iterator<Map.Entry<Long, Encounter>> it = encounters.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, Encounter> entry = it.next();

            if (entry.getValue().getExpirationTimestamp() <= now) {
                Log.i("scanner", String.format("Removing encounter %d as it has expired.", entry.getValue().getUid()));
                it.remove();
            }
        }
    }

    public void scan(final EncounterCallback uniqueEncounterCallback) throws InterruptedException {
        Log.d("scanner", "Performing scan");
        this.trim();

        try {
            locationProvider.requestLocationUpdate();
            strategy.doScan(new EncounterCallback() {
                @Override
                public void onEncounterReceived(Encounter encounter) {
                    if (!encounters.containsKey(encounter.getUid())) {
                        if (encounter.getExpirationTimestamp() < 0) {
                            Log.w("scanner", "Found a Pokémon with an expiration timestamp < 0, ignoring.");
                            return;
                        }

                        Log.i("scanner", String.format("Logging unseen Pokémon (encounter ID %d, #%d)", encounter.getUid(), encounter.getId()));
                        // Add the pkmn to the global encounter list and a list of new encounters
                        encounters.put(encounter.getUid(), encounter);
                        uniqueEncounterCallback.onEncounterReceived(encounter);
                    } else {
                        Log.d("scanner", String.format("Already saw Pokémon (encounter ID %d, #%d)", encounter.getUid(), encounter.getId()));
                    }
                }
            });
        } catch (LoginFailedException e) {
            Log.e("scanner", "login failed ex"); // todo
        } catch (RemoteServerException e) {
            Log.e("scanner", "remote server ex"); // todo
        }
    }
}
