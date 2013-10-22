package ro.poli.uav.communication;

import ro.poli.uav.client.UAV;
import ro.poli.uav.commander.AutopilotCommander;
import ro.poli.uav.commander.RouteManager;
import ro.poli.uav.protocol.ProtocolInterpreter;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/11/13
 * Time: 6:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private UAV uav;
    private AutopilotCommander autopilotCommander;
    private ProtocolInterpreter protocolInterpreter;
    private RaspberryPiIOManager raspberryPiIOManager;

    private Thread threadForFlightGear;
    private Thread threadForRaspberryPi;
    private Thread threadForTelemetryManager;

    public Main(UAV uav, AutopilotCommander autopilotCommander, ProtocolInterpreter protocolInterpreter,
                Integer inputPort, Integer outputPort, Integer rPiPort,
                Integer mavNumber) throws RouterException {
        this.uav = uav;
        this.autopilotCommander = autopilotCommander;
        this.protocolInterpreter = protocolInterpreter;
        connectToFlightGear(inputPort, outputPort);
        connectToRaspberryPi(rPiPort);
        connectToTelemetryRouter(mavNumber);
    }

    public Main(UAV uav, AutopilotCommander autopilotCommander, ProtocolInterpreter protocolInterpreter) {
        this.uav = uav;
        this.autopilotCommander = autopilotCommander;
        this.protocolInterpreter = protocolInterpreter;
        connectToFlightGear();
        connectToRaspberryPi();
    }

    /**
     * Creates a new thread for communication with FlightGear
     */
    private void connectToFlightGear() {
        FlightGearIOManager flightGearIOManager = new FlightGearIOManager(uav, autopilotCommander);
        threadForFlightGear = new Thread(flightGearIOManager);
    }

    /**
     * Creates a new thread for communication with FlightGear
     */
    private void connectToFlightGear(Integer inputPort, Integer outputPort) {
        FlightGearIOManager flightGearIOManager = new FlightGearIOManager(inputPort, outputPort, uav, autopilotCommander);
        threadForFlightGear = new Thread(flightGearIOManager);
    }

    /**
     * Creates a new thread for communication with RaspberryPi
     */
    private void connectToRaspberryPi() {
        raspberryPiIOManager = new RaspberryPiIOManager(uav, protocolInterpreter);
        threadForRaspberryPi = new Thread(raspberryPiIOManager);
    }

    /**
     * Creates a new thread for communication with RaspberryPi
     */
    private void connectToRaspberryPi(Integer port) {
        raspberryPiIOManager = new RaspberryPiIOManager(port, uav, protocolInterpreter);
        threadForRaspberryPi = new Thread(raspberryPiIOManager);
    }

    /**
     * Creates a new thread for communication with Telemetry Router
     * This method must be called after connectToRaspberryPi because it register
     * raspberryPiIOManager as an observer
     * @param mavNumber - mav number
     * @throws RouterException
     */
    private void connectToTelemetryRouter(Integer mavNumber) throws RouterException {
        TelemetryIOManager telemetryIOManager = new TelemetryIOManager(mavNumber, uav);
        telemetryIOManager.registerObserver(raspberryPiIOManager);
        threadForTelemetryManager = new Thread(telemetryIOManager);
        raspberryPiIOManager.registerObserver(telemetryIOManager);

    }

    public void startThreads() {
        threadForFlightGear.start();
        threadForRaspberryPi.start();
        threadForTelemetryManager.start();
    }

    public static void main(String[] args) throws InterruptedException, RouterException {
        System.out.println("Start");
        UAV uav = new UAV();

        RouteManager routeManager = new RouteManager();
        AutopilotCommander autopilotCommander = new AutopilotCommander(routeManager);

        ProtocolInterpreter protocolInterpreter = new ProtocolInterpreter();
        protocolInterpreter.registerObserver(autopilotCommander);

        Main main;
        if (args.length == 4) {
            main = new Main(uav, autopilotCommander, protocolInterpreter, Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]));
        }
        else {
            main = new Main(uav, autopilotCommander, protocolInterpreter);
        }
        main.startThreads();

        Helper helper = new Helper(protocolInterpreter);
        helper.loadRouteFromCode();
    }
}
