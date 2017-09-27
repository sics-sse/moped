#!/usr/bin/python

# MIT License
# 
# Copyright (c) 2017 John Bryan Moore
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import time
import VL53L0X

import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BOARD)

pins0 = [13, 15, 12, 16, 11]
pins0 = [13, 15, 12, 16]
pins0 = [13, 15, 12]
pins0 = [13, 12, 16]
pins0 = [16, 12, 13]
pins = pins0
adrs = [0x21, 0x22, 0x23, 0x24, 0x25]
adrs = [0x21, 0x22, 0x23, 0x24]
adrs = [0x21, 0x22, 0x23]
#adrs = [0x21, 0x22]
tofs = []

#mode = VL53L0X.VL53L0X_LONG_RANGE_MODE
mode = VL53L0X.VL53L0X_HIGH_SPEED_MODE
#mode = VL53L0X.VL53L0X_BETTER_ACCURACY_MODE
#mode = VL53L0X.VL53L0X_GOOD_ACCURACY_MODE

timing = None

def setup():
    global timing
    for p in pins0:
        GPIO.setup(p, GPIO.OUT)
        GPIO.output(p, False)

    for i in range(0, len(pins)):
        p = pins[i]
        a = adrs[i]

        GPIO.output(p, True)
        time.sleep(0.1)
        tofs.append(VL53L0X.VL53L0X(address=a))
        tofs[i].start_ranging(mode)

    #exit(0)
    timing = tofs[0].get_timing()
    if (timing < 20000):
        timing = 20000
    print ("Timing %d ms" % (timing/1000))

def scan(arr):
    global timing
    while True:
        t = time.time()
        s = "%15.3f" % t
        for i in range(0, len(pins)):

            distance = tofs[i].get_distance()
            if (distance > 0 and distance < 8190):
                pass
            else:
                distance = -1
            arr[i] = distance
            #s += "%5d" % distance

        #print(s)
        time.sleep(timing/(10*100000.00))

#tof.stop_ranging()


if __name__ == "__main__":
    setup()
    scan()
