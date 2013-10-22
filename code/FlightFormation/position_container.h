#pragma once

#include<map>
#include "uav_position.h"

/**
 * Map containing a pair with the last two positions for the uavs in formation
 */
class position_container {
public:

    /* Map with a pair of the last two positions for the uavs*/
    std::map<double, pair<uav_position,uav_position> > container;

    /* The number of uavs in formation */
    int no_uavs;

    /* Initializes the map with the number of desired uavs */
    position_container ( int _no_uavs ) :
        no_uavs ( _no_uavs ) {
    }

    /**
     * Inserts a new position for a uav
     * \param uav_position to be inserted
     */
    void insert ( const uav_position& uav_position );

    /**
     * If the map contains data about the last uavs
     */
    bool isProcesable();

    /**
     * Gets the last two positions of an uav based on a id (double)
     * \param id The id of the desired uav
     */
    pair<uav_position,uav_position> get ( const double id );

    /**
     * Gets the last two positions of an uav based on a id (string)
     * \param id The id of the desired uav
     */
    pair<uav_position,uav_position> get ( const string id );

};

/* << operator overload */
ostream& operator<< ( ostream& stream, const position_container& u );

// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
