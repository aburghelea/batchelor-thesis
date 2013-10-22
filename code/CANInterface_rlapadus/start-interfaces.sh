#!/bin/bash
END=$1

if [ $# -le 0 ]
then
  END=2
fi
  
for i in $(seq 1 $END)
do
  echo "Welcome $i"
  java -jar caninterface.jar 5510$i 5500$i 3500$i $i >> /dev/null  &
done
