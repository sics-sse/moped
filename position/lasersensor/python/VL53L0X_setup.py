#!/usr/bin/python

import time
import VL53L0X

import sys

import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setup(22, GPIO.OUT)
GPIO.output(22,False)
GPIO.setup(27, GPIO.OUT)
GPIO.output(27,False)

GPIO.output(27,True)
time.sleep(1)
tof = VL53L0X.VL53L0X(0x21)
tof.start_ranging(VL53L0X.VL53L0X_BETTER_ACCURACY_MODE)

GPIO.output(22,True)
time.sleep(1)
tof = VL53L0X.VL53L0X(0x22)
tof.start_ranging(VL53L0X.VL53L0X_BETTER_ACCURACY_MODE)
