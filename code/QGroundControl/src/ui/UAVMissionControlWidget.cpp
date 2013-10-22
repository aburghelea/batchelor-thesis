#include "UAVMissionControlWidget.h"
#include "ui_UAVMissionControlWidget.h"

#include <cstdio>
#include <iostream>

UAVMissionControlWidget::UAVMissionControlWidget(QWidget *parent) :
    QTabWidget(parent),
    ui(new Ui::UAVMissionControlWidget)
{
    if(UAVColors.isEmpty ()) {
        UAVColors.push_back ("#FF0000");
        UAVColors.push_back ("#0015FF");
        UAVColors.push_back ("#1EFF00");
        UAVColors.push_back ("#FFF200");
        UAVColors.push_back ("#FF00DD");
        UAVColors.push_back ("#FF9100");
    }

    ui->setupUi(this);
    ui->tabRealTime->setEnabled(false);

    ui->tableWidgetMissionOverview->insertRow (0);

    uavMission = new UAVMission();

    uavMission->uavMissionConfigs[0]->setColor(UAVColors[0]);

    fillUAVData(0);

    ui->labelMissionOverview->setText (ui->comboBoxMissionType->currentText ());

    editingArea = false;
}

UAVMissionControlWidget::~UAVMissionControlWidget()
{
    delete ui;
    delete uavMission;
}

void UAVMissionControlWidget::on_spinBoxNumUAV_valueChanged(int arg1)
{
    qDebug () << "UAV added or removed";

    //update UI

    // UAV equipment combo
    if(arg1 < ui->comboBoxEquipment->currentIndex ()) {
        ui->comboBoxEquipment->setCurrentIndex (arg1-1);
        updateEquipmentList (arg1-1);
    }

    if(arg1 > uavMission->UAVcount ()) {
        for(int i = uavMission->UAVcount (); i < arg1; ++i) {
            char uav_name[10];
            sprintf(uav_name, "UAV%d", i);
            ui->comboBoxEquipment->addItem(QString(uav_name));
        }
    }

    QGCMapWidget* mapWidget = static_cast<QGCMapTool*>
            (MainWindow::instance ()->getMapTool ().data ())
            ->findChild<QGCMapWidget*>("map");

    if(arg1 < uavMission->UAVcount ()) {
        for(int i = arg1; i < uavMission->UAVcount (); ++i) {
            ui->comboBoxEquipment->removeItem (arg1);

            UASWaypointManager* manager =
                    uavMission->uavMissionConfigs[arg1]->uavWaypointManager;
            mapWidget->removeWaypointList (manager->waypointsEditable);
        }
    }

    // enable combos for more UAVs
    if(arg1 > 1) {
        ui->comboBoxEquipment->setEnabled(true);
    }


    // update mission
    // UAV added
    if(arg1 > uavMission->UAVcount()) {
        for(int i = uavMission->UAVcount (); i < arg1; ++i) {
            uavMission->addUAV();
            uavMission->uavMissionConfigs[i]->setColor(UAVColors[i]);
        }
    }

    // UAV removed
    if(arg1 < uavMission->UAVcount ()) {
        for(int i = arg1; i < uavMission->UAVcount (); ++i) {
            uavMission->removeUAV();
        }
    }


    // update table
    if(arg1 > ui->tableWidgetMissionOverview->rowCount ()) {
        for(int i = ui->tableWidgetMissionOverview->rowCount (); i < arg1; ++i) {
            ui->tableWidgetMissionOverview->insertRow (i);
            fillUAVData(i);
        }
    }

    if(arg1 < ui->tableWidgetMissionOverview->rowCount ()) {
        for(int i = arg1; i < ui->tableWidgetMissionOverview->rowCount (); ++i) {
            ui->tableWidgetMissionOverview->removeRow (arg1);
        }
    }


}

void UAVMissionControlWidget::on_comboBoxEquipment_currentIndexChanged(int index)
{
    updateEquipmentList (index);

    ui->lineEditRole->setText (uavMission->uavMissionConfigs[index]->uavRole);

    if(editingArea) {
        QGCMapWidget* mapWidget = static_cast<QGCMapTool*>
                (MainWindow::instance ()->getMapTool ().data ())
                ->findChild<QGCMapWidget*>("map");
        mapWidget->setWaypointManager
                (uavMission->uavMissionConfigs[index]->uavWaypointManager);
    }

}

void UAVMissionControlWidget::on_listWidgetEquipment_itemClicked(QListWidgetItem *item)
{
    QListIterator<QListWidgetItem*> itEquipmentList
            (ui->listWidgetEquipment->selectedItems ());
    uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]
            ->uavEquipment.clear ();

    while(itEquipmentList.hasNext ()) {
        int indexEquiment = indexOfEquipment (itEquipmentList.next ());
        uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]
                ->uavEquipment.push_back ((UAVEquipment)indexEquiment);
    }

    fillUAVData (ui->comboBoxEquipment->currentIndex ());
}

int UAVMissionControlWidget::indexOfEquipment(QListWidgetItem* itemEquipment) {
    int itemIndex = 0;
    QListIterator<QListWidgetItem*> itEquipmentList
            (ui->listWidgetEquipment->findItems
             (QString("*"), Qt::MatchWrap | Qt::MatchWildcard));

    while(itEquipmentList.hasNext ()) {
        if(itemEquipment == itEquipmentList.next ()) {
            break;
        }
        ++itemIndex;
    }

    return itemIndex;
}

void UAVMissionControlWidget::updateEquipmentList(int index) {
    //update corresponding available equipment
    QVectorIterator<UAVEquipment> itUAVEquipment
            (uavMission->uavMissionConfigs[index]->uavEquipment);

    for(int i = 0; i < ui->listWidgetEquipment->count (); ++i) {
        ui->listWidgetEquipment->item (i)->setSelected (false);
    }

    while(itUAVEquipment.hasNext ()) {
        int indexEquip = itUAVEquipment.next ();
        ui->listWidgetEquipment->item (indexEquip)->setSelected (true);
    }
}

void UAVMissionControlWidget::fillUAVData(int uavIndex) {
    //name color equipment role

    // name
    char uavName[10];
    sprintf(uavName, "UAV%d", uavIndex);
    ui->tableWidgetMissionOverview->setItem
            (uavIndex, 0, new QTableWidgetItem(uavName));

    // color
    QString uavColor = uavMission->uavMissionConfigs[uavIndex]->uavColor.name ();
    QTableWidgetItem* colorItem = new QTableWidgetItem("");
    colorItem->setBackgroundColor (uavColor);
    ui->tableWidgetMissionOverview->setItem (uavIndex, 1, colorItem);


    // equipment
    QString uavEquipment = "";
    QVectorIterator<UAVEquipment> itUAVEquipment
            (uavMission->uavMissionConfigs[uavIndex]->uavEquipment);
    while(itUAVEquipment.hasNext ()) {
        switch(itUAVEquipment.next ()) {
        case CAMERA:
            uavEquipment += "Camera; ";
            break;
        case INFRARED:
            uavEquipment += "Infrared; ";
            break;
        default:
            uavEquipment += "Custom payload; ";
        }
    }
    ui->tableWidgetMissionOverview->setItem (uavIndex, 2, new QTableWidgetItem(uavEquipment));

    // role
//    QString uavLeader = uavMission->uavMissionConfigs[uavIndex]->uavLeader ? "x" : "";
    ui->tableWidgetMissionOverview->setItem
            (uavIndex, 3, new QTableWidgetItem(uavMission->uavMissionConfigs[uavIndex]->uavRole));
}

void UAVMissionControlWidget::on_comboBoxMissionType_currentIndexChanged(const QString &arg1)
{
    ui->labelMissionOverview->setText (arg1);
}

void UAVMissionControlWidget::on_pushButtonSave_clicked()
{
    QString fname = getenv("HOME");
    fname.append (DEFAULT_MISSION_FILE);

    QFile outFile(fname);

    if(outFile.open (QFile::Text | QFile::WriteOnly)) {
        QTextStream textStream(&outFile);
        textStream << *uavMission << flush;
        outFile.close ();
    } else {
        qDebug () << "Failed to open file " << fname <<" !";
    }
}

void UAVMissionControlWidget::on_pushButtonArea_clicked()
{
    editingArea = !editingArea;

    QGCMapWidget* mapWidget = static_cast<QGCMapTool*>(MainWindow::instance ()->getMapTool ().data ())->findChild<QGCMapWidget*>("map");

    if(editingArea) {
        mapWidget->setWaypointManager (uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]->uavWaypointManager);
        ui->pushButtonArea->setText ("Done");
    } else {
        mapWidget->setWaypointManager (NULL);
        ui->pushButtonArea->setText ("Define Area");
    }
}

void UAVMissionControlWidget::on_pushButtonAreaClear_clicked()
{
    QGCMapWidget* mapWidget = static_cast<QGCMapTool*>(MainWindow::instance ()->getMapTool ().data ())->findChild<QGCMapWidget*>("map");

    UASWaypointManager* manager = uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]->uavWaypointManager;

    mapWidget->removeWaypointList (manager->waypointsEditable);
    uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]->uavArea.clear ();
}

void UAVMissionControlWidget::on_lineEditRole_returnPressed()
{
    uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]->uavRole = ui->lineEditRole->text ();
    fillUAVData (ui->comboBoxEquipment->currentIndex ());
}

void UAVMissionControlWidget::on_lineEditRole_textChanged(const QString &arg1)
{
    uavMission->uavMissionConfigs[ui->comboBoxEquipment->currentIndex ()]->uavRole = arg1;
    fillUAVData (ui->comboBoxEquipment->currentIndex ());
}
