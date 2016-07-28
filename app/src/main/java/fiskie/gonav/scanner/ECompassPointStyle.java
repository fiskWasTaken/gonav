package fiskie.gonav.scanner;

public enum ECompassPointStyle {
    QUARTER_POINT(new String[]{"N", "E", "S", "W"}),
    QUARTER_POINT_VERBOSE(new String[]{"North", "East", "South", "West"}),
    EIGHT_POINT(new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"}),
    EIGHT_POINT_VERBOSE(new String[]{"North", "North east", "East", "South east", "South", "South west", "West", "North West"}),
    SIXTEEN_POINT(new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SE", "WSW", "W", "WNW", "NW", "NNW"});

    private String[] points;

    ECompassPointStyle(String[] points) {
        this.points = points;
    }

    public String[] getPoints() {
        return points;
    }
}
