package ro.poli.uav.commander;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 28.03.2013
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */

/**
 * Class that keeps data about a waypoint
 */
public class Waypoint {
    private double latitude;
    private double longitude;
    private double altitude;
    private int id;

    public Waypoint(double longitude, double latitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public Waypoint(double longitude, double latitude, double altitude, int id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Waypoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", id=" + id +
                '}';
    }
}