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
    double head;

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

    std::cout << "SYNC: " << sync_mode << " \n MAINTAIN : " << maintain_mode << std::endl;
    return stream.str();


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
