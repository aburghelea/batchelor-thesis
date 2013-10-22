#include "uav_position.h"

#define KT_TO_MS(kt) 0.51444 * (kt)
#define MS_TO_KT(m)  1.9438612860586 * (m)
#define FT_TO_M(ft) 0.3048 * (ft)
#define M_TO_FT(m) 3.28084  * (m)

#define A 6378137.0
#define E 8.1819190842622e-2

#define rad2deg(r) (180.0 * (r) / M_PI)
#define deg2rad(r) (M_PI * (r) / 180.0)

#define LINE_STEER_CORECTION 10
#define LEADER_DEVIATION -55

double sync_mode = 0, maintain_mode = 0;
/**
 * Converts the coordinates from (x,y,z) to (latitude, longitude, altitude)
 *  in ECEF coordinates
 */
void uav_position::ecef2lla() {

    double b, ep, p, th, n;

    b = sqrt (
            pow ( A,2 ) *
            ( 1-pow ( E,2 ) )
        );
    ep = sqrt (
             ( pow ( A,2 )-pow ( b,2 ) ) /
             pow ( b,2 )
         );

    p = sqrt (
            pow ( this->x,2 ) +
            pow ( this->y,2 )

        );
    th = atan2 ( A*this->z, b*p );

    this->longitude = atan2 ( this->y, this->x );
    this->latitude = atan2 (
                         ( this->z+ep*ep*b*pow ( sin ( th ),3 ) ),
                         ( p-E*E*A*pow ( cos ( th ),3 ) )
                     );

    n = A/sqrt ( 1-E*E*pow ( sin ( this->latitude ),2 ) );

    this->altitude = p/cos ( this->latitude )-n;
    this->latitude = ( this->latitude*180 ) /M_PI;
    this->longitude = ( this->longitude*180 ) /M_PI;

}

/**
 * Converts the coordinates from (latitude, longitude, altitude) to
 * (x,y,z) in ECEF coordinates
 */
void uav_position::lla2ecef() {

    double latitude, longitude;

    latitude = this->latitude* ( M_PI/180.0f );
    longitude = this->longitude* ( M_PI/180.0f );
    double n = A/sqrt ( ( 1.0-pow ( E,2 ) *pow ( sin ( latitude ),2 ) ) );
    this->x= ( n+this->altitude ) *cos ( latitude ) *cos ( longitude );
    this->y= ( n+this->altitude ) *cos ( latitude ) *sin ( longitude );
    this->z= ( n* ( 1-pow ( E,2 ) ) +this->altitude ) *sin ( latitude );
}

/**
 * Parses an input message and converts it to a uav_position
 * \param str_uav UAV message from the CAN interface
 * \param uav_position the uav_position that will store the date about
 * the uav
 * \returns if the operation succeed
 */
bool uav_position::parse_uav ( const string& str_uav, uav_position& uav_position ) {

    regex exp ( "(id|lat|lon|alt|heading|speed)=[0-9]+\\.?[0-9]+" );

    sregex_iterator it_start ( str_uav.begin(), str_uav.end(), exp );
    sregex_iterator it_end;
    map<string, double> parameters;

    /* parse and save parameters; on invalid values, no parameter is saved */
    while ( it_start != it_end ) {
        string param = ( *it_start ) [0];
        try {
            size_t delim = param.find ( '=' );
            double value = lexical_cast<double> ( param.substr ( delim + 1 ) );
            parameters[param.substr ( 0, delim )] = value;
        } catch ( bad_lexical_cast& ) {
            return false;
        }
        ++it_start;
    }
    uav_position.id = parameters["id"];
    uav_position.latitude = parameters["lat"];
    uav_position.longitude = parameters["lon"];
    uav_position.altitude = parameters["alt"]; // at is in ft
    uav_position.altitude = FT_TO_M ( uav_position.altitude ); // update to m
    uav_position.heading = parameters["heading"];
    uav_position.speed = parameters["speed"]; // is in kt
    uav_position.speed = KT_TO_MS ( uav_position.speed ); // update to m/s

    uav_position.lla2ecef();
}

/*
 * Return a dummy direction for the leader to follow
 */
string uav_position::get_leader_command() {
    stringstream ss;
    this->ecef2lla();
    ss << "MANUAL_MODE TARGET ";
    ss << M_TO_FT ( this->altitude ) + 3 << " ";
    ss << this->heading + LEADER_DEVIATION << " ";
    ss << MS_TO_KT ( this->speed ) << std::endl;

    return ss.str();
}

/**
 * Returns a command identical to the input
 */
string uav_position::get_unmodified_command() {
    stringstream ss;
    this->ecef2lla();
    ss << "MANUAL_MODE TARGET ";
    ss << M_TO_FT ( this->altitude ) << " ";
    ss << this->heading<< " ";
    ss << MS_TO_KT ( this->speed ) << std::endl;

    return ss.str();
}

/**
 * Creates a command for the uav drone
 * \param current_leader current uav_position of the leader
 * \param prev_leader previous uav_position of the leader
 * \param drone The details for the formation about the drone
 */
string uav_position::get_uav_command ( uav_position current_leader,
                                       uav_position prev_leader,
                                       uav_drone drone ) {
    time_t seconds_start;
    time_t seconds_end;
    time_t seconds_difference;

    seconds_start = time ( NULL );
    current_leader.ecef2lla();
    /**
     a tan2(   sin(Δlong).cos(lat2), cos*(lat1).sin(lat2) −
     * sin(lat1).cos(lat2).cos(long2) )
     */

    double alt_dev =  drone.geometry[0].distance[0];
    double head_dev = drone.geometry[0].distance
                      [1];

    double long1 = current_leader.longitude * M_PI / 180;
    double lat1 = current_leader.latitude * M_PI / 180;

    double long2 = this->longitude * M_PI / 180;
    double lat2 = this -> latitude * M_PI / 180;

    long1 = 0;
    lat1 = 0;
    double head;

    head = atan2 (
               sin ( long2-long1 ) *cos ( lat2 ),
               cos ( lat1 ) *sin ( lat2 ) - sin ( lat1 ) * cos ( lat2 ) *cos ( long2-long1 )
           ) * 180 / M_PI;

    double aproach_head = this->get_aproach_angle ( current_leader, prev_leader, drone );
    stringstream stream;
    stream << "MANUAL_MODE TARGET ";
    stream << M_TO_FT ( current_leader.altitude ) + alt_dev << " ";
    stream << current_leader.heading + aproach_head<< " ";
    stream << MS_TO_KT ( current_leader.speed ) << std::endl;
    seconds_difference = time ( NULL ) - seconds_start;
    if ( aproach_head == 0 )
        maintain_mode += seconds_difference;
    else
        sync_mode += seconds_difference;


    return stream.str();


}

/**
 * Calculates the angle offset for the current uav to enter formation
 * \param current_leader current uav_position of the leader
 * \param prev_leader previous uav_position of the leader
 */
double uav_position::get_aproach_angle ( uav_position current_leader,
        uav_position prev_leader,
        uav_drone drone ) {

    double alt_dev =  drone.geometry[0].distance[0];
    double head_dev = drone.geometry[0].distance[1];
    std::string orientation = this->get_orientation ( current_leader, prev_leader );
    double distance =  this->get_distance ( current_leader );
    if ( head_dev == 0 ) {
        if ( distance > 300 ) {
            if ( orientation == LEFT ) {
                std::cout << "Going to right" << std::endl;
                return LINE_STEER_CORECTION;
            } else {
                std::cout << "Goint to left " << std::endl;
                return -1 * LINE_STEER_CORECTION;
            }
            tc->inc_sync();
        }

        else if ( distance < 300 && distance > 200 ) {
            tc->inc_maintain();
        } else if ( distance < 200 ) {
            tc->inc_avoidance();
        }
    }

    else {
        if ( orientation == LEFT && head_dev > 0 ) {
            tc->inc_sync();
            return head_dev * LINE_STEER_CORECTION;
        }
        if ( orientation == RIGHT && head_dev < 0 ) {
            tc->inc_sync();
            return head_dev * LINE_STEER_CORECTION;
        }
        if ( this->get_distance ( current_leader ) < 300 ) {
            tc->inc_sync();
            return head_dev * LINE_STEER_CORECTION;
        }
        if ( this->get_distance ( current_leader ) > 500 ) {
            tc->inc_sync();
            return -1 * head_dev * LINE_STEER_CORECTION;
        }
        if ( distance < 300 && distance > 200 ) {
            tc->inc_maintain();
        } else if ( distance < 200 ) {
            tc->inc_avoidance();
        }
    }



    std::cout << "Reporting no deviation" << std::endl;
    return 0;

}

/**
 * Determines if the current positions is on left or right of a direction
 * determined by two points
 * \param _A destination uav_position
 * \param _B source uav_position
 * \return A string with "left" or "right" for the orientation
 */
std::string uav_position::get_orientation ( uav_position _A, uav_position _B ) {
    double a,b,c,d,e,f,g,h,i;

    a = _A.x - _B.x;
    b = this->y -_B.y;
    c = _A.y - _B.y;
    d = this->x - _B.x;

    double det = a*b - c*d;
    std::cout << "Determinat " << det << std::endl;

    return  det > 0 ? LEFT : RIGHT;
}

/**
 * Calculates the distance in ECEF coordinases to another uav_position
 * \param u Point to where the distance is calculated
 */
double uav_position::get_distance ( uav_position u ) {
    double dx = u.x - this->x;
    double dy = u.y - this->y;
    double dz = u.z - this->z;

    return sqrt ( dx*dx + dy * dy + dz*dz );
}

/* << operator overload */
ostream& operator<< ( ostream& stream, const uav_position& u ) {
    stream << "UAV{" ;
    stream << "id=" << u.id << ", ";
    stream << "latitude=" << u.latitude << ", ";
    stream << "longitude=" << u.longitude << ", ";
    stream << "altitude=" << M_TO_FT ( u.altitude ) << ", ";
    stream << "heading=" << u.heading << ", ";
    stream << "speed=" << MS_TO_KT ( u.speed );
    stream << "x=" << u.x << ", ";
    stream << "y=" << u.y << ", ";
    stream << "Z=" << u.z << ", ";
    stream << "}";

    return stream;
}


// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
