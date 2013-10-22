package ro.poli.uav.commander;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/11/13
 * Time: 8:22 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Class that keeps data about an waypoint and command code
 */
public class WaypointCommand {
    Waypoint waypoint;
    Command commandCode;

    public enum Command {
        INSERT, DELETE, JUMPTO, ACTIVATE, CLEAR, DEACTIVATE
    }

    public WaypointCommand(Waypoint wayPoint, Command commandCode) {
        this.waypoint = wayPoint;
        this.commandCode = commandCode;
    }

    public WaypointCommand(Command commandCode) {
        this.commandCode = commandCode;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(Waypoint waypoint) {
        this.waypoint = waypoint;
    }

    public Command getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(Command commandCode) {
        this.commandCode = commandCode;
    }
}
