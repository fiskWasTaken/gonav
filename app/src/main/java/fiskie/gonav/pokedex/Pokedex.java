package fiskie.gonav.pokedex;

import java.util.List;

/**
 * Created by fiskie on 26/07/2016.
 */
public class Pokedex {
    private List<PokedexEntry> pokemon;

    public PokedexEntry getById(int id) {
        for (PokedexEntry pkmn : this.pokemon) {
            if (pkmn.getId() == id)
                return pkmn;
        }

        return PokedexEntry.missingno(id);
    }

    public PokedexEntry getByName(String name) {
        for (PokedexEntry pkmn : this.pokemon) {
            if (pkmn.getName().equals(name))
                return pkmn;
        }

        return PokedexEntry.missingno(-1);
    }

    public List<PokedexEntry> getList() {
        return pokemon;
    }
}
