#!/bin/bash

if [ "$1" = "install" ]
then
    echo "Adding MoePi service to systemctl..."
    sudo cp MoePi.service /etc/systemd/system/MoePi.service
    sudo systemctl enable MoePi.service
    echo "Done."
elif [ "$1" = "start" ]
then
    echo "Starting MoePi service..."
    sudo systemctl start MoePi.service
elif [ "$1" = "stop" ]
then
    echo "Stopping MoePi service..."
    sudo systemctl stop MoePi.service
elif [ "$1" = "enable" ]
then
    echo "Enabling MoePi service to start on boot..."
    sudo systemctl enable MoePi.service
elif [ "$1" = "disable" ]
then
    echo "Disabling MoePi service from starting on boot..."
    sudo systemctl disable MoePi.service
elif [ "$1" = "uninstall" ]
then
    echo "Removing MoePi service from systemctl..."
    sudo systemctl stop MoePi.service
    sudo systemctl disable MoePi.service
    sudo rm -f /etc/systemd/system/MoePi.service
    sudo systemctl daemon-reload
elif [ "$1" = "help" ]
then
    echo ""
    echo "./service_manager.sh argument"
    echo ""
    echo "install - installs MoePi service and enables start on boot"
    echo "start - starts MoePi via systemctl (service must be installed)"
    echo "stop - stops MoePi via systemctl (service must be installed)"
    echo "enable - enables MoePi to start on boot (service must be installed)"
    echo "disable - disables MoePi from starting on boot (service must be installed)"
    echo "uninstall - completely removes MoePi service from systemctl"
    echo ""
else
    echo "Invalid option. Options are help, install, uninstall, enable, disable, start, stop."
fi
