package fiskie.gonav;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.HashMap;

import fiskie.gonav.auth.PTCCredentialsPair;
import fiskie.gonav.filters.PokemonFilter;
import fiskie.gonav.filters.PokemonFilters;
import fiskie.gonav.pokedex.Builder;
import fiskie.gonav.pokedex.Pokedex;

public class AppSettings {
    private SharedPreferences preferences;
    private Pokedex pokedex;

    public AppSettings(SharedPreferences preferences, AssetManager assets) {
        this.preferences = preferences;

        try {
            pokedex = new Builder(assets.open("pokedex.json")).getPokedex();
        } catch (IOException e) {
            Log.e("gonav", "Pokédex failed to load: IOException: " + e.getMessage());
        }
    }

    public int getFilterRange() {
        return this.preferences.getInt("filter_range", 1000);
    }

    public void setFilterRange(int filterRange) {
        this.preferences.edit().putInt("filter_range", filterRange).apply();
    }

    public PokemonFilters getPokemonFilters() {
        try {
            String json = preferences.getString("filters", null);
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<PokemonFilters> filters = moshi.adapter(PokemonFilters.class);
            return filters.fromJson(json);
        } catch (Exception e) {
            // Either JSON is null or there is an IOException
            return new PokemonFilters(new HashMap<Integer, PokemonFilter>());
        }
    }

    public void setPokemonFilters(PokemonFilters pokemonFilters) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<PokemonFilters> jsonAdapter = moshi.adapter(PokemonFilters.class);

        String json = jsonAdapter.toJson(pokemonFilters);
        preferences.edit().putString("filters", json).apply();
    }

    public String getGoogleRefreshToken() {
        return this.preferences.getString("refresh_token", null);
    }

    public void setGoogleRefreshToken(String token) {
        this.preferences.edit().putString("refresh_token", token).apply();
    }

    public PTCCredentialsPair getPTCCredentialsPair() {
        String username = this.preferences.getString("ptc_username", null);
        String password = this.preferences.getString("ptc_password", null);

        if (username == null)
            return null;

        return new PTCCredentialsPair(username, password);
    }

    public void setPTCCredentialsPair(PTCCredentialsPair pair) {
        this.preferences.edit()
                .putString("ptc_username", pair.getUsername())
                .putString("ptc_password", pair.getPassword())
                .apply();
    }

    public Pokedex getPokedex() {
        return pokedex;
    }

    public void removeCredentials() {
        this.preferences.edit()
                .remove("ptc_username")
                .remove("ptc_password")
                .remove("refresh_token")
                .apply();
    }

    public String getPreferredProvider() {
        return this.preferences.getString("provider", LocationManager.GPS_PROVIDER);
    }

    public void setPreferredProvider(String provider) {
        this.preferences.edit()
                .putString("provider", provider)
                .apply();
    }
}
