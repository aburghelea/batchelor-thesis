#ifndef UAVMISSIONCONTROLWIDGET_H
#define UAVMISSIONCONTROLWIDGET_H

#include <QTabWidget>
#include <QListWidgetItem>
#include <QFile>

#include <cstdlib>

#include "uav/UAVMission.h"

#include "MainWindow.h"
#include "QGCMapWidget.h"
#include "QGCMapTool.h"

#define DEFAULT_MISSION_FILE "/Desktop/mission.uav"

namespace Ui {
class UAVMissionControlWidget;
}

class UAVMissionControlWidget : public QTabWidget
{
    Q_OBJECT

public:
    QVector<QString> UAVColors;

    explicit UAVMissionControlWidget(QWidget *parent = 0);
    ~UAVMissionControlWidget();

private slots:
    void on_spinBoxNumUAV_valueChanged(int arg1);

    //void on_comboBoxLeader_currentIndexChanged(int index);

    void on_comboBoxEquipment_currentIndexChanged(int index);

    void on_listWidgetEquipment_itemClicked(QListWidgetItem *item);

    void on_comboBoxMissionType_currentIndexChanged(const QString &arg1);

    void on_pushButtonSave_clicked();

    void on_pushButtonArea_clicked();

    void on_pushButtonAreaClear_clicked();

    void on_lineEditRole_returnPressed();

    void on_lineEditRole_textChanged(const QString &arg1);

private:
    Ui::UAVMissionControlWidget *ui;
    UAVMission* uavMission;

    int indexOfEquipment(QListWidgetItem* itemEquipment);
    void updateEquipmentList(int index);
    void fillUAVData(int uavIndex);
    bool editingArea;
};

#endif // UAVMISSIONCONTROLWIDGET_H
