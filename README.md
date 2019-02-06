# MoePi
Vision software for offboard vision processing using a Raspberry Pi 3 using Java utilizing Pi4J and V4L4J over UDP for the FIRST Robotics Competition (FRC).

## Big Thanks to MOE 365!
Everything under [moe.js/](https://github.com/MOERobotics/moe.js) and [src/](https://github.com/MOERobotics/stronghold-pi-2016) belong to [MOE](http://moe365.org/) with only slight modifications from us.

## Features and Technique
See [this README](https://github.com/MOERobotics/stronghold-pi-2016/blob/master/README.md) by MOE for specifics on what the code features and how the image processing works.

## What You'll Need
1. Raspberry Pi 3 Model B/B+ running [Raspbian Stretch](https://www.raspberrypi.org/downloads/raspbian/) and Java 8 (via `sudo apt-get update` then `sudo apt-get install -y openjdk-8-jdk`)
2. Internet Connection for the Pi (initially, for `install_dependencies.sh` and the first `./gradlew build`)
3. USB Camera (tested with [Microsoft LifeCam HD-3000](https://www.amazon.com/Microsoft-3364820-LifeCam-HD-3000/dp/B008ZVRAQS))
4. [LED Halo (Green, tested with 60mm)](https://www.superbrightleds.com/moreinfo/led-halo-rings/led-halo-angel-eye-headlight-accent-lights/49/307/)
5. [MOSFET](https://www.amazon.com/WINGONEER-IRF520-MOSFET-Driver-Module/dp/B06XHH1TQM) to control the LED Halo
6. [3M Scotchlite Reflective Tape 8830](https://www.andymark.com/products/reflective-material-3m-2-in-wide-x-21-5-adhesive-backed)

## Installing The Necessary Dependencies
On the Pi, run the install dependencies shell script file via `./install_dependencies.sh`. This will install [WiringPi](http://wiringpi.com/) to control the Pi's GPIO pins (e.g., the LED Halo) and [V4L4J](https://github.com/mailmindlin/v4l4j) to control the camera.

## Building and Running on the Pi
If you are on the pi, you can use `./gradlew run` to run the code directly. You can also run `./gradlew build` to run a build. When doing this, the output files will be placed into `output/`. From there, you can run the shell script via `chmod +x runMoePi` then `./runMoePi`, this will start MoePi with the default configuration values. To see what values you can change/pass in, run `java -Djava.library.path=. -jar MoePi-all.jar --help` within `output/`.

## Viewing the Camera Feed
When running the code in the `output/` folder via `./runMoePi` after building the project via `./gradlew build`, you can visit `http://your-pi's-ip-address:5800` in a web browser to view the camera feed from the Pi.

## Setting up MoePi to Run on Boot
For MoePi to run on boot, it must be built under `/home/pi/MoePi` with the built `output/` directory via `./gradlew build` (unless you create your own service file and configure it for systemctl yourself). You can then use the script `service_manager.sh`:
```
./service_manager.sh argument

install - installs MoePi service and enables start on boot
start - starts MoePi via systemctl (service must be installed)
stop - stops MoePi via systemctl (service must be installed)
enable - enables MoePi to start on boot (service must be installed)
disable - disables MoePi from starting on boot (service must be installed)
uninstall - completely removes MoePi service from systemctl
```

## Generating the .classpath for your IDE/Editor
Run `./gradlew eclipse`
