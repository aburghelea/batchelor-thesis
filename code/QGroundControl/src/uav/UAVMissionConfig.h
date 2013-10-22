#ifndef UAVMISSIONCONFIG_H
#define UAVMISSIONCONFIG_H

#include <QVector>
#include <QVectorIterator>
#include <QColor>
#include <QTextStream>

#include <Waypoint.h>
#include <UASWaypointManager.h>

#define STR_CAMERA "CAMERA"
#define STR_INFRARED "INFRARED"
#define STR_CUSTOM "CUSTOM"

enum UAVEquipment
{
    CAMERA,
    INFRARED,
    CUSTOM
};

class UAVMissionConfig
{
private:


public:
    int uavId;
    //bool uavLeader;
    QString uavRole;
    QVector<UAVEquipment> uavEquipment;
    QColor uavColor;
    QVector<Waypoint*> uavArea;
    UASWaypointManager* uavWaypointManager;
    UAVMissionConfig();
    UAVMissionConfig(int _uavId);

    void setColor(QColor color);

    ~UAVMissionConfig();

    friend class UAVMission;
    friend class UAVMissionControlWidget;

    friend QTextStream& operator<< (QTextStream& out, UAVMissionConfig& uavMC);
    friend QTextStream& operator>> (QTextStream& in, UAVMissionConfig& uavMC);
};



#endif // UAVMISSIONCONFIG_H
