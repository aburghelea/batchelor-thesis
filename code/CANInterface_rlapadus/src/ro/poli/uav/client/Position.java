package ro.poli.uav.client;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 22.03.2013
 * Time: 18:14
 * To change this template use File | Settings | File Templates.
 */
public interface Position {
    double getLatitude();
    void setLatitude(double  latitude);
    double getLongitude();
    void setLongitude(double longitude);
    double getAltitude();
    void setAltitude(double altitude);
}
