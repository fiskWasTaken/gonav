package fiskie.gonav.filters;

public class PokemonFilter {
    private int id;
    private boolean enabled;

    public PokemonFilter(int id, boolean enabled) {
        this.id = id;
        this.enabled = enabled;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s", this.id, this.enabled ? "Enabled" : "Disabled");
    }
}
