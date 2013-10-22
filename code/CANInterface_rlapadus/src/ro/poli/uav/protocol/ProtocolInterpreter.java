package ro.poli.uav.protocol;

import ro.poli.uav.client.Target;
import ro.poli.uav.commander.FlightGearConstants;
import ro.poli.uav.commander.Observer;
import ro.poli.uav.commander.Subject;
import ro.poli.uav.commander.Waypoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/18/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolInterpreter implements Subject {
    private List<Waypoint> waypoints;
    private Observer observer;
    private Target target;
    private static Double SPEED_NOT_SET = -1.0;
    private Double speed;
    private ProtocolConstants currentMode;

    public ProtocolInterpreter() {
        speed = SPEED_NOT_SET;
    }

    /**
     * Interprets a command
     * @param command - command that will be interpreted
     */
    public void interpretCommand(String command) {
        String[] parameters = command.split(" ");
        String mode = parameters[0];

        if (ProtocolConstants.WAYPOINT_MODE.equalsName(mode)) {
            interpretCommandForWaypointMode(parameters);
        }

        if (ProtocolConstants.MANUAL_MODE.equalsName(mode)) {
            interpretCommandForManualMode(parameters);
        }
    }

    /**
     * Interprets a command for ManualMode
     * @param parameters - parameters used in building the command
     */
    private void interpretCommandForManualMode(String[] parameters) {
        this.currentMode = ProtocolConstants.MANUAL_MODE;
        String manualModeCommand = parameters[1];

        if (ProtocolConstants.TARGET.equalsName(manualModeCommand)) {
            target = makeTargetFromParams(parameters);
        }
        if (ProtocolConstants.SET_ALTITUDE.equalsName(manualModeCommand)) {
            setAltitudeToTarget(parameters);
        }
        if (ProtocolConstants.SET_SPEED.equalsName(manualModeCommand)) {
            setSpeedToTarget(parameters);
        }
        if (ProtocolConstants.SET_HEADING.equalsName(manualModeCommand)) {
            setHeadingToTarget(parameters);
        }

        notifyObservers();
    }

    /**
     * Interprets a command for WaypointMode
     * @param parameters - parameters used in building the command
     */
    private void interpretCommandForWaypointMode(String[] parameters) {
        this.currentMode = ProtocolConstants.WAYPOINT_MODE;
        String waypointModeCommand = parameters[1];

        if (ProtocolConstants.SET_ROUTE_SPEED.equalsName(waypointModeCommand)) {
            extractSpeedFromParams(parameters);
        }

        if (ProtocolConstants.BEGIN_ROUTE.equalsName(waypointModeCommand)) {
            makeNewListOfWaypoints();
        }

        if (ProtocolConstants.INSERT_WAYPOINT.equalsName(waypointModeCommand)) {
            Waypoint waypoint = makeWaypointFromParams(parameters);
            waypoints.add(waypoint);
        }

        if (ProtocolConstants.END_ROUTE.equalsName(waypointModeCommand)) {
            notifyObservers();
        }
    }

    private void makeNewListOfWaypoints() {
        waypoints = new ArrayList<Waypoint>();
    }

    /**
     * Makes a waypoint from params
     * @param params - params parsed by interpreter
     * @return waypoint instance
     */
    private Waypoint makeWaypointFromParams(String[] params) {
        Double longitude = Double.parseDouble(params[2]);
        Double latitude = Double.parseDouble(params[3]);
        Double altitude = Double.parseDouble(params[4]);
        int id = Integer.parseInt(params[5]);

        return new Waypoint(longitude, latitude, altitude, id);
    }

    /**
     * Makes a target from params
     * @param params - params parsed by interpreter
     * @return target instance
     */
    private Target makeTargetFromParams(String[] params) {
        Double targetAltitude = Double.parseDouble(params[2]);
        Double targetHeading = Double.parseDouble(params[3]);
        Double targetSpeed = Double.parseDouble(params[4]);

        Target target = new Target(targetAltitude, targetHeading, targetSpeed);
        System.out.println("Target " + target);
        return target;
    }

    /**
     * Sets speed to target
     * @param params - params parsed by interpreter
     */
    private void setSpeedToTarget(String[] params) {
        Double targetSpeed = Double.parseDouble(params[2]);
        target = new Target();
        target.setSpeed(targetSpeed);
    }

    /**
     * Sets altitude to target
     * @param params - params parsed by interpreter
     */
    private void setAltitudeToTarget(String[] params) {
        Double targetAltitude = Double.parseDouble(params[2]);
        target = new Target();
        target.setTargetAltitude(targetAltitude);
    }

    /**
     * Sets heading to target
     * @param params - params parsed by interpreter
     */
    private void setHeadingToTarget(String[] params) {
        Double targetHeading = Double.parseDouble(params[2]);
        target = new Target();
        target.setTargetHeading(targetHeading);
    }

    private void extractSpeedFromParams(String[] params) {
        speed = Double.parseDouble(params[2]);
    }

    /**
     * Tests if speed is set
     * @return true if speed is set false otherwise
     */
    private boolean isSpeedSet() {
        return !speed.equals(SPEED_NOT_SET);
    }


    @Override
    public void registerObserver(Observer observer) {
        this.observer = observer;
    }

    @Override
    public void notifyObservers() {
        System.out.println("Notify Observer");
        updateTarget();
        updateRoute();
    }

    /**
     * Updates the target if the mode is manual mode
     */
    private void updateTarget() {
        if (currentMode == ProtocolConstants.MANUAL_MODE) {
            observer.updateMode(FlightGearConstants.Mode.MANUAL_MODE);
            observer.updateTarget(target);
        }
    }

    /**
     * Updates the route if the mode is waypoint mode
     */
    private void updateRoute() {
        if (currentMode == ProtocolConstants.WAYPOINT_MODE) {
            observer.updateMode(FlightGearConstants.Mode.WAYPOINT_MODE);

            if (isSpeedSet()) {
                observer.setSpeedForWaypointMode(speed);
            }

            observer.updateRoute(waypoints);
        }
    }
}
