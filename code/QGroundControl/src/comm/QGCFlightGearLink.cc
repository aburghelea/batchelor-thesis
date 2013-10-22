/*=====================================================================

QGroundControl Open Source Ground Control Station

(c) 2009 - 2011 QGROUNDCONTROL PROJECT <http://www.qgroundcontrol.org>

This file is part of the QGROUNDCONTROL project

    QGROUNDCONTROL is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    QGROUNDCONTROL is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with QGROUNDCONTROL. If not, see <http://www.gnu.org/licenses/>.

======================================================================*/

/**
 * @file
 *   @brief Definition of UDP connection (server) for unmanned vehicles
 *   @see Flightgear Manual http://mapserver.flightgear.org/getstart.pdf
 *   @author Lorenz Meier <mavteam@student.ethz.ch>
 *
 */

#include <QTimer>
#include <QList>
#include <QDebug>
#include <QMutexLocker>
#include <iostream>
#include "QGCFlightGearLink.h"
#include "QGC.h"
#include <QHostInfo>
#include "MainWindow.h"

QGCFlightGearLink::QGCFlightGearLink(UASInterface* mav, QString remoteHost, QHostAddress host, quint16 port)
{
    this->host = host;

    this->port = port;
    this->connectState = false;
    this->currentPort = port;

    this->mav = mav;
    this->name = tr("FlightGear Link (port:%1)").arg(port);
    setRemoteHost(remoteHost);

}

QGCFlightGearLink::~QGCFlightGearLink()
{
    disconnectSimulation();
}

/**
 * @brief Runs the thread
 *
 **/
void QGCFlightGearLink::run()
{
    exec();
}

void QGCFlightGearLink::setPort(int port)
{
    this->port = port;
    if (connectState){
        disconnectSimulation();
        connectSimulation();
    }
}

void QGCFlightGearLink::processError(QProcess::ProcessError err)
{
    switch(err)
    {
    case QProcess::FailedToStart:
        MainWindow::instance()->showCriticalMessage(tr("FlightGear Failed to Start"), tr("Please check if the path and command is correct"));
        break;
    case QProcess::Crashed:
        MainWindow::instance()->showCriticalMessage(tr("FlightGear Crashed"), tr("This is a FlightGear-related problem. Please upgrade FlightGear"));
        break;
    case QProcess::Timedout:
        MainWindow::instance()->showCriticalMessage(tr("FlightGear Start Timed Out"), tr("Please check if the path and command is correct"));
        break;
    case QProcess::WriteError:
        MainWindow::instance()->showCriticalMessage(tr("Could not Communicate with FlightGear"), tr("Please check if the path and command is correct"));
        break;
    case QProcess::ReadError:
        MainWindow::instance()->showCriticalMessage(tr("Could not Communicate with FlightGear"), tr("Please check if the path and command is correct"));
        break;
    case QProcess::UnknownError:
    default:
        MainWindow::instance()->showCriticalMessage(tr("FlightGear Error"), tr("Please check if the path and command is correct."));
        break;
    }
}

/**
 * @param host Hostname in standard formatting, e.g. localhost:14551 or 192.168.1.1:14551
 */
void QGCFlightGearLink::setRemoteHost(const QString& host)
{
    if (host.contains(":"))
    {
        //qDebug() << "HOST: " << host.split(":").first();
        QHostInfo info = QHostInfo::fromName(host.split(":").first());
        if (info.error() == QHostInfo::NoError)
        {
            // Add host
            QList<QHostAddress> hostAddresses = info.addresses();
            QHostAddress address;
            for (int i = 0; i < hostAddresses.size(); i++)
            {
                // Exclude loopback IPv4 and all IPv6 addresses
                if (!hostAddresses.at(i).toString().contains(":"))
                {
                    address = hostAddresses.at(i);
                }
            }
            currentHost = address;
            currentPort = host.split(":").last().toInt();
        }
    }
    else
    {
        QHostInfo info = QHostInfo::fromName(host);
        if (info.error() == QHostInfo::NoError)
        {
            // Add host
            currentHost = info.addresses().first();
        }
    }
}

void QGCFlightGearLink::writeBytes(const char* data, qint64 size)
{
    //#define QGCFlightGearLink_DEBUG
#ifdef QGCFlightGearLink_DEBUG
    QString bytes;
    QString ascii;
    for (int i=0; i<size; i++)
    {
        unsigned char v = data[i];
        bytes.append(QString().sprintf("%02x ", v));
        if (data[i] > 31 && data[i] < 127)
        {
            ascii.append(data[i]);
        }
        else
        {
            ascii.append(219);
        }
    }
    qDebug() << "Sent" << size << "bytes to" << currentHost.toString() << ":" << currentPort << "data:";
    qDebug() << bytes;
    qDebug() << "ASCII:" << ascii;
#endif
    if (connectState && socket) socket->writeDatagram(data, size, currentHost, currentPort);
}

/**
 * @brief Read a number of bytes from the interface.
 *
 * @param data Pointer to the data byte array to write the bytes to
 * @param maxLength The maximum number of bytes to write
 **/
void QGCFlightGearLink::readBytes()
{
    const qint64 maxLength = 65536;
    char data[maxLength];
    QHostAddress sender;
    quint16 senderPort;

    unsigned int s = socket->pendingDatagramSize();
    if (s > maxLength) std::cerr << __FILE__ << __LINE__ << " UDP datagram overflow, allowed to read less bytes than datagram size" << std::endl;
    socket->readDatagram(data, maxLength, &sender, &senderPort);

    QByteArray b(data, s);

    // Print string
    QString state(b);
    //qDebug() << "FG LINK GOT:" << state;

    QStringList values = state.split("\t");

    // Parse string
    double time;
    float roll, pitch, yaw, rollspeed, pitchspeed, yawspeed;
    double lat, lon, alt;
    double vx, vy, vz;
    double airspeed;
    int currentwp;
    QString rmgrStatus;


    if (values.count() >= 19) {
        time = values.at(0).toDouble();
        lat = values.at(1).toDouble();
        lon = values.at(2).toDouble();
        alt = values.at(3).toDouble();
        roll = values.at(4).toDouble();
        pitch = values.at(5).toDouble();
        yaw = values.at(6).toDouble();
        rollspeed = values.at(7).toDouble();
        pitchspeed = values.at(8).toDouble();
        yawspeed = values.at(9).toDouble();

        vx = values.at(10).toDouble();
        vy = values.at(11).toDouble();
        vz = values.at(12).toDouble();

        airspeed = values.at(13).toDouble();

        currentwp = values.at(14).toInt();

        rmgrStatus = values.at(15);

        this->targetHeadValue = values.at(16).toDouble();
        this->targetAltitudeValue = values.at(17).toDouble();
        this->targetSpeedValue = values.at(18).toDouble();

        emit hilStateChanged(QGC::groundTimeUsecs(), roll, pitch, yaw, rollspeed,
                             pitchspeed, yawspeed, lat, lon, alt,
                             vx, vy, vz, airspeed, currentwp, rmgrStatus);

    }
}


/**
 * @brief Get the number of bytes to read.
 *
 * @return The number of bytes to read
 **/
qint64 QGCFlightGearLink::bytesAvailable()
{
    return socket->pendingDatagramSize();
}

/**
 * @brief Disconnect the connection.
 *
 * @return True if connection has been disconnected, false if connection couldn't be disconnected.
 **/
bool QGCFlightGearLink::disconnectSimulation()
{
    disconnect(this, SIGNAL(hilStateChanged(uint64_t,float,float,float,float,float,float,float,float,float,float,float,float,float,int,QString)), dynamic_cast<UAS*>(mav), SLOT(sendHilState(uint64_t,float,float,float,float,float,float,float,float,float,float,float,float,float,int,QString)));

     if (socket)
    {
        socket->close();
        delete socket;
        socket = NULL;
    }

    connectState = false;

    emit flightGearDisconnected();
    emit flightGearConnected(false);
    return !connectState;
}

/**
 * @brief Connect the connection.
 *
 * @return True if connection has been established, false if connection couldn't be established.
 **/
bool QGCFlightGearLink::connectSimulation()
{
    if (!mav) return false;
    socket = new QUdpSocket(this);
    connectState = socket->bind(host, port);
    qDebug() << "Host " << host << "\n";
    QObject::connect(socket, SIGNAL(readyRead()), this, SLOT(readBytes()));


    connect(this, SIGNAL(hilStateChanged(uint64_t,float,float,float,float,float,float,float,float,float,float,float,float,float,int,QString)), dynamic_cast<UAS*>(mav), SLOT(sendHilState(uint64_t,float,float,float,float,float,float,float,float,float,float,float,float,float,int,QString)));

    emit flightGearConnected(connectState);
    if (connectState) {
        emit flightGearConnected();
        connectionStartTime = QGC::groundTimeUsecs()/1000;
    }

    start(LowPriority);
    return connectState;
}

/**
 * @brief Check if connection is active.
 *
 * @return True if link is connected, false otherwise.
 **/
bool QGCFlightGearLink::isConnected()
{
    return connectState;
}

QString QGCFlightGearLink::getName()
{
    return name;
}

void QGCFlightGearLink::setName(QString name)
{
    this->name = name;
}
