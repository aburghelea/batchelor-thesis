#!/bin/bash

qgroundcontrol &
java -jar router.jar

kill -9 $(ps aux | grep -v grep| grep router.jar | awk -v x=2 '{print $x}')
kill -9 $(ps aux | grep -v grep| grep caninterface.jar | awk -v x=2 '{print$x}')
kill -9 $(ps aux | grep -v grep| grep fgfs | awk -v x=2 '{print $x}')
kill -9 $(ps aux | grep -v grep| grep qgroundcontrol | awk -v x=2 '{print $x}')

