package fiskie.gonav;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import fiskie.gonav.pokedex.Builder;
import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.pokedex.PokedexEntry;

public class FiltersActivity extends AppCompatActivity {
    Pokedex pokedex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        pokedex = new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets()).getPokedex();

        for (PokedexEntry entry : pokedex.getList()) {
            Log.i("gonav", entry.toString());
        }
    }
}
