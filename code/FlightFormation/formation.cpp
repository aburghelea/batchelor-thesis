#include "json_spirit.h"
#include "formation.h"

using namespace json_spirit;

#define VIRTUAL "virtual"

/**
 * Get the number of drones in the formation specification
 * \return int representing the number of drones needed for the formation
 */
int uav_formation::get_number_of_drones() {

    this->drones.size();
}


/**
 * Get the leader of the formation
 * \return uav_formation The leader of the formation if the formation has one,
 * otherwise it returns the first uav in the vector
 */
uav_drone uav_formation::get_leader() {

    for ( int i = 0; i < this->drones.size(); i++ ) {
        if ( this->drones.at ( i ).is_leader )
            return this->drones.at ( i );
    }

    return this->drones.at ( 0 );
}

/**
 * Gets a specific uav_drone based on an id/name
 * \param id double representing the id/name of the desired uav_drone
 * \return uav_drone The desired uav_drone if it exists, otherwise it returns
 * the first uav in the vector
 */
uav_drone uav_formation::get_uav_drone ( double id ) {

    std::stringstream stream;
    stream << id;

    for ( int i = 0; i < this->drones.size(); i++ ) {
        if ( this->drones.at ( i ).name == stream.str() )
            return this->drones.at ( i );
    }

    return this->drones.at ( 0 );
}

/**
 * Creates an uav_drone based on a json_spirit Object
 * \param obj JSON Object
 * \return uav_drone according to JSON configuration
 */
uav_drone read_uav_drone ( const Object &obj ) {

    uav_drone drone;

    for ( Object::size_type i = 0; i != obj.size(); ++i ) {

        const Pair& pair = obj[i];
        const std::string& name  = pair.name_;
        const Value&  value = pair.value_;

        if ( name == "name" )
            drone.name = value.get_str();
        else if ( name == "role" )
            drone.role = value.get_str();
        else if ( name == "personal_space" )
            drone.personal_space = value.get_real();
        else if ( name == "emergency_space" )
            drone.emergency_space = value.get_real();
        else if ( name == "is_leader" )
            drone.is_leader = value.get_bool();
    }

    return drone;
}

/**
 * Reads each geometry element and adds it to the formation
 * \param formation The formation where the geometry will be stored
 * \param obj JSON Object describing the geometry
 */
void read_geometry ( uav_formation& formation, const Object &obj ) {

    uav_geometry geometry;
    geometry.relative_drone = NULL;
    int drone_idx = -1;

    for ( Object::size_type i = 0; i != obj.size(); ++i ) {
        const Pair& pair = obj[i];
        const std::string& name  = pair.name_;
        const Value&  value = pair.value_;

        if ( name == "relative_to" ) {
            for ( unsigned int j = 0; j < formation.drones.size(); j++ ) {
                if ( formation.drones.at ( j ).name == value.get_str() ) {

                    geometry.relative_drone = &formation.drones.at ( j );
                    geometry.relative_drone_name = value.get_str();
                    break;
                }
            }
        } else if ( name == "distance" ) {
            const Array& distance = value.get_array();
            geometry.distance.push_back ( distance[0].get_real() );
            geometry.distance.push_back ( distance[1].get_real() );
            geometry.distance.push_back ( distance[2].get_real() );
        } else if ( name == "drone_name" ) {
            for ( unsigned int j = 0; j < formation.drones.size(); j++ ) {
                if ( formation.drones.at ( j ).name == value.get_str() ) {
                    drone_idx = j;
                    break;
                }
            }
        }
    }

    if ( drone_idx >= 0 && geometry.relative_drone != NULL ) {
        formation.drones.at ( drone_idx ).geometry.push_back ( geometry );
    }
}

/**
 * Reads a formation from a JSON file
 * \param file_name The filename containing the JSON specification
 * for the formation
 * \return uav_formation The uav_formation object
 */
uav_formation read_formation ( const char* file_name ) {

    uav_formation formation;

    std::ifstream file_stream;
    file_stream.open ( file_name, std::ifstream::in );

    if ( !file_stream.is_open() ) {
        std::cerr << " Could not open " << file_name << std::endl;
        exit ( -1 );
    }

    Value value;

    read ( file_stream, value );

    const Object& addr_array = value.get_obj();
    for ( Object::size_type i = 0; i != addr_array.size(); i++ ) {
        const Pair& obj = addr_array[i];

        const std::string& name  = obj.name_;
        const Value&  value = obj.value_;
        if ( name == "name" ) {
            formation.name = value.get_str();
        } else if ( name == "has_leader" ) {
            formation.has_leader = value.get_bool();
        } else if ( name == "drones" ) {
            const Array& drone_array = value.get_array();
            for ( unsigned int j = 0; j < drone_array.size(); j++ ) {
                uav_drone drone = read_uav_drone ( drone_array[j].get_obj() ) ;
                if ( drone.name != VIRTUAL )
                    formation.drones.push_back ( drone );
            }
        } else if ( name == "geometry" ) {
            std::cout << "Reading formation" << std::endl;
            const Array& geometry = value.get_array();
            for ( unsigned int j = 0; j < geometry.size(); j++ ) {
                read_geometry ( formation, geometry[j].get_obj() );
            }
        }

    }

    return formation;
}

/**
 * Input file is a json.
 * It has to be valid acording to jsonlint.com
 */
// int main ( int argc, char **argv ) {
//
//     if ( argc != 2 ) {
//         std::cout << "Usage: " << argv[0] << " json_file\n";
//         exit ( 0 );
//     }
//
//     const char* file_name ( argv[1] );
//
//     std::cout << read_formation ( file_name );
//
//     return 0;
// }
// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
