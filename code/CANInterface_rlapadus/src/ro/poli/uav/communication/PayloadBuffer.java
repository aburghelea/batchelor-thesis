package ro.poli.uav.communication;

import java.nio.CharBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 6/11/13
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayloadBuffer {
    private int bytesReadFromAPayload;
    private StringBuilder payloadBufferBuilder;

    public PayloadBuffer() {
        bytesReadFromAPayload = 0;
        payloadBufferBuilder = new StringBuilder("");
    }

    public void addPayloadPart(CharBuffer payloadPart) {
        payloadBufferBuilder.append(payloadPart);
        bytesReadFromAPayload += payloadPart.length();
    }

    public boolean isPayloadMessageComplete() {
        return bytesReadFromAPayload >=Payload.PAYLOAD_MESSAGE_SIZE;
    }

    public String getCompletePayloadMessage() {
        String completePayload = payloadBufferBuilder.substring(0, Payload.PAYLOAD_MESSAGE_SIZE);
        resetPayload();

        return completePayload;
    }

    private void resetPayload() {
        String informationFromNextPayload = payloadBufferBuilder.substring(Payload.PAYLOAD_MESSAGE_SIZE,
                payloadBufferBuilder.length());
        payloadBufferBuilder = new StringBuilder(informationFromNextPayload);
        bytesReadFromAPayload = 0;
    }
}
