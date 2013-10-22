package ro.poli.uav.commander;

import ro.poli.uav.client.Target;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/11/13
 * Time: 7:41 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Builds command for manual mode and waypoint mode
 */
public class CommandBuilder {
    public static String SEPARATOR = "$";

    /**
     * Builds command for waypoint mode
     * @param waypointCommand - waypoint command that contains data about the waypoint and commad code
     * @return - command that will be interpreted by FlightGear's autopilot
     */
    public static String buildCommandForFlightGearRouteManager(WaypointCommand waypointCommand) {
        String command = "";
        Waypoint waypoint = waypointCommand.getWaypoint();
        WaypointCommand.Command commandCode = waypointCommand.getCommandCode();

        switch (commandCode) {
            case INSERT:
                command = "@INSERT:" + waypoint.getLongitude() + ","
                        + waypoint.getLatitude() + "@" + waypoint.getAltitude();
                break;
            case DELETE:
                command = "@DELETE" + waypoint.getId();
                break;
            case JUMPTO:
                command = "@JUMP" + 0;
                break;
            case ACTIVATE:
                command = "@ACTIVATE";
                break;
            case DEACTIVATE:
                command = "@DEACTIVATE";
                break;
            case CLEAR:
                command = "@CLEAR";
                break;
            default:
                break;
        }

        return command;
    }

    /**
     * Builds command for FlightGear when the mode selected is waypoint mode
     * @param routeManagerCommand - command send by route manager
     * @param speed - speed between waypoints
     * @return - command that will be interpreted by FlightGear
     */
    public static String buildCommandForWaypointMode(String routeManagerCommand, Double speed) {
        String command =
                        "" + SEPARATOR +
                        "" + SEPARATOR +
                        String.format(Locale.ENGLISH, "%03.6f", speed) + SEPARATOR +
                        FlightGearConstants.HeadingLock.TRUE_HEADING_HOLD + SEPARATOR +
                        FlightGearConstants.AltitudeLock.ALTITUDE_HOLD + SEPARATOR +
                        FlightGearConstants.SpeedLock.SPEED_WITH_THROTTLE + SEPARATOR +
                        "true" + SEPARATOR +
                        routeManagerCommand + SEPARATOR;

        return command;
    }

    /**
     * Builds command for FlightGear when the mode selected is manual mode
     * @param target - contains information about heading, altitude and speed
     * @return - command that will be interpreted by FlightGear
     */
    public static String buildCommandForManualControl(Target target) {
        String command = String.format(Locale.ENGLISH, "%03.6f", target.getTargetHeading()) + SEPARATOR +
                String.format(Locale.ENGLISH, "%03.6f", target.getTargetAltitude()) + SEPARATOR +
                String.format(Locale.ENGLISH, "%03.6f", target.getSpeed()) + SEPARATOR +
                target.getHeadingLock() + SEPARATOR +
                FlightGearConstants.AltitudeLock.ALTITUDE_HOLD + SEPARATOR +
                FlightGearConstants.SpeedLock.SPEED_WITH_THROTTLE + SEPARATOR +
                "false" + SEPARATOR +
                "" + SEPARATOR;

        return command;
    }
}
