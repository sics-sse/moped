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
