package fiskie.gonav.scanner;

public class CoordinatesComparison {
    private Coordinates from;
    private Coordinates to;

    public CoordinatesComparison(Coordinates from, Coordinates to) {
        this.from = from;
        this.to = to;
    }

    public Coordinates getRelativeCoordinates() {
        return new Coordinates(to.getLatitude() - from.getLatitude(), to.getLongitude() - from.getLongitude());
    }

    private Coordinates getAbsoluteCoordinates() {
        Coordinates relative = this.getRelativeCoordinates();
        return new Coordinates(Math.abs(relative.getLatitude()), Math.abs(relative.getLongitude()));
    }

    public double getDistanceInMetres() {
        Coordinates absolute = this.getAbsoluteCoordinates();
        double lat = absolute.getLatitude();
        double lon = absolute.getLongitude();

        return Math.sqrt(lat * lat + lon * lon) * 111111;
    }

    public double getAngle() {
        double fLatR = Math.toRadians(from.getLatitude());
        double tLatR = Math.toRadians(to.getLatitude());
        double longDiff = Math.toRadians(to.getLongitude() - from.getLongitude());
        double y = Math.sin(longDiff) * Math.cos(tLatR);
        double x = Math.cos(fLatR) * Math.sin(tLatR) - Math.sin(fLatR) * Math.cos(tLatR) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public String getCompassPoint() {
        // 360 / number of points in compass = distance between each compass point.
        // dividing the angle by this value and rounding the result will get the closest point id.
        // Using from modulus solves an OOB issue with rotations higher than ~340 degrees.

        String[] points = ECompassPointStyle.EIGHT_POINT.getPoints();
        int closestPoint = (int) Math.round(this.getAngle() / (360. / points.length)) % points.length;
        return points[closestPoint];
    }
}
