package ro.poli.uav.communication;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 6/12/13
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TelemetryOfOtherUAVSubject {
    public void registerObserver(TelemetryOfOtherUAVObserver telemetryOfOtherUAVObserver);
    public void notifyObserver();
}
