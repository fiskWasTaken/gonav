package fiskie.gonav.scanner;

public class PokeStopEncounter extends Encounter implements IEncounter {
    protected String pokestopName;

    public String getPokestopName() {
        return pokestopName;
    }

    public void setPokestopName(String pokestopName) {
        this.pokestopName = pokestopName;
    }

    @Override
    public String getSourceString() {
        return "Pokéstop: " + pokestopName;
    }
}
