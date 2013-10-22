#!/bin/bash


kill -9 $(ps aux | grep -v grep| grep caninterface.jar | awk -v x=2 '{print $x}')
java -jar caninterface.jar 5510$1 5500$1 3500$1 $1 &

