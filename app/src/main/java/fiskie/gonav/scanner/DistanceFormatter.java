package fiskie.gonav.scanner;

/**
 * Created by fiskie on 27/07/2016.
 */
public class DistanceFormatter {
    public String format(int metres) {
        if (metres >= 1000)
            return String.format("%dkm", metres / 1000);

        return String.format("%dm", metres);
    }
}
