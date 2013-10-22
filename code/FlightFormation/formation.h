#pragma once

#include <fstream>
#include <iostream>

/* Geometry relative to a uav_drone */
class uav_geometry {
public:

    /* The drone according to witch the geometry is relative */
    void *relative_drone;
    std::string relative_drone_name;

    /* [0] = height diference, [1] = heading factor, [2] = distance */
    std::vector<float> distance;

    /* << Operator overload */
    friend std::ostream& operator<< ( std::ostream&os, const uav_geometry geometry ) {
        os << ">     Relative:" << geometry.relative_drone_name << std::endl;
        os << ">     distance: " << geometry.distance.at ( 0 ) << " - ";
        os <<  geometry.distance.at ( 1 ) << " - ";
        os <<  geometry.distance.at ( 2 ) << std::endl    ;

        return os;
    }
};

/* Drone configuration for the flight */
class uav_drone {
public:

    /* Name of drone */

    std::string name;
    /* Role of drone */

    std::string role;
    /* Personal space where collision avoidance should have equal priority */
    double personal_space;

    /* Emergency space where collision avoidance should have higher priority */
    double emergency_space;

    /* If the current uav_drone is the leader of the formation */
    bool is_leader;

    /* Geometry relative to other drones */
    std::vector < uav_geometry > geometry;

    /* << Operator overload */
    friend std::ostream& operator<< ( std::ostream&os, const uav_drone& drone ) {

        os << "Drone: " << std::endl;
        os << ">  Name: " << drone.name << std::endl ;
        os << ">  Role: " << drone.role << std::endl ;
        os << ">  Personal: "<< drone.personal_space << std::endl ;
        os << ">  Emergency: " << " - " << drone.emergency_space << std::endl ;
        os << ">  Is leader: "<< " - " << drone.is_leader << std::endl;
        os << ">  Geometry:" << std::endl;
        for ( unsigned int i = 0 ; i < drone.geometry.size(); i++ )
            os << drone.geometry.at ( i );
        os << std::endl;

        return os;
    }
};

/* Uav formation configuration for the flight */
class uav_formation {
public:

    /* If the current formation has a uav_drone leader  */
    bool has_leader;

    /* Name of the formation */
    std::string name;

    /* Drones in the desired formation */
    std::vector<uav_drone> drones;

    /**
     * Get the number of drones in the formation specification
     * \return int representing the number of drones needed for the formation
     */
    int get_number_of_drones();

    /**
     * Get the leader of the formation
     * \return uav_formation The leader of the formation if the formation has one,
     * otherwise it returns the first uav in the vector
     */
    uav_drone get_leader();

    /**
     * Gets a specific uav_drone based on an id/name
     * \param id double representing the id/name of the desired uav_drone
     * \return uav_drone The desired uav_drone if it exists, otherwise it returns
     * the first uav in the vector
     */
    uav_drone get_uav_drone ( double id );

    friend std::ostream& operator<< ( std::ostream&os, const uav_formation& formation ) {

        os << ">Has leader: " << formation.has_leader << std::endl;
        os << ">Name: " << formation.name << std::endl;
        for ( unsigned int i = 0; i < formation.drones.size(); i++ )
            os << formation.drones.at ( i );
        os << std::endl;

        return os;
    };
};

/**
 * Reads a formation from a JSON file
 * \param file_name The filename containing the JSON specification
 * for the formation
 * \return uav_formation The uav_formation object
 */
uav_formation read_formation ( const char* file_name );
// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
