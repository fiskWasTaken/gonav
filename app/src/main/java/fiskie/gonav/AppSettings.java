package fiskie.gonav;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import fiskie.gonav.pokedex.Builder;
import fiskie.gonav.pokedex.Pokedex;

/**
 * Created by fiskie on 27/07/2016.
 */
public class AppSettings {
    SharedPreferences preferences;
    Pokedex pokedex;

    public AppSettings(SharedPreferences preferences, AssetManager assets) {
        this.preferences = preferences;

        try {
            pokedex = new Builder(assets.open("pokedex.json")).getPokedex();
            Log.i("gonav", pokedex.getById(1).toString());
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

    public void enablePokemonById(int id) {
        Set<String> set = this.getEnabledPokemon();
        set.add(String.valueOf(id));
        this.preferences.edit().putStringSet("enabled_pokemon", set).apply();
    }

    public void disablePokemonById(int id) {
        Set<String> set = this.getEnabledPokemon();
        set.remove(id);
        this.preferences.edit().putStringSet("enabled_pokemon", set).apply();
    }

    public Set<String> getEnabledPokemon() {
        return this.preferences.getStringSet("enabled_pokemon", new HashSet<String>());
    }

    public String getRefreshToken() {
        return this.preferences.getString("refresh_token", null);
    }

    public void setRefreshToken(String token) {
        this.preferences.edit().putString("refresh_token", token).apply();
    }

    public Pokedex getPokedex() {
        return pokedex;
    }
}
