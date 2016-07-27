package fiskie.gonav.scanner;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import POGOProtos.Map.Pokemon.WildPokemonOuterClass;
import fiskie.gonav.daemon.ICallback;

/**
 * Created by fiskie on 26/07/2016.
 */
public class Scanner {
    PokemonGo client;
    LocationManager locationManager;

    public Scanner(PokemonGo client, LocationManager locationManager) {
        this.client = client;
        this.locationManager = locationManager;
    }

    public void scan() {
        Log.i("gonavd", "Performing scan");

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        client.setLatitude(location.getLatitude());
        client.setLongitude(location.getLongitude());

        Log.d("gonavd", String.format("Client coordinates set for %f %f", client.getLatitude(), client.getLongitude()));

        try {
            Map map = client.getMap();

            Log.i("gonavd", "Nearby: " + map.getNearbyPokemon().size());

            for (WildPokemonOuterClass.WildPokemon pkmn : map.getMapObjects().getWildPokemons()) {
                Log.i("gonavd", String.format("Pokémon #%d at %f %f", pkmn.getPokemonData().getPokemonId().getNumber(), pkmn.getLatitude(), pkmn.getLongitude()));
            }
        } catch (LoginFailedException e) {
            Log.e("gonavd", "login failed ex"); // todo
        } catch (RemoteServerException e) {
            Log.e("gonavd", "remote server ex"); // todo
        }
    }
}
