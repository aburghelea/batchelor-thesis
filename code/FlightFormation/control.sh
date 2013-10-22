#!/bin/bash

CMD="./ai_manager 192.168.1.133 3500$1 initial_commands formation.json $1"
echo $CMD

$CMD
