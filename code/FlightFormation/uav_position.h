#pragma once

#include <iostream>

#include <boost/regex.hpp>
#include <boost/lexical_cast.hpp>
#include <sstream>
#include <cmath>
#include "formation.h"
#include "timer_counter.h"

using namespace std;
using namespace boost;

#define LEFT "left"
#define RIGHT "right"


class uav_position {
public:
    double id;
    double latitude;
    double longitude;
    double altitude;
    double heading;
    double speed;
    double x;
    double y;
    double z;
    timer_counter *tc;

    uav_position() :
        id ( 0 ),
        latitude ( 0 ),
        longitude ( 0 ),
        altitude ( 0 ),
        heading ( 0 ),
        speed ( 0 ) {
           tc = timer_counter::getInstance();   
    }

    uav_position (
        double _id,
        double _latitude,
        double _longitude,
        double _altitude,
        double _heading,
        double _speed
    ) :
        id ( _id ),
        latitude ( _latitude ),
        longitude ( _longitude ),
        altitude ( _altitude ),
        heading ( _heading ),
        speed ( _speed ) {
    }

    /**
     * Converts the coordinates from (x,y,z) to (latitude, longitude, altitude)
     *  in ECEF coordinates
     */
    void ecef2lla();

    /**
     * Converts the coordinates from (latitude, longitude, altitude) to
     * (x,y,z) in ECEF coordinates
     */
    void lla2ecef();

    /**
     * Parses an input message and converts it to a uav_position
     * \param str_uav UAV message from the CAN interface
     * \param uav_position the uav_position that will store the date about
     * the uav
     * \returns if the operation succeed
     */
    static bool parse_uav ( const string& str_uav, uav_position& u );

    /*
     * Return a dummy direction for the leader to follow
     */
    string get_leader_command();

    /**
     * Returns a command identical to the input
     */
    string get_unmodified_command();

    /**
     * Creates a command for the uav drone
     * \param current_leader current uav_position of the leader
     * \param prev_leader previous uav_position of the leader
     * \param drone The details for the formation about the drone
     */
    string get_uav_command ( uav_position current_leader , uav_position prev_leader, uav_drone drone );

    /**
     * Calculates the distance in ECEF coordinases to another uav_position
     * \param u Point to where the distance is calculated
     */
    double get_distance ( uav_position u );

    /**
     * Determines if the current positions is on left or right of a direction
     * determined by two points
     * \param _A destination uav_position
     * \param _B source uav_position
     * \return A string with "left" or "right" for the orientation
     */
    string get_orientation ( uav_position a, uav_position b );

    /**
     * Calculates the angle offset for the current uav to enter formation
     * \param current_leader current uav_position of the leader
     * \param prev_leader previous uav_position of the leader
     */
    double get_aproach_angle ( uav_position current_leader,uav_position prev_leader, uav_drone drone );

    virtual ~uav_position() {
    }
private:

};

ostream& operator<< ( ostream& stream, const uav_position& u );


// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
