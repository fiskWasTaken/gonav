package fiskie.gonav.scanner;

import com.pokegoapi.api.map.pokemon.CatchablePokemon;

import java.io.Serializable;

/**
 * Encounter appropriates relevant data from a CatchablePokemon
 * instance so it can be serialized and used in an Intent
 */
public class Encounter implements Serializable, IEncounter {
    protected long uid;
    protected int id;
    protected double latitude;
    protected double longitude;
    protected long expirationTimestamp;

    public Encounter(CatchablePokemon catchablePokemon) {
        this.uid = catchablePokemon.getEncounterId();
        this.id = catchablePokemon.getPokemonId().getNumber();
        this.latitude = catchablePokemon.getLatitude();
        this.longitude = catchablePokemon.getLongitude();
        this.expirationTimestamp = catchablePokemon.getExpirationTimestampMs();
    }

    public Encounter() {
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    @Override
    public String getSourceString() {
        return null;
    }

    public void setExpirationTimestamp(long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    @Override
    public String toString() {
        return String.format("#%d", this.id);
    }
}
