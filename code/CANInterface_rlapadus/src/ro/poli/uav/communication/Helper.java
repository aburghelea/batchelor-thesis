package ro.poli.uav.communication;

import ro.poli.uav.commander.Waypoint;
import ro.poli.uav.protocol.ProtocolInterpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/12/13
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Helper {
    private List<Double> speed;
    private List<Waypoint> wayPoints;
    private ProtocolInterpreter protocolInterpreter;

    public Helper(ProtocolInterpreter protocolInterpreter) {
        this.speed = new ArrayList<Double>();
        this.wayPoints = new ArrayList<Waypoint>();
        this.protocolInterpreter = protocolInterpreter;
    }

    public List<Double> getSpeed() {
        return speed;
    }

    public List<Waypoint> getWayPoints() {
        return wayPoints;
    }

    private String getPathToFile(String fileName) {
        File file = new File(".");
        String FILE_SEPARATOR = System.getProperty("file.separator");
        String pathToFile = "";
        try {
            pathToFile = file.getCanonicalPath() + FILE_SEPARATOR + "src" + FILE_SEPARATOR + fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pathToFile;
    }

    public void loadRouteFromFile(String filename) {
        String pathToFile = getPathToFile(filename);
        Scanner scanner;

        try {
            scanner = new Scanner(new File(pathToFile));

            while (scanner.hasNextLine()) {
                protocolInterpreter.interpretCommand(scanner.nextLine());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadRouteFromCode() {
        protocolInterpreter.interpretCommand("WAYPOINT_MODE BEGIN_ROUTE");
        protocolInterpreter.interpretCommand("WAYPOINT_MODE SET_ROUTE_SPEED 200");
        protocolInterpreter.interpretCommand(" WAYPOINT_MODE INSERT_WAYPOINT -113.8 40.81 800 1");
        protocolInterpreter.interpretCommand("WAYPOINT_MODE END_ROUTE");
    }
}
