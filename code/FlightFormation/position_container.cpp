#include "position_container.h"

typedef std::pair<uav_position, uav_position> pair_up;

/* << operator overload */
ostream& operator<< ( ostream& stream, const position_container& u ) {

    typedef std::map<double, pair<uav_position,uav_position> >::const_iterator iter;
    iter it =u.container.begin();

    for ( ; it!=u.container.end(); ++it ) {
        pair_up p = it->second ;

        stream << it->first << " => " << '\n';
        stream << p.first.id << " " << p.first.altitude  << " ";
        stream << p.first.latitude << " " << p.first.longitude<< " | ";
        stream << p.second.id << " " << p.second.altitude << " ";
        stream << p.second.latitude << " " << p.second.longitude<<"\n";
    }

    return stream;
}

/**
 * Inserts a new position for a uav
 * \param uav_position to be inserted
 */
void position_container::insert ( const uav_position& u ) {

    pair_up pair;

    if ( this->container.count ( u.id ) == 0 ) {
        uav_position second;
        pair = std::make_pair<uav_position, uav_position> ( u, second );
        this->container[u.id] = pair;
    } else {
        pair = this->container[u.id];
        if (
            u.latitude != pair.first.latitude ||
            u.longitude != pair.first.longitude ||
            u.altitude != pair.first.altitude
        )
            this->container[u.id] = std::make_pair<uav_position, uav_position> ( u, pair.first );
    }

}

/**
 * If the map contains data about the last uavs
 */
bool position_container::isProcesable() {

    return this->container.size() == this->no_uavs;
}


/**
 * Gets the last two positions of an uav based on a id (double)
 * \param id The id of the desired uav
 */
pair<uav_position,uav_position> position_container::get ( const double id ) {

    return this->container[id];
}

/**
 * Gets the last two positions of an uav based on a id (string)
 * \param id The id of the desired uav
 */
pair<uav_position, uav_position> position_container::get ( const string id ) {
    return this->get ( atof ( id.c_str() ) );
}


/*
int main()
{
        position_container p ( 3 );
        uav_position a,b,c,d,e,f;
        cout << p << "---\n";

        a.id = 1;       a.altitude = 1;
        p.insert(a);
        cout << p << p.isProcesable()<< "---\n";

        b.id = 1;       b.altitude = 2;
        p.insert(b);
        cout << p << p.isProcesable()<< "---\n";

        c.id = 3;       c.altitude =3;
        p.insert(c);
        cout << p << p.isProcesable()<< "---\n";

        d.id = 4;       d.altitude=4;
        p.insert(d);
        cout << p << p.isProcesable()<< "---\n";

        e.id = 4;       e.altitude=5;
        p.insert(e);
        cout << p << p.isProcesable()<< "---\n";

        f.id = 4;       f.altitude=6;
        p.insert(f);
        cout << p << p.isProcesable()<< "---\n";

        return 0;
}*/
// kate: indent-mode cstyle; indent-width 4; replace-tabs on; 
