package fiskie.gonav.scanner;

public interface IEncounter {
    double getLatitude();
    double getLongitude();
    long getUid();
    int getId();
    long getExpirationTimestamp();
    String getSourceString();
}
