package ro.poli.uav.commander;
import ro.poli.uav.client.Target;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/18/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Observer {
    public void updateRoute(List<Waypoint> waypointList);
    public void updateTarget(Target target);
    public void setSpeedForWaypointMode(Double speedForWaypointMode);
    public void updateMode(FlightGearConstants.Mode mode);
}
