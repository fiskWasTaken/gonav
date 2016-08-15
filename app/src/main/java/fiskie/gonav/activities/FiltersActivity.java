package fiskie.gonav.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fiskie.gonav.AppSettings;
import fiskie.gonav.R;
import fiskie.gonav.filters.PokemonFilter;
import fiskie.gonav.filters.PokemonFilters;
import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.pokedex.PokedexEntry;

public class FiltersActivity extends AppCompatActivity {
    private AppSettings settings;
    private Pokedex pokedex;
    private FilterAdapter filterAdapter;
    private PokemonFilters filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        settings = new AppSettings(getSharedPreferences("gonav", MODE_PRIVATE), getAssets());
        pokedex = settings.getPokedex();

        filters = settings.getPokemonFilters();
        filters.repopulate(pokedex);

        List<PokemonFilter> filterList = new ArrayList<>(filters.getFilters().values());
        Collections.sort(filterList, new FilterSort());
        filterAdapter = new FilterAdapter(this, filterList);

        ListView listView = (ListView) findViewById(R.id.filtersContainer);
        listView.setAdapter(filterAdapter);

    }

    public void onSwitchToggle(View v) {
        boolean checked = ((Switch) v).isChecked();

        PokemonFilter filter = (PokemonFilter) v.getTag();
        filter.setEnabled(checked);
        filters.getFilters().put(filter.getId(), filter);
    }

    public void onToggleAll(View v) {
        for (PokemonFilter filter : filters.getFilters().values()) {
            filter.setEnabled(true);
        }
        filterAdapter.notifyDataSetChanged();
    }

    public void onToggleNone(View v) {
        for (PokemonFilter filter : filters.getFilters().values()) {
            filter.setEnabled(false);
        }
        filterAdapter.notifyDataSetChanged();
    }

    public void onToggleInvert(View v) {
        for (PokemonFilter filter : filters.getFilters().values()) {
            filter.setEnabled(!filter.isEnabled());
        }
        filterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        settings.setPokemonFilters(filters);
        Toast.makeText(this, "Alert changes will take effect when scanning is restarted.", Toast.LENGTH_SHORT).show();
    }

    public class FilterAdapter extends ArrayAdapter<PokemonFilter> {
        public FilterAdapter(Context context, List<PokemonFilter> entries) {
            super(context, 0, entries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            PokemonFilter filterable = getItem(position);
            PokedexEntry entry = pokedex.getById(filterable.getId());

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.filterable_pokemon_item, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.pokemonName)).setText(entry.getDexString());
            ((TextView) convertView.findViewById(R.id.pokemonType)).setText(entry.getSubString());
            ((Switch) convertView.findViewById(R.id.isEnabled)).setChecked(filterable.isEnabled());
            convertView.findViewById(R.id.isEnabled).setTag(filterable);

            // Return the completed view to render on screen
            return convertView;
        }
    }

    private class FilterSort implements Comparator<PokemonFilter> {
        @Override
        public int compare(PokemonFilter a, PokemonFilter b) {
            return a.getId() < b.getId() ? -1 : 1;
        }
    }
}
