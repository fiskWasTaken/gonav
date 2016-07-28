package fiskie.gonav.scanner;

class DistanceFormatter {
    public String format(int metres) {
        if (metres >= 1000)
            return String.format("%dkm", metres / 1000);

        return String.format("%dm", metres);
    }
}
