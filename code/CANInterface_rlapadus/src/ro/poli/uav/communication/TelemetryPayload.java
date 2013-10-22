package ro.poli.uav.communication;

import java.text.DecimalFormat;

public class TelemetryPayload extends Payload {
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;
    private double speed;
    private int id;
    public static final int PAYLOAD_MESSAGE_SIZE = 127;

    public static class Builder {
        private final double  latitude;
        private final double longitude;
        private final double altitude;
        private final int id;
        private double heading = 0;
        private double speed = 0;

        public Builder(int id, double latitude, double longitude, double altitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }

        public Builder heading(double val) {
            heading = val;
            return this;
        }

        public Builder speed(double val) {
            speed = val;
            return this;
        }

        public Payload build() {
            return new TelemetryPayload(this);
        }
    }

    private TelemetryPayload(Builder builder) {
        id = builder.id;
        latitude = builder.latitude;
        longitude = builder.longitude;
        altitude = builder.altitude;
        heading = builder.heading;
        speed = builder.speed;
    }

    public String serialize() {
        DecimalFormat df = new DecimalFormat("00000.000000");
        String content = "UAV{" +
                "id=" + df.format(id) +
                ", lat=" + df.format(latitude) +
                ", lon=" + df.format(longitude) +
                ", alt=" + df.format(altitude) +
                ", heading=" + df.format(heading) +
                ", speed=" + df.format(speed);

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

        for (int i = 0; i < numberOfCharacters - 1; i++ ) {
            stringBuilder.append(" ");
        }
        stringBuilder.append("}");


        return stringBuilder.toString();
    }

}
