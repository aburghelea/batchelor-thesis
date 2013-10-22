package ro.poli.uav.communication;

import ro.poli.uav.client.UAV;
import ro.poli.uav.protocol.ProtocolInterpreter;

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
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class RaspberryPiIOManager implements Runnable, TelemetryOfOtherUAVObserver, TelemetryOfOtherUAVSubject {
    private static int RASPBERRY_PI_PORT = 55102;
    private int port;

    private ServerSocket serverSocket;
    private Socket ioSocket;

    private Scanner inputScanner;
    private PrintWriter outputWriter;

    private boolean forceClose = false;


    private String telemetryOfOtherUAVs;
    private boolean isTelemetryOfOtherUAVSet;
    private UAV uav;
    private ProtocolInterpreter protocolInterpreter;
    private TelemetryOfOtherUAVObserver telemetryOfOtherUAVObserver;
    private String completeMessage;
    private String completeMessageOut;


    /**
     * Class that deals with input/output operations from RaspberryPi.
     * This class receives commands send by RaspberryPi and sends back information about the uav.
     * @param port - port
     * @param uav - uav instance that will contain data about position, speed and heading
     * @param protocolInterpreter - interpreter of commands send by RaspberryPi
     */
    public RaspberryPiIOManager(int port, UAV uav, ProtocolInterpreter protocolInterpreter) {
        System.out.println("Rpi io manager using "+port);
        this.port = port;
        this.uav = uav;
        this.protocolInterpreter = protocolInterpreter;
        isTelemetryOfOtherUAVSet = false;
        completeMessage = "";
        completeMessageOut = "";
    }

    /**
     * Class that deals with input/output operations from RaspberryPi.
     * This class receive commands send by RaspberryPi and sends back information about the uav.
     * @param uav - uav instance that will contain data about position and speed
     * @param protocolInterpreter - interpreter of commands send by RaspberryPi
     */
    public RaspberryPiIOManager(UAV uav, ProtocolInterpreter protocolInterpreter) {
        this.uav = uav;
        this.protocolInterpreter = protocolInterpreter;
        port = RASPBERRY_PI_PORT;
        isTelemetryOfOtherUAVSet = false;
        completeMessage = "";
        completeMessageOut = "";
    }

    public void setTelemetryOfOtherUAVs(String telemetryOfOtherUAVs) {
        this.isTelemetryOfOtherUAVSet = true;
        this.telemetryOfOtherUAVs = telemetryOfOtherUAVs;
    }

    /**
     * Forces the manager to stop the thread.
     */
    public void forceClose() {
        forceClose = true;
    }

    /**
     * Method used for establishing connection to RaspberryPi.
     */
    private void establishConnection() {
        try {
            serverSocket = new ServerSocket(port);

            System.out.println("Waiting for RaspberryPi");
            ioSocket = serverSocket.accept();

            System.out.println(ioSocket.getInetAddress());
            System.out.println("RaspberryPi accepted");
            inputScanner = new Scanner(ioSocket.getInputStream());
            outputWriter = new PrintWriter(ioSocket.getOutputStream(), true);
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Couldn't connect to RaspberryPi", e);
        }

    }

    /**
     * Closes the sockets
     */
    private void cleanup() {
        try {
            outputWriter.close();
            inputScanner.close();
            ioSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Couldn't close RaspberryPi sockets", ex);
        }
    }

    /**
     * Receives a command from RaspberryPi and sends it to ProtocolInterpreter.
     * Sends data about uav to RaspberryPi.
     */
    @Override
    public void run() {
        establishConnection();

        Thread threadForReceivingInfo = getDataFromRaspberryPi();
        Thread threadForSendingInfo = sendDataToRaspberryPi();

        try {
            threadForReceivingInfo.join();
            threadForSendingInfo.join();
        } catch (InterruptedException e) {
        }

        cleanup();
    }

    private Thread getDataFromRaspberryPi() {
        Thread threadForReceivingInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!ioSocket.isClosed() && !forceClose) {
                    String packet ="";
                    try {
                        packet = inputScanner.nextLine();
                    } catch (Exception e) {
                        forceClose();
                    }

                    if (packet.substring(0, 5).equals("BROAD"))
                    {
                    	System.out.println("Sending message: " + packet.substring(6));
                    	completeMessageOut = packet.substring(6);
                    	notifyObserver();
                    }
                    
                    if (!packet.equals("Empty")) {
                        System.out.print("RPI packet ");
                        System.out.println(packet);
                        protocolInterpreter.interpretCommand(packet);
                    }

                    outputWriter.println(uav);
                }
            }
        });

        threadForReceivingInfo.start();

        return threadForReceivingInfo;
    }

    private Thread sendDataToRaspberryPi() {
        Thread threadForSendingInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!ioSocket.isClosed() && !forceClose) {
                    System.out.println("Own telemetry :" + uav);
                    outputWriter.println(uav);

                    if (isTelemetryOfOtherUAVSet) {
                        System.out.println("Telemetry of other: " + telemetryOfOtherUAVs);
                        outputWriter.println(telemetryOfOtherUAVs);
                    }
                    
                    if (!completeMessage.equals("")) {
                        System.out.println("Message: " + completeMessage);
                        outputWriter.println(completeMessage);
                        completeMessage = "";
                    }

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Interrupt exception", e);
                    }

                }
            }
        });

        threadForSendingInfo.start();

        return threadForSendingInfo;
    }

    @Override
    public void updateTelemetryOfOtherUAVs(String payload) {
        setTelemetryOfOtherUAVs(payload);
    }

	@Override
	public void registerObserver(TelemetryOfOtherUAVObserver telemetryOfOtherUAVObserver) {
		this.telemetryOfOtherUAVObserver = telemetryOfOtherUAVObserver;
	}

	@Override
	public void notifyObserver() {
		telemetryOfOtherUAVObserver.updateMessagesFromOtherUAVs(completeMessageOut);
		completeMessageOut = "";
	}

	@Override
	public void updateMessagesFromOtherUAVs(String messageFromOther) {
		completeMessage = messageFromOther;
	}
}
