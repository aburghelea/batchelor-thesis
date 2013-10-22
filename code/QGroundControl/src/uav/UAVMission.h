#ifndef UAVMISSION_H
#define UAVMISSION_H

#include "UAVMissionConfig.h"

#define STR_UAV_MAPPING "UAV_MAPPING"
#define STR_UAV_SURVEILLANCE "UAV_SURVEILLANCE"
#define STR_UAV_DISASTER_MANAGEMENT "UAV_DISASTER_MANAGEMENT"

#define UAV_END "UAV_END"

enum UAVMissionType
{
    UAV_MAPPING,
    UAV_SURVEILLANCE,
    UAV_DISASTER_MANAGEMENT
};

class UAVMission
{
private:
    QVector<UAVMissionConfig*> uavMissionConfigs;
    UAVMissionType uavMissionType;

public:
    UAVMission();

    int UAVcount();
    void addUAV();
    void addUAV(UAVMissionConfig* uav);
    void removeUAV();

    ~UAVMission();

    friend class UAVMissionControlWidget;
    friend QTextStream& operator<< (QTextStream& out, UAVMission& uavMission);
    friend QTextStream& operator>> (QTextStream& in, UAVMission& uavMission);
};


//QTextStream& operator<< (QTextStream& out, UAVMission& uavMission);
//QTextStream& operator>> (QTextStream& in, UAVMission& uavMission);

#endif // UAVMISSION_H
