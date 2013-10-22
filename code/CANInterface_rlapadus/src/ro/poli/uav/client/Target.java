package ro.poli.uav.client;

import ro.poli.uav.commander.FlightGearConstants;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 29.03.2013
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class Target implements Speed{
    private Double targetAltitude;
    private Double targetHeading;
    private Double targetSpeed;
    private String targetName;

    private FlightGearConstants.AltitudeLock altitudeLock;
    private FlightGearConstants.HeadingLock headingLock;
    private FlightGearConstants.SpeedLock speedLock;

    public Target() {
        setDefaultLocks();
    }

    public Target(double targetAltitude, double targetHeading, double targetSpeed) {
        this.targetAltitude = targetAltitude;
        this.targetHeading = targetHeading;
        this.targetSpeed = targetSpeed;
        setDefaultLocks();
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public FlightGearConstants.HeadingLock getHeadingLock() {
        return headingLock;
    }

    public void setHeadingLock(FlightGearConstants.HeadingLock headingLock) {
        this.headingLock = headingLock;
    }

    public double getTargetAltitude() {
        return targetAltitude;
    }

    public void setTargetAltitude(double targetAltitude) {
        this.targetAltitude = targetAltitude;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading;
    }

    public FlightGearConstants.AltitudeLock getAltitudeLock() {
        return altitudeLock;
    }

    public void setAltitudeLock(FlightGearConstants.AltitudeLock altitudeLock) {
        this.altitudeLock = altitudeLock;
    }

    public FlightGearConstants.SpeedLock getSpeedLock() {
        return speedLock;
    }

    public void setSpeedLock(FlightGearConstants.SpeedLock speedLock) {
        this.speedLock = speedLock;
    }

    public boolean isSpeedSet() {
        return targetSpeed != null;
    }

    public boolean isHeadingSet() {
        return  targetHeading != null;
    }

    public boolean isAltitudeSet() {
        return  targetAltitude != null;
    }

    private void setDefaultLocks() {
        altitudeLock = FlightGearConstants.AltitudeLock.ALTITUDE_HOLD;
        headingLock = FlightGearConstants.HeadingLock.TRUE_HEADING_HOLD;
        speedLock = FlightGearConstants.SpeedLock.SPEED_WITH_THROTTLE;
    }

    @Override
    public String toString() {
        return "Target{" +
                "targetAltitude=" + targetAltitude +
                ", targetHeading=" + targetHeading +
                ", targetSpeed=" + targetSpeed +
                ", targetName='" + targetName + '\'' +
                '}';
    }

    @Override
    public double getSpeed() {
        return targetSpeed;
    }

    @Override
    public void setSpeed(double speed) {
        this.targetSpeed = speed;
    }

    public void updateTarget(Target target) {
        if (target.isSpeedSet()) {
            setSpeed(target.getSpeed());
        }
        if (target.isAltitudeSet()) {
            setTargetAltitude(target.getTargetAltitude());
        }
        if (target.isHeadingSet()) {
            setTargetHeading(target.getTargetHeading());
        }
    }
}
