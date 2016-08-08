package fiskie.gonav.filters;

import java.util.Map;

import fiskie.gonav.pokedex.Pokedex;
import fiskie.gonav.pokedex.PokedexEntry;

public class PokemonFilters {
    private Map<Integer, PokemonFilter> filters;

    public PokemonFilters(Map<Integer, PokemonFilter> filters) {
        this.filters = filters;
    }

    public Map<Integer, PokemonFilter> getFilters() {
        return filters;
    }

    public void setFilters(Map<Integer, PokemonFilter> filters) {
        this.filters = filters;
    }

    public void repopulate(Pokedex pokedex) {
        for (PokedexEntry entry : pokedex.getList()) {
            if (!filters.containsKey(entry.getId())) {
                filters.put(entry.getId(), new PokemonFilter(entry.getId(), false));
            }
        }
    }

    /**
     * Should be used over getFilters().get().isEnabled()
     * as it handles the filters not being populated
     * @param id
     * @return pokemon is enabled
     */
    public boolean isEnabled(int id) {
        try {
            return this.filters.get(id).isEnabled();
        } catch (NullPointerException e) {
            return false;
        }
    }
}
