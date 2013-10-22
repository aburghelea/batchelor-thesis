/**
 * Raspberry Pi IO Manager
 */
#include <iostream>
#include <boost/algorithm/string.hpp>
#include <fstream>
#include <string>
#include <boost/array.hpp>
#include <boost/asio.hpp>
#include <boost/make_shared.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>

#include "formation.h"
#include "uav_position.h"
#include "position_container.h"
#include "timer_counter.h"

#define DEFAULT_PORT_STRING "35001"
#define DEFAULT_WAYPOINTS_FILE "waypoints.txt"
#define DEFAULT_FORMATION_FILE "line.json"
#define MESSAGE_SIZE 128

namespace btcp = boost::asio::ip;

std::string host;
std::string port;
std::string waypoints;
std::string formation_file;
std::string c_mav;
timer_counter *timer_counter;

/**
 * Shared pointer containing the socket connection between RPI IO and CAN
 */
shared_ptr<btcp::tcp::socket> shared_socket;

/**
 * Reads all lines from a file
 * \param file_name Path to file (absolute or relative)
 * \return List of strings with each line
 */
std::list<std::string> read_lines_from_file ( std::string file_name ) {

    std::list<std::string> lines;
    std::string line;

    std::ifstream input;
    input.open ( file_name.c_str() );
    std::cout<< "Opening " << file_name << std::endl;
    while ( input ) {
        std::getline ( input, line );
        if ( !line.empty() ) {
            boost::erase_all ( line, "\r" );
            line.append ( "\n" );
            std::cout<<"Read: " << line << std::endl;
            lines.push_back ( line );
        }
    }

    std::cout << "finished read" << std::endl;
    input.close();

    return lines;
}

/**
 * Sends a list of strings over a TCP socket
 * \param lines A list of strings
 * \socket output socket
 */
void write_list_to_sockets ( std::list<std::string> lines,
                             btcp::tcp::socket *socket ) {

    while ( !lines.empty() ) {

        boost::system::error_code ignored_error;
        boost::asio::write (
            *socket, boost::asio::buffer ( lines.front() ),
            ignored_error );
        std::cout << "Writing to socket " << lines.front();
        lines.pop_front();

    }
}

/**
 * Validates (and initializes if needed) the input parameters
 * \param argc Argument count (same as main)
 * \param argv String array (same as main)
 */
void validate_params ( int argc, char* argv[] ) {

    if ( argc <= 5 ) {
        std::cerr << "Usage: "<< argv[0] <<" <host> <port> ";;
        std::cerr << " <initial_commands> <formation_file> <mav_id>"<<std::endl;

        exit ( 1 );
    }
    c_mav = std::string ( argv[5] );
    formation_file = std::string ( argv[4] );
    waypoints = std::string ( argv[3] );
    port = std::string ( argv[2] );
    host = std::string ( argv[1] );
}

/**
 * Create a boost shared socket for connecting to the CAN interface
 */
void create_shared_socket () {

    boost::asio::io_service io_service;
    btcp::tcp::resolver resolver ( io_service );

    /* Get the endpoint with socket */
    btcp::tcp::resolver::query query ( host, port );
    btcp::tcp::resolver::iterator endpoint_it = resolver.resolve ( query );
    btcp::tcp::resolver::iterator end;

    shared_socket = boost::make_shared<btcp::tcp::socket> (
                        boost::ref ( io_service )

                    );

    boost::system::error_code error = boost::asio::error::host_not_found;
    while ( error && endpoint_it != end ) {
        shared_socket.get()->close();
        shared_socket.get()->connect ( *endpoint_it++, error );
    }
    if ( error )
        throw boost::system::system_error ( error );
}

/**
 * Create and send a command to the controlled uav_drone
 * \param position_container Map containing last two positions for each drone
 * in formation
 * \param uav_formation UAV formation configuration
 * \param i Iteration step (helps debugging)
 */

void process_command ( position_container pos_container,
                       uav_formation formation,
                       int i ) {

    string command;
    boost::system::error_code error;
    uav_position u = pos_container.get ( 0 ).first;
    std::pair< uav_position, uav_position > leader_pair;
    leader_pair = pos_container.get ( formation.get_leader().name );
    uav_position curent_leader = leader_pair.first;
    uav_position prev_leader = leader_pair.second;


    uav_drone drone= formation.get_uav_drone ( atof ( c_mav.c_str() ) );
    if ( pos_container.isProcesable() ) {
        if ( formation.get_leader().name == c_mav ) {
            command = u.get_leader_command ();
        } else {
            command = u.get_uav_command ( curent_leader,prev_leader, drone );
        }
        boost::asio::write ( *shared_socket.get(),
                             boost::asio::buffer ( command ),
                             error );
    }

    if ( i % 500 == 0 ) {
        std::cout << "Writing " << command;
        std::cout << "Distance to leader" << u.get_distance ( curent_leader );
        std::cout << std::endl;
        std::cout << "Command " << command << std::endl;
        std::cout << "Orientation " << u.get_orientation (
                      curent_leader, prev_leader
                  ) << std::endl;
    }

}

/**
 * Reads an input file and sends it line by line
 * over a tcp socket
 * Params: host port[=35001] input[=waypoints.txt]
 */
int main ( int argc, char *argv[] ) {
    timer_counter = timer_counter::getInstance();

    bool notified = false;
    validate_params ( argc, argv );
    uav_formation formation = read_formation ( formation_file.c_str() );
    std::cout << formation << endl;
    std::cout << "Elements " << formation.get_number_of_drones() << std::endl;
    std::cout << "Controlled mav" << c_mav << std::endl;
    std::cout << "Leader mav" << formation.get_leader() << std::endl;

    try {
        position_container pos_container ( formation.get_number_of_drones() );
        create_shared_socket ();
        btcp::tcp::socket* socket = shared_socket.get();

        std::list<std::string> lines = read_lines_from_file ( waypoints );
        write_list_to_sockets ( lines,socket );

        std::cout << "Wrote lists to socket" << std::endl;
        std::cout << pos_container;
        for ( int i = 0; ; i++ ) {

            boost::array<char, MESSAGE_SIZE> buf;
            boost::system::error_code error;
            size_t len = socket->read_some (
                             boost::asio::buffer ( buf ),
                             error );

            uav_position position;
            uav_position::parse_uav ( buf.data(), position );
            pos_container.insert ( position );

            if ( pos_container.isProcesable() ) {
                if ( !notified ) {
                    notified = true;
                    std::cout << pos_container;
                    std::cout << "Now the data for all planes has arived";
                    std::cout << std::endl;
                }
            }
            process_command ( pos_container, formation, i );

        }

        std::cout << "Ended" << std::endl;
    } catch ( std::exception &e ) {
        std::cerr <<"AI_MANAGER: "<< e.what() << std::endl;
    }

    std::cout << "Exiting" << std::endl;

    return 0;
}

// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
