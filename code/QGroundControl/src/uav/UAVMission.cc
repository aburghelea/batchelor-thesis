#include "UAVMission.h"

UAVMission::UAVMission()
{
    UAVMissionConfig* conf0 = new UAVMissionConfig();
    uavMissionConfigs.push_back (conf0);
}

int UAVMission::UAVcount () {
    return uavMissionConfigs.size ();
}

void UAVMission::addUAV () {
    uavMissionConfigs.push_back (new UAVMissionConfig());
}

void UAVMission::addUAV(UAVMissionConfig* uav) {
    uavMissionConfigs.push_back (uav);
}

void UAVMission::removeUAV () {
    uavMissionConfigs.pop_back ();
}

UAVMission::~UAVMission ()
{
    for(int i = 0; i < uavMissionConfigs.size (); ++i) {
        delete uavMissionConfigs[i];
    }
}

QTextStream& operator<< (QTextStream& out, UAVMission& uavMission) {
//    Mission type
    switch(uavMission.uavMissionType) {
    case UAV_SURVEILLANCE:
        out << STR_UAV_SURVEILLANCE;
        break;
    case UAV_MAPPING:
        out << STR_UAV_MAPPING;
        break;
    case UAV_DISASTER_MANAGEMENT:
        out << STR_UAV_DISASTER_MANAGEMENT;
        break;
    }
    out << endl;

//    Number of UAVs
    out << uavMission.UAVcount () << endl;

//    Configurations separated by UAV_END
    QVectorIterator<UAVMissionConfig*> itMissionConfig(uavMission.uavMissionConfigs);
    while(itMissionConfig.hasNext ()) {
        out << *(itMissionConfig.next ());
        out << UAV_END << endl;
    }

    return out;
}

QTextStream& operator>> (QTextStream& in, UAVMission& uavMission) {
//    Mission type
    QString missionType;
    in >> missionType;
    if(missionType == STR_UAV_MAPPING) {
        uavMission.uavMissionType = UAV_MAPPING;
    } else if(missionType == STR_UAV_DISASTER_MANAGEMENT) {
        uavMission.uavMissionType = UAV_DISASTER_MANAGEMENT;
    } else if(missionType == STR_UAV_SURVEILLANCE) {
        uavMission.uavMissionType = UAV_SURVEILLANCE;
    }

//    UAV Configs
    int nUAV;
    QString uavEnd;
    in >> nUAV;

    for(int i = 0; i < nUAV; ++i) {
        UAVMissionConfig* uavMC = new UAVMissionConfig();
        in >> *uavMC;
        in >> uavEnd;

        if(uavEnd == UAV_END) {
            uavMission.addUAV (uavMC);
        }
    }

    return in;
}
