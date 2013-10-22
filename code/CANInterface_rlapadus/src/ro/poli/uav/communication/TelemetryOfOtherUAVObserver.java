package ro.poli.uav.communication;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 6/12/13
 * Time: 9:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TelemetryOfOtherUAVObserver {
    public void updateTelemetryOfOtherUAVs(String telemetryOfOther);
    public void updateMessagesFromOtherUAVs(String messageFromOther);
}
