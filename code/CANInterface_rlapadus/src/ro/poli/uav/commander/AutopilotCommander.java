package ro.poli.uav.commander;

import ro.poli.uav.client.Target;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 28.03.2013
 * Time: 12:26
 * To change this template use File | Settings | File Templates.
 */
public class AutopilotCommander implements Observer{
    private FlightGearConstants.Mode mode;
    private RouteManager routeManager;
    private Target target;

    /**
     * Sets current mode as manual mode and initialize routeManager
     * @param target - target instance used by manual mode
     */
    public AutopilotCommander(Target target) {
        this.mode = FlightGearConstants.Mode.MANUAL_MODE;
        this.target = new Target();
    }

    /**
     * Sets current mode as waypoint mode and initialize routeManager
     * @param routeManager - route manager instance used by waypoint mode
     */
    public AutopilotCommander(RouteManager routeManager) {
        this.mode = FlightGearConstants.Mode.WAYPOINT_MODE;
        this.routeManager = routeManager;
        this.target = new Target();
    }

    public FlightGearConstants.Mode getMode() {
        return mode;
    }

    public void setMode(FlightGearConstants.Mode mode) {
        this.mode = mode;
    }

    public RouteManager getRouteManager() {
        return routeManager;
    }

    public void setRouteManager(RouteManager routeManager) {
        this.routeManager = routeManager;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Returns next command based on the mode
     * @return - next command
     */
    public String getCommand() {
        String command = "";

        switch (mode) {
            case MANUAL_MODE:
                command = CommandBuilder.buildCommandForManualControl(target);
                break;
            case WAYPOINT_MODE:
                command = routeManager.getCommand();
                break;
        }

        return command;
    }

    /**
     * Updates the route
     * @param waypointList - list used by routeManager
     */
    @Override
    public void updateRoute(List<Waypoint> waypointList) {
        routeManager.setNewRoute(waypointList);
    }

    /**
     * Updates the target
     * @param receivedTarget - target used in manual mode
     */
    @Override
    public void updateTarget(Target receivedTarget) {
        target.updateTarget(receivedTarget);
    }

    /**
     * Sets speed for waypoint mode
     * @param speedForWaypointMode - speed used by routeManager
     */
    @Override
    public void setSpeedForWaypointMode(Double speedForWaypointMode) {
        routeManager.setSpeed(speedForWaypointMode);
    }

    /**
     * Updates the mode
     * @param mode - mode used by autopilot commander
     */
    @Override
    public void updateMode(FlightGearConstants.Mode mode) {
        this.mode = mode;
    }
}
