#!/bin/bash

# Installs MoePi Dependencies for the Pi

# Install WiringPi
echo "Installing WiringPi..."
sudo apt-get update
sudo apt-get install -y wiringpi
gpio
echo "Finished Installing WiringPi"

# Install V4L4J
echo "Installing V4L4J..."
sudo apt-get install -y libjpeg-dev build-essential ant libv4l-dev
cd
git clone https://github.com/mailmindlin/v4l4j.git
cd v4l4j
ant all
sudo ant install
sudo ldconfig
cd
rm -rf v4l4j
echo "Finished Installing V4L4J"

echo "Finished Installing Dependencies for MoePi."
