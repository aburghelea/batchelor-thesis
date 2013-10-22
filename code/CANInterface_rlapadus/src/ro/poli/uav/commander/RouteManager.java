package ro.poli.uav.commander;

import ro.poli.uav.client.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 28.03.2013
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */

/**
 * Class used for sending a route to FlightGear's autopilot
 */
public class RouteManager {
    private List<Waypoint> currentRoute;
    private Double speed;
    private static Double DEFAULT_SPEED = 100.0;
    private List<WaypointCommand> commandQueue;

    public RouteManager() {
        this.speed = DEFAULT_SPEED;
        this.commandQueue = new ArrayList<WaypointCommand>();
    }

    /**
     * Creates a route based on the list of waypoints and sets the speed between waypoints
     * @param currentRoute - list of waypoints used in building the route
     * @param speed - speed between waypoints
     */
    public RouteManager(List<Waypoint> currentRoute, Double speed) {
        this.currentRoute = currentRoute;
        this.speed = speed;
        this.commandQueue = new ArrayList<WaypointCommand>();
        createRoute();
    }

    /**
     * Creates a route based on the list of waypoints and sets the speed between waypoints to default speed
     * @param currentRoute - list of waypoints used in building the route
     */
    public RouteManager(List<Waypoint> currentRoute) {
        this.currentRoute = currentRoute;
        this.speed = DEFAULT_SPEED;
        this.commandQueue = new ArrayList<WaypointCommand>();
        createRoute();
    }

    public void setNewRoute(List<Waypoint> route) {
        this.currentRoute = route;
        createRoute();
    }

    private Target buildDefaultTarget() {
        Target defaultTarget = new Target();
        defaultTarget.setSpeed(DEFAULT_SPEED);

        return defaultTarget;
    }

    /**
     * Iterates the waypoints and builds command for inserting each waypoint in FlightGear autopilot
     */
    private void createRoute() {
        clearCommand();

        ListIterator<Waypoint> currentRouteIterator = currentRoute.listIterator(currentRoute.size());

        while(currentRouteIterator.hasPrevious()) {
            WaypointCommand wayPointCommand = new WaypointCommand(currentRouteIterator.previous(),
                    WaypointCommand.Command.INSERT);
            addCommand(wayPointCommand);
        }

        activateCommand();
        jumpToCommand();
    }

    /**
     * Returns the next command that will be sent to FlightGear's autopilot
     * @return - command for autopilot
     */
    public String getCommand() {
        if (commandQueue.isEmpty()) {
            return "EMPTY_COMMAND";
        }

        WaypointCommand wayPointCommand = commandQueue.get(0);
        commandQueue.remove(0);

        String commandForRouteManager = CommandBuilder.buildCommandForFlightGearRouteManager(wayPointCommand);
        return CommandBuilder.buildCommandForWaypointMode(commandForRouteManager, speed);
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    private void clearCommand() {
        addCommand(WaypointCommand.Command.CLEAR);
    }

    private void activateCommand() {
        addCommand(WaypointCommand.Command.ACTIVATE);
    }

    private void jumpToCommand() {
        addCommand(WaypointCommand.Command.JUMPTO);
    }

    private void addCommand(WaypointCommand.Command commandCode) {
        WaypointCommand command = new WaypointCommand(commandCode);
        commandQueue.add(command);
    }

    private void addCommand(WaypointCommand command) {
        commandQueue.add(command);
    }

    public void clearRoute() {
        currentRoute.clear();
        commandQueue.clear();
    }
}
