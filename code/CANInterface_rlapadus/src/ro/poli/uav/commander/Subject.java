package ro.poli.uav.commander;

/**
 * Created with IntelliJ IDEA.
 * User: lapa
 * Date: 4/18/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Subject {
    void registerObserver(Observer observer);
    void notifyObservers();
}
