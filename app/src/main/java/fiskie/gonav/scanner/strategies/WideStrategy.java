package fiskie.gonav.scanner.strategies;

import android.location.Location;
import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.List;

import fiskie.gonav.scanner.Coordinates;
import fiskie.gonav.scanner.CoordinatesComparison;
import fiskie.gonav.scanner.Encounter;
import fiskie.gonav.scanner.EncounterCallback;
import fiskie.gonav.scanner.LocationProvider;
import fiskie.gonav.scanner.SpiralGenerator;

/**
 * WideStrategy scans a 5x5 grid around the player, in a spiral pattern beginning from the middle.
 */
public class WideStrategy implements IScanStrategy {

    private LocationProvider locationProvider;
    private PokemonGo pokemonGo;
    Location previousLocation;

    public WideStrategy(LocationProvider locationProvider, PokemonGo pokemonGo) {
        this.locationProvider = locationProvider;
        this.pokemonGo = pokemonGo;
    }

    /**
     * Strategy: look around the location the player is *going* to be, as opposed to
     * possibly out-of-date "current" location figures
     *
     * This function just does a simple extrapolation assuming
     * that the player's speed and direction is constant.
     *
     * It's not going to be very accurate -- gps bounce et al will screw it as well -- but
     * it should be better than getting scan results from just behind.
     *
     * @return Coordinates
     */
    public Coordinates getForwardLocation() {
        Location location = locationProvider.getLastLocation();
        Coordinates projection;

        if (previousLocation == null) {
            projection = new Coordinates(location);
        } else {
            Coordinates prev = new Coordinates(previousLocation);
            Coordinates curr = new Coordinates(location);
            Coordinates relative = new CoordinatesComparison(prev, curr).getRelativeCoordinates();
            projection = new Coordinates(relative.getLatitude() * 2 + curr.getLatitude(), relative.getLongitude() * 2 + curr.getLongitude());
        }

        previousLocation = location;

        return projection;
    }

    @Override
    public void doScan(final EncounterCallback callback) throws LoginFailedException, RemoteServerException, InterruptedException {
        Location lastLocation = locationProvider.getLastLocation();

        if (lastLocation == null) {
            Log.w("widestrategy", "Cannot scan - no location available.");
            return;
        }

        pokemonGo.setAltitude(lastLocation.getAltitude());

        // 70 metres is the radius of each catchable scan
        // Multiply by 2 for circumference and divide by 111111 for an accurate lat/long offset
        // todo: may need changing for one of the lat/long values?
        final double gap = 70 * 2 / 111111.;

        final List<int[]> offsets = new ArrayList<>();

        // does the coordinates generation in a spiral beginning from the middle.
        // have to put everything into a list because we can't do proper exception handling from a callback without changing the interface.
        int SCAN_RADIUS = 2;
        new SpiralGenerator().generate(SCAN_RADIUS, new SpiralGenerator.SpiralGeneratorCallback() {
            @Override
            public void yield(int x, int y) {
                offsets.add(new int[]{x, y});
            }
        });

        // Want to try and keep up with the player
        for (int[] offset : offsets) {
            Coordinates forward = getForwardLocation();
            double lat = forward.getLatitude() + gap * offset[1];
            double lon = forward.getLongitude() + gap * offset[0];

            scanAt(callback, lat, lon);
            int PING_RATE = 10000;
            Thread.sleep(PING_RATE);
        }
    }

    /**
     * @param latitude  Latitude
     * @param longitude Longitude
     * @throws LoginFailedException
     * @throws RemoteServerException
     */
    private void scanAt(EncounterCallback callback, double latitude, double longitude) throws LoginFailedException, RemoteServerException {
        pokemonGo.setLatitude(latitude);
        pokemonGo.setLongitude(longitude);

        Log.d("scanner", String.format("Scanning (%f, %f)", pokemonGo.getLatitude(), pokemonGo.getLongitude()));

        // track all wild pokemon in this area
        for (CatchablePokemon pkmn : pokemonGo.getMap().getCatchablePokemon()) {
            Log.d("scanner", "Found " + pkmn.toString() + " in scan");
            callback.onEncounterReceived(new Encounter(pkmn));
        }
    }
}
