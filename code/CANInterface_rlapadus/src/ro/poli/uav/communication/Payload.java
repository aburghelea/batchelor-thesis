package ro.poli.uav.communication;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 6/11/13
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Payload implements Serializable {
    public static final int PAYLOAD_MESSAGE_SIZE = 127;
    private String payload;
    
    public Payload() {
    	payload = "";
    }
    
    public Payload(String content) {
    	payload = content;
    }

    public String serialize() {
        return modifyPayloadLengthToStandard(payload);
    }

    private String modifyPayloadLengthToStandard(String content) {
        if ( content.length() <  PAYLOAD_MESSAGE_SIZE) {
            return content + buildExtraContentForPayload(PAYLOAD_MESSAGE_SIZE - content.length());
        }

        return payload;
    }

    private String buildExtraContentForPayload(int numberOfCharacters) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < numberOfCharacters; i++ ) {
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }
}
