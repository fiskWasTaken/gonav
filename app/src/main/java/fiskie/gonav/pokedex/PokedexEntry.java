package fiskie.gonav.pokedex;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

public class PokedexEntry implements Serializable {
    private int id;
    private String name;
    private String region;
    private List<String> type;

    public static PokedexEntry missingno(int id) {
        PokedexEntry missingno = new PokedexEntry();
        missingno.setId(id);
        return missingno;
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
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

    public String getSubString() {
        return String.format("%s, %s", this.region, TextUtils.join("/", this.type));
    }

    public String getDexString() {
        return String.format("#%d %s", this.id, this.name);
    }
}
