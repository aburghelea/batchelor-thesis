import java.nio.CharBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 05.06.2013
 * Time: 12:30
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
