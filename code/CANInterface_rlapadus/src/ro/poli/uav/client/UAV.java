package ro.poli.uav.client;

import ro.poli.uav.commander.FlightGearConstants;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 22.03.2013
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */
public class UAV implements Orientation, Position, Speed {
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;
    private double speed;
    public static final int PAYLOAD_MESSAGE_SIZE = 127;

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public void setHeading(double heading) {
        this.heading = heading;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void updateData(String packet) {
        String[] bits = packet.split("\\" + FlightGearConstants.SEPARATOR);

        this.setLatitude(Double.parseDouble(bits[0]));
        this.setLongitude(Double.parseDouble(bits[1]));
        this.setAltitude(Double.parseDouble(bits[2]));
        this.setHeading(Double.parseDouble(bits[3]));
        this.setSpeed(Double.parseDouble(bits[4]));

    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("00000.000000");
        String s = "UAV{" +
                "id=" + df.format(0) +
                ", lat=" + df.format(latitude) +
                ", lon=" + df.format(longitude) +
                ", alt=" + df.format(altitude) +
                ", heading=" + df.format(heading) +
                ", speed=" + df.format(speed);
        return modifyPayloadLengthToStandard(s);
    }

    private String modifyPayloadLengthToStandard(String payload) {
        if ( payload.length() <  PAYLOAD_MESSAGE_SIZE) {
            return payload + buildExtraContentForPayload(PAYLOAD_MESSAGE_SIZE - payload.length());
        }

        return payload;
    }

    private String buildExtraContentForPayload(int numberOfCharacters) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < numberOfCharacters - 1; i++ ) {
            stringBuilder.append(" ");
        }
        stringBuilder.append("}");


        return stringBuilder.toString();
    }
}
