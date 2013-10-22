#include "UAVMissionConfig.h"

UAVMissionConfig::UAVMissionConfig()
{
    uavId = 0;
//    uavLeader = false;

    uavWaypointManager = new UASWaypointManager(NULL);
    uavWaypointManager->uav = this;
}

UAVMissionConfig::UAVMissionConfig(int _uavId) {
    uavId = _uavId;
//    uavLeader = false;

    uavWaypointManager = new UASWaypointManager(NULL);
    uavWaypointManager->uav = this;
}

void UAVMissionConfig::setColor(QColor color) {
    uavColor = color;
    uavWaypointManager->color = color;
}

UAVMissionConfig::~UAVMissionConfig ()
{
    for(int i = 0; i < uavArea.size (); ++i) {
        delete uavArea[i];
    }

    //delete uavWaypointManager;
}

QTextStream& operator<< (QTextStream& out, UAVMissionConfig& uavMC) {
//    UAV ID
    out << uavMC.uavId << endl;
//    UAV Leader
//    out << (uavMC.uavLeader ? "1" : "0") << endl;

//    UAV Role
    out << uavMC.uavRole << endl;

//    UAV Equipment
    out << uavMC.uavEquipment.size () << endl;

    QVectorIterator<UAVEquipment> itEquipment(uavMC.uavEquipment);
    while(itEquipment.hasNext ()) {
        switch(itEquipment.next ()) {
        case CAMERA:
            out << STR_CAMERA << endl;
            break;
        case INFRARED:
            out << STR_INFRARED << endl;
            break;
        default:
            out << STR_CUSTOM << endl;
        }
    }

//    UAV Color R G B
    out << uavMC.uavColor.red () << " " << uavMC.uavColor.green () << " " << uavMC.uavColor.blue () << endl;

//    Waypoints
    out << uavMC.uavArea.size () << endl;
    if(!uavMC.uavArea.empty ()) {
        QVectorIterator<Waypoint*> itWaypoint(uavMC.uavArea);
        while(itWaypoint.hasNext ()) {
            itWaypoint.next ()->save (out);
        }
    }

    out << flush;

    return out;
}

QTextStream& operator>> (QTextStream& in, UAVMissionConfig& uavMC) {
//    UAV Id
    in >> uavMC.uavId;
//    UAV leader
//    QString role;
    in >> uavMC.uavRole;

//    UAV Equipment
    int nEquipment;
    QString equipment;
    in >> nEquipment;
    for(int i = 0; i < nEquipment; ++i) {
        in >> equipment;

        if(equipment.toUpper () == STR_CAMERA) {
            uavMC.uavEquipment.push_back (CAMERA);
        } else if(equipment.toUpper () == STR_INFRARED) {
            uavMC.uavEquipment.push_back (INFRARED);
        } else if(equipment.toUpper () == STR_CUSTOM) {
            uavMC.uavEquipment.push_back (CUSTOM);
        }
    }

//    UAV Color R G B
    int r, g, b;
    in >> r;
    in >> g;
    in >> b;
    uavMC.uavColor.setRed (r);
    uavMC.uavColor.setGreen (g);
    uavMC.uavColor.setBlue (b);

//    Waypoints
    int nWaypoints;
    in >> nWaypoints;
    for(int i = 0; i < nWaypoints; ++i) {
        Waypoint* wp = new Waypoint();
        wp->load (in);
        uavMC.uavArea.push_back (wp);
    }

    return in;
}
