package fiskie.gonav.scanner.strategies;

import android.location.Location;
import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.scanner.Encounter;
import fiskie.gonav.scanner.EncounterCallback;
import fiskie.gonav.scanner.LocationProvider;

/**
 * BasicStrategy emulates the game's scanning -- it only scans around your immediate area.
 */
public class BasicStrategy implements IScanStrategy {
    private LocationProvider locationProvider;
    private PokemonGo pokemonGo;

    public BasicStrategy(LocationProvider locationProvider, PokemonGo pokemonGo) {
        this.locationProvider = locationProvider;
        this.pokemonGo = pokemonGo;
    }

    @Override
    public void doScan(EncounterCallback callback) throws LoginFailedException, RemoteServerException, InterruptedException {
        Location location = locationProvider.getLastLocation();

        if (location != null) {
            pokemonGo.setLatitude(location.getLatitude());
            pokemonGo.setLongitude(location.getLongitude());
            pokemonGo.setAltitude(location.getAltitude());
            Log.d("scanner", String.format("Scanning (%f, %f)", pokemonGo.getLatitude(), pokemonGo.getLongitude()));

            // track all wild pokemon in this area
            for (CatchablePokemon pkmn : pokemonGo.getMap().getCatchablePokemon()) {
                callback.onEncounterReceived(new Encounter(pkmn));
            }
        } else {
            Log.w("scanner", "Cannot perform scan -- no location could be determined.");
        }
    }
}
