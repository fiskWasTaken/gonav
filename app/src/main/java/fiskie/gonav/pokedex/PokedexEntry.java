package fiskie.gonav.pokedex;

import java.util.List;

/**
 * Created by fiskie on 26/07/2016.
 */
public class PokedexEntry {
    private int id;
    private String name;
    private String region;
    private List<String> type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "Missingno";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("[Pokémon: #%s %s]", this.getId(), this.getName());
    }

    public static PokedexEntry missingno(int id) {
        PokedexEntry missingno = new PokedexEntry();
        missingno.setId(id);
        return missingno;
    }
}
