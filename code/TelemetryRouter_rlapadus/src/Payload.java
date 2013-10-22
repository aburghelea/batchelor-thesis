import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 27.05.2013
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class Payload implements Serializable {
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;
    private double speed;
    public static final int PAYLOAD_MESSAGE_SIZE = 127;

    public static class Builder {
        private final double  latitude;
        private final double longitude;
        private final double altitude;
        private double heading = 0;
        private double speed = 0;

        public Builder(double latitude, double longitude, double altitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }

        public Builder heading(int val) {
            heading = val;
            return this;
        }

        public Builder speed(int val) {
            speed = val;
            return this;
        }

        public Payload build() {
            return new Payload(this);
        }
    }

    private Payload(Builder builder) {
        latitude = builder.latitude;
        longitude = builder.longitude;
        altitude = builder.altitude;
        heading = builder.heading;
        speed = builder.speed;
    }

    public String serialize() {
        String TEMPLATE = "%f$%f$%f$%f$%f";

        final String content = String.format(TEMPLATE, latitude, longitude, altitude, heading, speed);
        return modifyPayloadLengthToStandard(content);
    }

    private String modifyPayloadLengthToStandard(String payload) {
        if ( payload.length() <  PAYLOAD_MESSAGE_SIZE) {
            return payload + buildExtraContentForPayload(PAYLOAD_MESSAGE_SIZE - payload.length());
        }

        return payload;
    }

    private String buildExtraContentForPayload(int numberOfCharacters) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < numberOfCharacters; i++ ) {
            stringBuilder.append("$");
        }

        return stringBuilder.toString();
    }
}
