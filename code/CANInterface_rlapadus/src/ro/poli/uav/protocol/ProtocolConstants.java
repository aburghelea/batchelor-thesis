package ro.poli.uav.protocol;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/18/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ProtocolConstants {
    /**
     * Constants used by Protocol Interpreter
     */
    MANUAL_MODE,
    TARGET,
    SET_ALTITUDE,
    SET_SPEED,
    SET_HEADING,

    WAYPOINT_MODE,
    SET_ROUTE_SPEED,
    SET_ROUTE_ALTITUDE,
    BEGIN_ROUTE,
    INSERT_WAYPOINT,
    END_ROUTE;

    public boolean equalsName(String otherName){
        return this.toString().equals(otherName);
    }
}
