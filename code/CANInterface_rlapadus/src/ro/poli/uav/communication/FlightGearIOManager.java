package ro.poli.uav.communication;

import ro.poli.uav.client.UAV;
import ro.poli.uav.commander.AutopilotCommander;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/11/13
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlightGearIOManager implements Runnable {
    private static int INPUT_PORT_FLIGHT_GEAR = 55000;
    private static int OUTPUT_PORT_FLIGHT_GEAR = 55001;

    private int inputPort;
    private int outputPort;

    private ServerSocket serverSocket;
    private Socket inputSocket;
    private Socket outputSocket;

    private Scanner inputScanner;
    private PrintWriter outputWriter;

    private boolean forceClose = false;
    private UAV uav;

    private AutopilotCommander autopilotCommander;

    /**
     * Class that deals with input/output operations from FlightGear.
     * This class receives information about the uav and sends commands to FlightGear
     * @param inputPort - input port
     * @param outputPort - output port
     * @param uav - uav instance that will contain data about position, speed and heading
     * @param autopilotCommander - autopilot commander gives the next command that will be send to FlightGear
     */
    public FlightGearIOManager(int inputPort, int outputPort, UAV uav, AutopilotCommander autopilotCommander) {
        System.out.println("FlightGearIOManager: using specified ports ");
        this.inputPort = inputPort;
        this.outputPort = outputPort;
        this.uav = uav;
        this.autopilotCommander = autopilotCommander;
    }

    /**
     * Class that deals with input/output operations from FlightGear.
     * This class receives information about the uav and sends commands to FlightGear
     * @param uav - uav instance that will contain data about position and speed
     * @param autopilotCommander - autopilot commander gives the next command that will be send to FlightGear
     */
    public FlightGearIOManager(UAV uav, AutopilotCommander autopilotCommander) {
        this.uav = uav;
        this.autopilotCommander = autopilotCommander;
        inputPort = INPUT_PORT_FLIGHT_GEAR;
        outputPort = OUTPUT_PORT_FLIGHT_GEAR;
    }

    /**
     * Forces the manager to stop the thread.
     */
    public void forceClose() {
        forceClose = true;
    }

    /**
     * Method used for establishing connection to FlightGear.
     */
    private void establishConnection() {
        try {
            serverSocket = new ServerSocket(inputPort);
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Couldn't create socket server", e);
        }


        try {
            System.out.println("Waiting for FlightGear");
            inputSocket = serverSocket.accept();
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Exception at accepting a client", e);
        }

        try {
            System.out.println(inputSocket.getInetAddress());
            Thread.sleep(2000);
            outputSocket = new Socket(inputSocket.getInetAddress(), outputPort);
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Couldn't connect to FlightGear", e);
        } catch (InterruptedException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Interrupted exception", e);
        }

        try {
            System.out.println("FlightGear accepted");
            inputScanner = new Scanner(inputSocket.getInputStream());
            outputWriter = new PrintWriter(outputSocket.getOutputStream(), true);
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                    "Exception when creating the scanner and the writer", e);
        }
    }

    /**
     * Closes the sockets
     */
    private void cleanup() {
        try {
            outputWriter.close();
            inputScanner.close();
            inputSocket.close();
            outputSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Couldn't close FlightGear sockets", ex);
        }
    }

    /**
     * Receives data about the uav and updates the state of uav.
     * Sends commands to FlightGear.
     */
    @Override
    public void run() {
        establishConnection();

        while (!inputSocket.isClosed() && !forceClose) {
            String packet = inputScanner.nextLine();
            uav.updateData(packet);
            String command = autopilotCommander.getCommand();

            if (!command.equals("EMPTY_COMMAND")) {
                System.out.println("Command for FlightGear = " + command);
                outputWriter.println(command);
            }
        }

        cleanup();
    }
}
