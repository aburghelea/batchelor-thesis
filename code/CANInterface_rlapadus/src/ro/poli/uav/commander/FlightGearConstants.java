package ro.poli.uav.commander;

/**
 * Created with IntelliJ IDEA.
 * User: Lapa
 * Date: 28.03.2013
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class FlightGearConstants {
    /**
     * Operation mode
     */
    public enum Mode {
        MANUAL_MODE, WAYPOINT_MODE
    }

    /**
     * Separator used by Flight Gear
     */
    public static String SEPARATOR = "$";

    /**
     * Heading lock, needed for FlightGear autopilot
     */
    public enum HeadingLock {
        WING_LEVELER {
            @Override
            public String toString() {
                return "wing-leveler";
            }
        },

        TRUE_HEADING_HOLD {
            @Override
            public String toString() {
                return "true-heading-hold";
            }
        }
    }

    /**
     * Altitude lock, needed for FlightGear autopilot
     */
    public enum AltitudeLock {
        VERTICAL_SPEED_HOLD {
            @Override
            public String toString() {
                return "verical-speed-hold";
            }
        },

        PITCH_HOLD {
            @Override
            public String toString() {
                return "pitch-hold";
            }
        },

        ALTITUDE_HOLD {
            @Override
            public String toString() {
                return "altitude-hold";
            }
        }
    }

    /**
     * Speed control lock, needed for FlightGear autopilot
     */
    public enum SpeedLock {
        SPEED_WITH_THROTTLE {
            @Override
            public String toString() {
                return "speed-with-throttle";
            }
        },

        SPEED_WITH_PITCH_TRIM {
            @Override
            public String toString() {
                return "speed-with-pitch-trim";
            }
        }
    }
}
