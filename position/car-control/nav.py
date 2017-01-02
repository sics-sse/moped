import re
import smbus
import time
import threading
import os
import subprocess
import math
import socket
import sys
import ast
import json

from godircalc import godir

import eight
from eight import *

import random

from math import pi, cos, sin, sqrt, atan2, acos, asin

import urllib
import urllib.parse
import paho.mqtt.client as mosquitto

def start_new_thread(f, args):
    threading.Thread(target=f, args=args, daemon=True).start()


class globals:
    pass

g = globals()

g.VIN = None
#g.VIN = sys.argv[1]

g.parameter = 164
g.parameter = 100
g.parameter = 152
g.parameter = 120

g.allangles = False

# setting this to True is the same as setting goodmarkers to []?
g.ignoremarkers = False

g.detectcrashes = False
g.detectcrashes = True

g.ledcmd = None

# Set, but not used yet
g.section_status = dict()

g.warningblinking = None

#g.oldpos = dict()
g.oldpos = None
g.adjust_t = None

g.paused = False

g.speedtime = None

g.speakcount = 1

g.markerno = 0
g.markercnt = 0

g.speedsign = 1
g.braking = False

g.mxmin = None
g.mxmax = None
g.mymin = None
g.mymax = None

g.currentbox = None

g.angleknown = False
g.inspeed = 0.0
g.finspeed = 0.0
g.inspeed_avg = 0.0
g.odometer = 0
g.fodometer = 0
g.lastodometer = None
g.age = -1

g.totals = 0

g.limitspeed = None
g.limitspeed0 = "notset"

g.can_steer = 0
g.can_speed = 0

g.last_send = None

g.send_sp = None
g.send_st = None

g.dstatus = 0

g.ground_control = None

g.rc_button = False
g.remote_control = False

g.targetx = None
g.targety = None

#TARGETDIST = 0.3
TARGETDIST = 0.15
TARGETDIST = 0.25
TOOHIGHSPEED = 2.0

R = 0.83

YMAX = 19.7

global logf
global accf

bus = smbus.SMBus(1)

address = 0x68

def Write_Sensor(reg, val):
    bus.write_byte_data(address, reg, val)

def imuinit0():
    smbusinit = False

    for i in range(0, 3):
        try:
            bus.write_byte_data(address, 0x6b, 0)
            smbusinit = True
        except Exception as e:
            print(e)

    if not smbusinit:
        print("couldn't init IMU")
        exit(0)

imuinit0()

bus.read_byte_data(address, 0x75)

#bus.write_byte_data(address, 0x1a, 5)
#bus.write_byte_data(address, 0x1b, 0)

bus.write_byte_data(address, 0x1a, 1)
bus.write_byte_data(address, 0x1b, 16)

MPU9150_SMPLRT_DIV = 0x19 # 25
MPU9150_CONFIG = 0x1a # 26
MPU9150_GYRO_CONFIG = 0x1b
MPU9150_ACCEL_CONFIG = 0x1c
MPU9150_FIFO_EN = 0x23
MPU9150_I2C_MST_CTRL = 0x24
MPU9150_I2C_SLV0_ADDR = 0x25
MPU9150_I2C_SLV0_REG = 0x26
MPU9150_I2C_SLV0_CTRL = 0x27
MPU9150_I2C_SLV1_ADDR = 0x28
MPU9150_I2C_SLV1_REG = 0x29
MPU9150_I2C_SLV1_CTRL = 0x2a
MPU9150_I2C_SLV1_DO = 0x64
MPU9150_I2C_MST_DELAY_CTRL = 0x67 # 103
MPU9150_I2C_SLV4_CTRL = 0x34 # 52
MPU9150_USER_CTRL = 0x6a #106

def sleep(x):
    if True:
        time.sleep(x)

def imuinit():

    bus.write_byte_data(address, 0x6b, 0x80)
    sleep(0.1)
    bus.write_byte_data(address, 0x6b, 0)

    b = bus.read_byte_data(address, 0x49)
    print("read byte %#x" % b)

    sleep(0.1)
    Write_Sensor(MPU9150_I2C_SLV0_ADDR, 0x8C);
    b = bus.read_byte_data(address, 0x49)
    print("read byte %#x" % b)

    sleep(0.1)
    Write_Sensor(MPU9150_I2C_SLV0_CTRL, 0x88);
    sleep(0.1)
    b = bus.read_byte_data(address, 0x49)
    print("read byte %#x" % b)

    Write_Sensor(MPU9150_USER_CTRL, 0x20);
    sleep(0.1)

    while True:
        b = bus.read_byte_data(address, 0x49)
        print("read byte %#x" % b)
        if b == 0x48:
            break

    if True:
        # this did the trick:
        Write_Sensor(MPU9150_CONFIG, 0x02);
        # maybe important:
        Write_Sensor(MPU9150_GYRO_CONFIG, 0x08);
        sleep(0.1)

        Write_Sensor(MPU9150_SMPLRT_DIV, 0x7);
        sleep(0.1)

        Write_Sensor(MPU9150_I2C_SLV1_ADDR, 0x0C);
        sleep(0.1)
        # Set where reading at slave 1 starts
        Write_Sensor(MPU9150_I2C_SLV1_REG, 0x0A);
        sleep(0.1)
        # Enable at set length to 1
        Write_Sensor(MPU9150_I2C_SLV1_CTRL, 0x81);
        sleep(0.1)

        # overvride register
        Write_Sensor(MPU9150_I2C_SLV1_DO, 0x01);
        sleep(0.1)

        # set delay rate
        Write_Sensor(MPU9150_I2C_MST_DELAY_CTRL, 0x03);
        sleep(0.1)
        # set i2c slv4 delay
        Write_Sensor(MPU9150_I2C_SLV4_CTRL, 0x04);
        sleep(0.1)



imuinit()

bus.write_byte_data(address, MPU9150_CONFIG, 1)
bus.write_byte_data(address, MPU9150_GYRO_CONFIG, 16)

# px etc. is the dead reckoning from the IMU acc and gyro
# ppx etc. is the dead reckoning from wheel speed and gyro
g.px = None
g.py = None
g.pz = None

g.ppx = None
g.ppy = None
g.ppz = None

g.vx = None
g.vy = None
g.vz = None

g.crash = False
g.crashacc = None

g.ppx = 0
g.ppy = 0

g.angdiff = 0.0
g.ppxdiff = 0.0
g.ppydiff = 0.0
g.ang = 0.0

g.rbias = 0
g.rxbias = 0
g.rybias = 0

g.xbias = 0
g.ybias = 0
g.zbias = 0

g.t0 = None

def make_word(high, low):
    x = high*256+low
    if x >= 32768:
        x -= 65536
    return x

def make_word2(high, low):
    x = high*256+low
    return x

def readgyro():
    while True:
        tolog("starting readgyro")
        readgyro0()

def readgyro0():
    #gscale = 32768.0/250
    gscale = 32768.0/1000
    ascale = 1670.0

    x = 0.0
    y = 0.0
    x0 = 0.0
    y0 = 0.0
    z0 = 0.0
    rx = 0.0
    ry = 0.0
    acc = 0.0

    try:

        tlast = time.time()
        t1 = time.time()

        while True:
            w = bus.read_i2c_block_data(address, 0x47, 2)
            high = w[0]
            low = w[1]
            r = make_word(high, low)

            r -= g.rbias

            if True:
                high = bus.read_byte_data(address, 0x45)
                low = bus.read_byte_data(address, 0x46)
                ry = make_word(high, low)
                ry -= g.rybias

                w = bus.read_i2c_block_data(address, 0x43, 2)
                high = w[0]
                low = w[1]
                rx = make_word(high, low)
                rx -= g.rxbias

            if False:
                if rx > 120 and g.finspeed != 0 and g.dstatus != 2:
                    inhibitdodrive()
                    g.dstatus = 2
                    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
                        246, 0)
                    os.system(cmd)
    #                dodrive(0, 0)
                    print("stopped")
                    drive(0)


            # make the steering and the angle go in the same direction
            # now positive is clockwise
            r = -r

            t2 = time.time()
            dt = t2-t1
            t1 = t2

            angvel = r/gscale
            dang = angvel*dt
            g.ang += dang

            if True:
                w = bus.read_i2c_block_data(address, 0x3b, 6)
                x = make_word(w[0], w[1])
                x -= g.xbias
                y = make_word(w[2], w[3])
                y -= g.ybias
                z = make_word(w[4], w[5])
                z -= g.zbias

                x /= ascale
                y /= ascale
                z /= ascale

                acc = sqrt(x*x+y*y+z*z)
                if acc > 9.0 and g.detectcrashes:
                    g.crash = acc

                x0 = -x
                y0 = -y
                z0 = z

                # the signs here assume that x goes to the right and y forward

                x = x0*cos(pi/180*g.ang) - y0*sin(pi/180*g.ang)
                y = x0*sin(pi/180*g.ang) + y0*cos(pi/180*g.ang)

                g.vx += x*dt
                g.vy += y*dt
                g.vz += z*dt

                g.px += g.vx*dt
                g.py += g.vy*dt
                g.pz += g.vz*dt

            corr = 1.0

            vvx = g.inspeed*corr/100.0*sin(pi/180*g.ang)
            vvy = g.inspeed*corr/100.0*cos(pi/180*g.ang)

            ppxi = vvx*dt
            ppyi = vvy*dt

            if True:
                ds = sqrt(ppxi*ppxi+ppyi*ppyi)
                g.totals += ds

            g.ppx += ppxi
            g.ppy += ppyi

            if g.oldpos != None:
                t2_10 = int(t2*10)/10.0
                g.oldpos[t2_10] = (g.ppx, g.ppy, g.ang)

            # don't put too many things in this thread

            if False:
                w = bus.read_i2c_block_data(address, 0x4c, 6)
                mx0 = make_word(w[1], w[0])
                my0 = make_word(w[3], w[2])
                mz0 = make_word(w[5], w[4])

                mx = float((mx0-g.mxmin))/(g.mxmax-g.mxmin)*2 - 1
                my = float((my0-g.mymin))/(g.mymax-g.mymin)*2 - 1
                mz = mz0

                quot = (mx+my)/sqrt(2)
                if quot > 1.0:
                    quot = 1.0
                if quot < -1.0:
                    quot = -1.0
                mang = (asin(quot))*180/pi+45
                if mx < my:
                    mang = 270-mang
                    mang = mang%360

                mang = -mang
                mang = mang%360

            if True:
                accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
                        x, y, g.vx, g.vy, g.px, g.py, x0, y0, vvx, vvy, g.ppx, g.ppy, g.ang,
                        angvel, g.can_steer, g.can_speed, g.inspeed, outspeed, g.odometer,
                        z0, r, rx, ry, acc, g.finspeed, g.fodometer, t2-g.t0, can_ultra))

            if (t2-tlast > 0.1):
                tolog0("")
                tlast = t2

            j = g.angdiff/1000
            g.ang += j
            g.angdiff -= j

            #print("pp diff %f %f" % (g.ppxdiff, g.ppydiff))

            j = g.ppxdiff/100
            g.ppx += j
            g.ppxdiff -= j

            j = g.ppydiff/100
            g.ppy += j
            g.ppydiff -= j

            time.sleep(0.00001)

    except Exception as e:
        tolog("exception in readgyro: " + str(e))
        print("exception in readgyro: " + str(e))

def tolog2(str0, stdout):

    stdout = False

    if g.targetx and g.targety:
        d = dist(g.ppx, g.ppy, g.targetx, g.targety)
    else:
        d = -1

    str = "(speed %d time %.3f %f %f %f %f %f %3f) %s" % (
        g.inspeed,
        time.time() - g.t0,
        g.ppx,
        g.ppy,
        g.ang,
        d,
        battery,
        can_ultra,
        str0)
    logf.write(str + "\n")
    if stdout:
        print(str)

def tolog0(str):
    tolog2(str, False)

def tolog(str):
    tolog2(str, True)

global markermsg
markermsg = None

lastmarker0 = None

lastpos = None
lastpost = None

badmarkers = [0]
#badmarkers = [5]

goodmarkers = [25, 10, 43]
goodmarkers = None

battery = 0.0
ultra = 0.0
can_ultra = 0.0

global mqttc

def on_message(mosq, obj, msg):
    global battery, ultra

    p = str(msg.payload)
    while p[-1] == '\n' or p[-1] == '\t':
        p = p[:-1]

    # ignore our own position
    if g.VIN in msg.topic:
        return

    #sys.stdout.write(msg.topic + " " + str(msg.qos) + " " + p + "\n")
# "adc","current_value":"7.7142 7.630128199403445"

    m = re.search('"adc","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % g.VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"adc","current_value":"[0-9.]+ ([0-9.]+)"', p)
            if m:
            #print("battery %s" % m.group(1))
                battery = float(m.group(1))
                if battery < 20:
                    send_to_ground_control("battery %f" % battery)
        return

    # We still read this, but we don't use 'ultra' - we use 'can_ultra'
    m = re.search('"DistPub","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % g.VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"DistPub","current_value":"[0-9]+ ([0-9]+)"', p)
            if m:
                ultra = float(m.group(1))
        return


def mqtt_init():
    global mqttc

    url_str = "mqtt://test.mosquitto.org:1883"
    #url_str = "mqtt://iot.eclipse.org:1883"
    url = urllib.parse.urlparse(url_str)
    mqttc = mosquitto.Mosquitto()
    mqttc.on_message = on_message
    mqttc.connect(url.hostname, url.port)
    # will match /sics/moped/position/car2, for example
    mqttc.subscribe("/sics/moped/+/+", 0)
    mqttc.subscribe("/sics/moped/value", 0)

def send_to_mqtt(x, y):
    mqttc.publish("/sics/moped/position/%s" % g.VIN, "%f %f" % (x, y))
    pass

def handle_mqtt():
    global mqttc

    while True:
        try:
            mqtt_init()

            i = 0
            rc = 0
            while rc == 0:
                rc = mqttc.loop(5.0)
                i += 1

            print("mqttc.loop returned %d" % rc)
            if rc == 7 or rc == 1:
                mqttc = mosquitto.Mosquitto()
                mqtt_init()
        except Exception as e:
            time.sleep(5000)

ledstate = 0

def blinkleds():
    global ledstate

    ledstate = (ledstate + 1)%7
    setleds(0, ledstate)

def readmarker():
    while True:
        tolog("starting readmarker")
        try:
            readmarker0()
        except Exception as e:
            tolog("readmarker exception %s" % str(e))
            print("readmarker exception %s" % str(e))

def readmarker0():
    global lastmarker0, markermsg
    global lastpos, lastpost

    recentmarkers = []

    markertime = None

    while True:
        p = subprocess.Popen("tail -1 /tmp/marker0", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()
        m = res[0].decode('ascii')
        m = m.split('\n')[0]
        if m == lastmarker0:
            tolog0("no new marker0")
            continue

        lastmarker0 = m

        if g.ignoremarkers:
            continue

        tolog0("marker0 %s age %d" % (m, g.age))
        m1 = m.split(" ")
        if len(m1) != 7:
            print("bad marker line")
            continue
        if m1 == "":
            g.age += 1
        else:
            t = time.time()

            doadjust = False

            #blinkleds()

            g.markerno = int(m1[0])
            x = float(m1[1])
            y = float(m1[2])
            quality = float(m1[4])
            ori = float(m1[3])
            odiff = ori - (g.ang%360)
            if odiff > 180:
                odiff -= 360
            if odiff < -180:
                odiff += 360
            accepted = False
#            if g.angleknown and abs(odiff) > 45.0 and g.markerno != -1:
#                tolog("wrong marker %d %f" % (g.markerno, odiff))
#                g.markerno = -1
            if markertime == None or t-markertime > 5:
                #markertime = t
                skipmarker = False
            else:
                skipmarker = True

            if ((g.markerno > -1 and quality > 0.35
                 and g.markerno not in badmarkers
                 and (goodmarkers == None or g.markerno in goodmarkers)
                 and (x > -0.3 and x < 3.3 and y > 0 and y < 19.7)
                 or (x > 3.0 and x < 30 and y > 2.3 and y < 5.5))
                and not skipmarker):
                close = True
                if not g.angleknown:
                    g.ang = ori
                    g.ppx = x
                    g.ppy = y
                    g.oldpos = dict()
                g.angleknown = True

                it0 = float(m1[5])
                it1 = float(m1[6])
                now = time.time()
                delay1 = it1 - it0
                delay2 = now - it1
                #tolog0("delay %f delay2 %f" % (delay1, delay2))
                # Since the Optipos client runs on the same machine,
                # we can simply add the delays
                delay = delay1 + delay2

                it0_10 = int(it0*10)/10.0
                if g.adjust_t and it0 < g.adjust_t and False:
                    tolog0("POS: picture too old, we already adjusted %f %f" % (
                            it0, g.adjust_t))
                    send_to_ground_control("mpos %f %f %f %f 0 %f" % (x,y,g.ang,time.time()-g.t0, g.inspeed))
                    continue
                elif g.oldpos != None and it0_10 in g.oldpos:
                    (thenx, theny, thenang) = g.oldpos[it0_10]
                    doadjust = True
                    tolog0("POS: position then: %f %f" % (thenx, theny))
                elif g.oldpos != None:
                    tolog0("POS: can't use oldpos")                    
                    continue

                if True:
                    if lastpos != None:
                        (xl, yl) = lastpos
                        dst = dist(thenx, theny, xl, yl)
                        tolog0("local speed %f" % (dst/(t-lastpost)))
                        if dst/(t-lastpost) > TOOHIGHSPEED:
                            close = False

                dst = dist(g.ppx, g.ppy, x, y)
                # even if somewhat correct: it causes us to lose
                # position when we reverse
                if dst > 2.0 and g.markercnt > 10:
                    close = False
                tolog0("marker dist %f" % dst)

                if not close:
                    msg = "bad marker %d not close" % g.markerno
                    if msg != markermsg:
                        tolog(msg)
                        markermsg = msg
                    g.age += 1
                else:
                    accepted = True
                    g.markercnt += 1
                    tolog0("marker1 %s %d %f %f" % (m, g.age, g.ang, ori))
                    if doadjust:
                        doadjust_n = 1
                    else:
                        doadjust_n = 0
                    send_to_ground_control("mpos %f %f %f %f %d %f" % (x,y,g.ang,time.time()-g.t0, doadjust_n, g.inspeed))
                    #send_to_mqtt(x, y)
                    lastpos = (thenx,theny)
                    g.px = x
                    g.py = y
                    if True:
#                    if g.markercnt % 10 == 1:
#                    if g.markercnt == 1:
                        if doadjust:
                            g.adjust_t = time.time()
                            tolog0("adjusting pos %f %f -> %f %f" % (g.ppx, g.ppy,
                                                                     x, y))
                        if g.markercnt != 1:
                            g.angdiff = (ori-g.ang)%360
                            if True:
                                if doadjust:
                                    tolog0("old pp diff %f %f" % (
                                            g.ppxdiff, g.ppydiff))
                                    ppxdiff1 = x-g.ppx
                                    ppydiff1 = y-g.ppy

                                    ppxdiff1 = x-thenx
                                    ppydiff1 = y-theny
                                    angdiff1 = (ori-thenang)%360

                                    ppxdiff1 /= 2
                                    ppydiff1 /= 2
                                    #angdiff1 /= 2
                                    g.ppxdiff = ppxdiff1
                                    g.ppydiff = ppydiff1
                                    #print("3 ppydiff := %f" % g.ppydiff)
                                    g.angdiff = angdiff1
                            else:
                                g.ppx = x
                                g.ppy = y
                                #print("1 ppy := %f" % g.ppy)
                            g.angdiff = g.angdiff % 360
                            if g.angdiff > 180:
                                g.angdiff -= 360
                        else:
                            g.ppx = x
                            g.ppy = y
                            #print("2 ppy := %f" % g.ppy)
                    #g.vx = sin(g.ang*pi/180)*g.inspeed/100
                    #g.vy = cos(g.ang*pi/180)*g.inspeed/100
                    lastpost = it0
                    g.age = 0
                    #g.ang = ori
            else:
                g.age += 1

            if False:
                print("marker good=%s %d = (%f,%f) (%f, %f) %f %f" % (
                        str(accepted), g.markerno, x, y,
                        g.ppx, g.ppy, g.finspeed, g.inspeed))
            if accepted:
                recentmarkers = [str(g.markerno)] + recentmarkers
            else:
                recentmarkers = ['x'] + recentmarkers
            if len(recentmarkers) > 10:
                recentmarkers = recentmarkers[0:10]
            send_to_ground_control("markers %s" % " ".join(recentmarkers))

            if not accepted:
                send_to_ground_control("badmarker %f %f" % (x,y))
                tolog0("marker5 %s %d %f %f" % (m, g.age, g.ang, ori))
#            tolog0("marker2 %d %f %f %d %f %d %f" % (-1, g.px, g.py, int(g.ang), 0.5, g.age, g.ang))
#            tolog0("marker3 %d %f %f %d %f %d %f" % (-1, g.ppx, g.ppy, int(g.ang), 0.5, g.age, g.ang))
        time.sleep(0.00001)


# The code here is a bit ad-hoc. We need to find out why the various
# constants and offsets appear.
def readspeed2():
    global can_ultra, can_ultra_count

    part = b""
    part2 = b""
    while True:
        data = canSocket.recv(1024)
        if (data[0], data[1]) == (100,4) and data[4] == 2:
            # length of packet is 2
            print((data[8], data[9]))
            g.rc_button = True
        elif (data[0], data[1]) == (100,4):
            if data[8] == 16:
                parts = str(part)

                m = re.search("speed x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)", parts)
                if m:
                    #print(parts)
                    oinspeed = g.inspeed
                    g.inspeed = g.speedsign * int(m.group(1))

                    alpha = 0.8
                    g.inspeed_avg = (1-alpha)*g.inspeed + alpha*oinspeed

                    if (g.inspeed == 0 and g.speedtime != None and
                        time.time() - g.speedtime > 7.0):
                        speak("obstacle")
                        send_to_ground_control("obstacle")
                        drive(0)

                    g.odometer = int(m.group(2))
                    if g.odometer != g.lastodometer:
                        send_to_ground_control("odometer %d" % (g.odometer))
                        g.lastodometer = g.odometer
                    #print("rsp-odo %d %d" % (g.inspeed, g.odometer))

                    g.finspeed = int(m.group(3))
                    g.finspeed *= g.speedsign

                    g.fodometer = int(m.group(4))
                    #print("fsp-odo %d %d" % (g.finspeed, g.fodometer))

                part = b""
            part += data[9:]
        elif (data[0], data[1]) == (1,1):
            sp = data[8]
            st = data[9]
            if False:
                if g.last_send != None and (sp, st) != g.last_send:
                    tolog("remote control")
                    g.remote_control = True
            if sp > 128:
                sp -= 256
            g.can_speed = sp
            if not g.braking:
                if sp < 0:
                    g.speedsign = -1
                elif sp > 0:
                    g.speedsign = 1
            if st > 128:
                st -= 256
            tolog("CAN %d %d" % (sp, st))
            g.can_steer = st
        elif (data[0], data[1]) == (108,4):
            # Reading DistPub this way is not a good idea, since those
            # messages come often and slow down the other threads (or the
            # whole process?).
            # DistPub
            # note that non-ASCII will appear as text \x07 in 'parts'
            if data[8] == 16:
                part2 = part2[19:]
                part2s = str(part2)

                m = re.search("([0-9]+) ([0-9]+)", part2s)
                if m:
                    cnt = int(m.group(1))
                    d = int(m.group(2))

                    #print((cnt,d))
                    can_ultra = d/100.0
                    can_ultra_count = cnt
                part2 = b""
            part2 += data[9:]

        time.sleep(0.00001)            

outspeed = 0.0
outspeedcm = None
steering = 0

# error here: we normalize the argument, but not the other value

def drive(sp):
    global outspeed
    global outspeedcm

    if True:
        if sp != 0 and not g.braking:
            g.speedsign = sign(sp)

        outspeedcm = sp*2
        print("outspeedcm = %d" % outspeedcm)
    else:

        # do this in readspeed2 instead
        # maybe, but then steer will zero the speed
        outspeed = sp

        if abs(sp) >= 7:
            g.speedtime = time.time()
        else:
            g.speedtime = None

        if sp != 0 and not g.braking:
            g.speedsign = sign(sp)

        if sp < 0:
            sp += 256
        st = steering
        if st < 0:
            st += 256
        tolog("motor %d steer %d" % (sp, steering))
        cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
            sp, st)
        #print (sp, steering, cmd)
        os.system(cmd)

def steer(st):
    global steering

    steering = st
    sp = outspeed
#    if st < 0:
#        st += 256
#    if sp < 0:
#        sp += 256

#    tolog("motor %d steer %d" % (outspeed, st))
#    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
#        sp, st)
    #print (outspeed, st, cmd)
    dodrive(sp, st)
#    os.system(cmd)
#    tolog("motor2 %d steer %d" % (outspeed, st))

def stop(txt = ""):
    global steering, outspeed

    steering = 0
    outspeed = 0.0

    g.speedtime = None

    tolog("(%s) motor %d steer %d" % (txt, outspeed, steering))
    dodrive(0, 0)

def connect_to_ground_control():
    while True:
        if not g.ground_control:
            s = open_socket()
            if not s:
                print("no connection")
            else:
                print("connection opened")
                g.ground_control = s
                start_new_thread(from_ground_control, ())
                send_to_ground_control("info %s" % g.VIN)
        time.sleep(5)

# almost the same as in tcontrol_comm.py
def linesplit(socket):
    buffer = socket.recv(4096)
    buffer = buffer.decode("ascii")
    buffering = True
    while buffering:
        if "\n" in buffer:
            (line, buffer) = buffer.split("\n", 1)
            yield line
        else:
            more = socket.recv(4096)
            more = more.decode("ascii")
            if not more:
                buffering = False
            else:
                buffer += more
    if buffer:
        yield buffer
    return None

def warningblink(state):
    if state == True:
        if g.warningblinking == True:
            return
        setleds(7, 0)
        g.warningblinking = True
    else:
        if g.warningblinking == False:
            return
        setleds(0, 7)
        g.warningblinking = False

def from_ground_control():
    global path
    global heartn_r

    lastreportclosest = False

    while True:
        if g.ground_control:
            for data in linesplit(g.ground_control):
                #print(data)
                l = data.split(" ")
                #print(l)
                #print(data)
                if l[0] == "go":
                    x = float(l[1])
                    y = float(l[2])
                    print(("goto is not implemented", x, y))
                elif l[0] == "path":
                    path = ast.literal_eval(data[5:])
                    print(path)
                elif l[0] == "continue":
                    g.paused = False
                elif l[0] == "carsinfront":
                    n = int(l[1])
                    closest = None
                    for i in range(0, n):
                        #dir = float(l[5*i+2])
                        dist = float(l[5*i+3])
                        #x = float(l[5*i+4])
                        #y = float(l[5*i+5])
                        othercar = float(l[5*i+6])
                        if closest == None or closest > dist:
                            closest = dist
                    if closest:
                        #print("closest car in front1: dir %f dist %f" % (
                         #       dir, closest))
                        # a car length
                        closest = closest - 0.5
                        # some more safety:
                        closest = closest - 0.5
                        if closest < 0:
                            closest = 0
                        # 4 is our safety margin and should make for
                        # a smoother ride
                        if g.limitspeed == None:
                            print("car in front")
                        tolog("car in front")
                        g.limitspeed = 100*closest/0.85/4
                        if g.limitspeed < 11:
                            #print("setting limitspeed to 0")
                            g.limitspeed = 0
                            if outspeedcm != None and outspeedcm != 0:
                                warningblink(True)
                        else:
                            #print("reduced limitspeed")
                            pass

                        #print("closest car in front2: dir %f dist %f limitspeed %f" % (
                                #dir, closest, g.limitspeed))
                        lastreportclosest = True
                    else:
                        g.limitspeed = None
                        if lastreportclosest:
                            #print("no close cars")
                            pass
                        lastreportclosest = False
                    if outspeedcm:
                        # neither 0 nor None
                        if g.limitspeed == 0:
                            send_to_ground_control("message stopping for obstacle")
                        elif g.limitspeed != None and g.limitspeed < outspeedcm:
                            send_to_ground_control("message slowing for obstacle %f" % g.limitspeed)
                        else:
                            send_to_ground_control("message ")
                    else:
                        send_to_ground_control("message ")
                elif l[0] == "parameter":
                    g.parameter = int(l[1])
                    print("parameter %d" % g.parameter)
                # can be used so we don't have to stop if the next
                # section is free
                elif l[0] == "free":
                    s = int(l[1])
                    g.section_status[s] = "free"
                elif l[0] == "occupied":
                    s = int(l[1])
                    g.section_status[s] = "occupied"
                elif l[0] == "cargoto":
                    x = float(l[2])
                    y = float(l[3])
                    goto(x, y, l[4])
                elif l[0] == "heartecho":
                    t1 = float(l[1])
                    t2 = float(l[2])
                    heartn_r = int(l[3])
                    #print("heartecho %.3f %.3f %.3f %d" % (time.time() - g.t0, t1, t2, heartn_r))
                else:
                    print("unknown control command %s" % data)
        time.sleep(1)

def readvin():
    f = open("/home/pi/can-utils/java/settings.properties")
    for line0 in f:
        line = line0[:-1]
        m = re.match("VIN=(.*)", line)
        if m:
            return m.group(1)
    return None

def connect_to_ecm():
    stopped = False

    s = open_socket2()

    ecmt0 = time.time()

    counter = 0
    g.crashacc = None

    while True:
        if g.crashacc:
            #g.crash = False
            if not stopped:
                counter = 9
                #s.send("crash".encode('ascii'))
                print("crash: %f" % g.crash)
                g.remote_control = True
                #stop()
                drive(0)
                #speak("ouch")
                warningblink(True)
                stopped = True
                s112 = open_socket3()
                sstr = ('accident %f %f %f %s\n' % (
                        g.ppx, g.ppy, g.crashacc, g.VIN)).encode("ascii")
                print(sstr)
                s112.send(sstr)
                print(sstr)
                resp = s112.recv(1024)
                print(resp)
                resp = resp.decode("ascii")
                print(resp)
                if True:
                    resp = json.loads(resp)
                    print(resp)
                    say = resp['speak']
                else:
                    say = resp
                print(say)
                speak(say)
                s112.close()

        if True:
            t = time.time()
            if t-ecmt0 > 1.0:
                #s.send("bar".encode('ascii'))
                g.crashacc = 0.0
                if g.crash:
                    g.crashacc = g.crash
                    send_to_ground_control("message crash %f" % g.crashacc)
                s.send(('{"crash":%d, "x":%f, "y":%f, "crashacc":%f}\n' % (
                            counter, g.ppx, g.ppy, g.crashacc)).encode("ascii"))
                ecmt0 = t
        time.sleep(0.05)
        if counter != 9:
            counter = (counter+1)%8

heartn = -1
heartn_r = -1

def heartbeat():
    global heartn

    while True:
        heartn += 1
        send_to_ground_control("heart %.3f %d" % (time.time()-g.t0, heartn))

        if heartn-heartn_r > 1:
            print("waiting for heart echo %d %d" % (heartn, heartn_r))

        if heartn-heartn_r > 3:
            if g.limitspeed0 == "notset":
                print("setting speed to 0 during network pause")
                g.limitspeed0 = g.limitspeed
                g.limitspeed = 0.0

        if heartn-heartn_r < 2:
            if g.limitspeed0 != "notset":
                print("restoring speed limit to %s after network pause" % (str(g.limitspeed0)))
                g.limitspeed = g.limitspeed0
                g.limitspeed0 = "notset"

        time.sleep(5)

def init():
    global logf, markermsg
    global accf

    g.VIN = readvin()
    print("VIN %s" % g.VIN)

    if g.VIN == "car5":
        g.mxmin = -99
        g.mxmax = -40
        g.mymin = 100
        g.mymax = 152

    if g.VIN == "car3":
        g.mxmin = -20
        g.mxmax = 39
        g.mymin = 37
        g.mymax = 85

    # fake, just to make them defined
    if g.VIN == "car4":
        g.mxmin = 0
        g.mxmax = 100
        g.mymin = 0
        g.mymax = 100

    g.angleknown = False

    eightinit()

    setleds(0, 7)

    start_new_thread(connect_to_ground_control, ())

    logf = open("navlog", "w")
    accf = open("acclog", "w")
    #accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
    #x, y, g.vx, g.vy, g.px, g.py, x0, y0, vvx, vvy, g.ppx, g.ppy, g.ang))
    accf.write("x y vx vy px py x0 y0 vvx vvy ppx ppy ang angvel steering speed inspeed outspeed odometer z0 r rx ry acc finspeed fodometer t can_ultra\n")

    g.t0 = time.time()
    print("t0 = %f" % g.t0)

    tolog("init")

    g.rbias = 0
    g.xbias = 0
    g.ybias = 0
    g.zbias = 0

    # computing angbias would be better
    ncalibrate = 100
    for i in range(0, ncalibrate):
        high = bus.read_byte_data(address, 0x47)
        low = bus.read_byte_data(address, 0x48)
        r = make_word(high, low)
        g.rbias += r

        if False:
            high = bus.read_byte_data(address, 0x43)
            low = bus.read_byte_data(address, 0x44)
            r = make_word(high, low)
            g.rxbias += r

            high = bus.read_byte_data(address, 0x45)
            low = bus.read_byte_data(address, 0x46)
            r = make_word(high, low)
            g.rybias += r

        w = bus.read_i2c_block_data(address, 0x3b, 6)
        x = make_word(w[0], w[1])
        y = make_word(w[2], w[3])
        z = make_word(w[4], w[5])
        g.xbias += x
        g.ybias += y
        g.zbias += z

    g.rbias = g.rbias/float(ncalibrate)
    g.rxbias = g.rxbias/float(ncalibrate)
    g.rybias = g.rybias/float(ncalibrate)
    g.xbias = g.xbias/float(ncalibrate)
    g.ybias = g.ybias/float(ncalibrate)
    g.zbias = g.zbias/float(ncalibrate)

    print("rbias = %f, rxbias = %f, rybias = %f, xbias = %f, ybias = %f, zbias = %f" % (g.rbias, g.rxbias, g.rybias, g.xbias, g.ybias, g.zbias))


    g.px = 0.0
    g.py = 0.0
    g.pz = 0.0

    g.ppx = 0.0
    g.ppy = 0.0
    g.ppz = 0.0

    g.vx = 0.0
    g.vy = 0.0
    g.vz = 0.0

    start_new_thread(readmarker, ())
    start_new_thread(handle_mqtt, ())
    start_new_thread(readspeed2, ())
    start_new_thread(readgyro, ())
    start_new_thread(senddrive, ())
    start_new_thread(keepspeed, ())
    start_new_thread(heartbeat, ())
    start_new_thread(connect_to_ecm, ())

def dodrive(sp, st):
    g.send_sp = sp
    g.send_st = st

global senddriveinhibited
senddriveinhibited = False

def inhibitdodrive():
    global senddriveinhibited
    senddriveinhibited = True


def senddrive():

    first0done = False

    old_sp = 0
    old_st = 0
    while True:
        time.sleep(0.00001)

        if g.send_sp != None:

    #        if g.remote_control:
    #            continue

            g.send_sp = int(g.send_sp)
            g.send_st = int(g.send_st)

            sp = g.send_sp
            if sp < 0:
                sp += 256
            st = g.send_st
            if st < 0:
                st += 256

            #print(g.last_send)
            if not senddriveinhibited:
                if (sp == 0 and not first0done) or g.last_send != (sp, st):
                    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
                        sp, st)
                    #tolog("senddrive %d %d" % (g.send_sp, send_st))
                    #print("senddrive %d %d" % (g.send_sp, send_st))
                    g.last_send = (sp, st)
                    os.system(cmd)
                    if sp == 0:
                        first0done = True
        if g.ledcmd:
            (mask, code) = g.ledcmd
            #print("doing setleds %d %d" % (mask, code))
            cmd = "/home/pi/can-utils/cansend can0 '461#060000006D3%d3%d00'" % (
                mask, code)
            os.system(cmd)
            g.ledcmd = None


# 0 to 9
speeds = [0, 7, 11, 15, 19, 23, 27, 37, 41, 45, 49]

def keepspeed():
    global outspeed
    global outspeedcm

    outspeedi = 0

    while True:
        time.sleep(0.1)

        if outspeedcm == None:
            continue

        spi = outspeedi

        desiredspeed = outspeedcm

        if g.limitspeed != None and desiredspeed > g.limitspeed:
            desiredspeed = g.limitspeed

        if user_pause:
            desiredspeed = 0

        if desiredspeed > g.finspeed:
            if spi < len(speeds)-1:
                spi += 1
        elif desiredspeed < g.finspeed:
            if spi > 0:
                spi -= 1

        desiredspeed_sign = sign(desiredspeed)
        desiredspeed_abs = abs(desiredspeed)
        if True:
            # bypass the control
            spi = int(desiredspeed_abs/10)
            if spi > len(speeds)-1:
                spi = len(speeds)-1
            sleeptime = 1
        else:
            sleeptime = 3

        sp = speeds[spi]
        outspeedi = spi
        # spi/outspeedi don't remember the sign

        sp *= desiredspeed_sign

        if False:
            print("outspeedcm %f finspeed %f outspeedi %d spi %d sp %f outspeed %f" % (
                    outspeedcm, g.finspeed, outspeedi, spi, sp, outspeed))

        if outspeed == sp and sp != 0:
#            pass
            continue

        outspeed = sp

        if abs(sp) >= 7:
            g.speedtime = time.time()
        else:
            g.speedtime = None

        if sp != 0 and not g.braking:
            g.speedsign = sign(sp)

        if sp != 0:
            warningblink(False)

#        if sp < 0:
#            sp += 256
        st = steering
#        if st < 0:
#            st += 256
        tolog("motor %d steer %d" % (sp, st))
        dodrive(sp, st)
        time.sleep(sleeptime)

def dist(x1, y1, x2, y2):
    return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

def sign(x):
    if x < 0:
        return -1
    if x > 0:
        return 1
    return 0

def getdist(x2, y2):
    # NEW
    x1 = g.ppx
    y1 = g.ppy

    d = dist(x1, y1, x2, y2)
    tolog("we are at (%f, %f), distance to (%f, %f) is %f" % (
            x1, y1, x2, y2, d))

    return d

# d > 0
# User command
def backup(d):
    drive(-10)
    godist(d)
    stop()

def godist(d):
    loops = 0
    o1 = g.odometer
    o2 = o1 + d*5/(pi*10.2/100)
    while True:
        loops += 1
        if outspeed == 0.0:
            tolog("motor needed in godist")
            return False
        if g.inspeed == 0.0 and loops > 20:
            tolog("speed 0 in godist; obstacle?")
            return False
        o = g.odometer
        if o >= o2:
            return True
        time.sleep(0.1)

import socket
import sys

HOST = 'localhost'    # The remote host
HOST = '192.168.43.73'	# merkur on my hotspot
HOST = '193.10.66.250'  # merkur on the SICS wifi net
PORT = 50008              # The same port as used by the server

ECMHOST = 'localhost'
ECMPORT = 9002

s = None

def open_socket():
    for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            #print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            #print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        print('could not open socket')
        return False

    return s

def open_socket2():
    for res in socket.getaddrinfo(ECMHOST, ECMPORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        print('could not open socket2')
        return False

    return s

H112HOST = "appz-ext.sics.se"
H112PORT = 6892

def open_socket3():
    for res in socket.getaddrinfo(H112HOST, H112PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        print('could not open socket2')
        return False

    return s

def send_to_ground_control(str):
    if not g.ground_control:
        return

    try:
        str1 = str + "\n"
        g.ground_control.send(str1.encode('ascii'))
    except Exception as e:
        print("send1 %s" % e)
        g.ground_control = None
#        connect_to_ground_control()

def initializeCAN(network):
    """
    Initializes the CAN network, and returns the resulting socket.
    """
    # create a raw socket and bind it to the given CAN interface
    s = socket.socket(socket.AF_CAN, socket.SOCK_RAW, socket.CAN_RAW)
    s.bind((network,))
    return s

canNetwork = "can0"
canFrameID = 1025
canSocket = initializeCAN(canNetwork)

def checkbox1(x, y, tup, leftp):
    (lxprev, lyprev, lx, ly) = tup

    dlx = lx-lxprev
    dly = ly-lyprev
    a = atan2(dlx, dly)
    lx1 = lxprev + dlx*cos(a) - dly*sin(a)
    ly1 = lyprev + dlx*sin(a) + dly*cos(a)

    dx = x-lxprev
    dy = y-lyprev
    x1 = lxprev + dx*cos(a) - dy*sin(a)
    y1 = lyprev + dx*sin(a) + dy*cos(a)

    if False:
        if y1 >= lyprev and y1 <= ly1:
            if leftp:
                print("%f %f [%f %f]" % (x, y, lxprev, x1))
                if lxprev > x1:
                    speak("ee")
            else:
                # when leftp==False, we have really rxprev etc.
                print("%f %f         [%f %f]" % (x, y, x1, lxprev))
                if lxprev < x1:
                    speak("oo")

def checkpos():
    pos = findpos(g.ppx,g.ppy,g.ang)
    #print((g.ppx,g.ppy,g.ang),pos)


    if g.currentbox == None:
        return
    x = g.ppx
    y = g.ppy
    checkbox1(x, y, g.currentbox[0], True)
    checkbox1(x, y, g.currentbox[1], False)


def goto_1(x, y):
    g.targetx = x
    g.targety = y

    missed = False
    inc = 0
    inc2 = 0
    lastdist = None
    brake_s = 0.0

    while True:
        if g.remote_control:
            print("remote_control is true")
            return

        checkpos()

        dist = getdist(x, y)
        if g.inspeed != 0:
            # Assume we are going in the direction of the target.
            # At low speeds, braking time is about 1.5 s.
            brake_s = 1.5 * abs(g.inspeed)/100

        # say that braking distance is 1 dm at higher speed, when
        # braking electrically
        if g.inspeed > 0:
            brake_s = 0.4
        else:
            brake_s = 0.6

        # we should only use brake_s when we are going to stop
        brake_s = 0.0

        # 'lastdist' is never non-None now, nor 'missed'
        if lastdist != None:
            if dist < lastdist - 0.01:
                inc = -1
                lastdist = dist
            elif dist > lastdist + 0.01:
                if inc == -1:
                    missed = True
                    tolog("missed target")
                inc = 1
                lastdist = dist

        tolog("gotoa1 %f %f -> %f %f" % (g.ppx, g.ppy, x, y))

        a = atan2(y-g.ppy, x-g.ppx)
        adeg = 180/pi*a
        adeg = 90-adeg

        adiff = g.ang - adeg
        adiff = adiff%360

        tolog("gotoa2 a %f adeg %f adiff %f" % (a, adeg, adiff))

        if g.speedsign < 0:
            adiff += 180

        if adiff > 180:
            adiff -= 360

        adiff = -adiff
        # now, positive means the target is to the right of us

        tolog("gotoa3 adiff %f" % (adiff))

        #print(adiff)

#        if dist < TARGETDIST or dist < brake_s or missed:
        if (not g.allangles and abs(adiff) > 90) or dist < 0.3:
            if False:
                #stop("9")
    #            drive(-1)
                # continue a little so it can pass the target if it wasn't
                # there yet
                time.sleep(0.5)
    #            drive(-1)
    #            time.sleep(0.2)
                drive(0)
            #print("adiff %f dist %f" % (adiff, dist))
            if dist < 0.3:
                #print("dist < 0.3")
                pass
            if abs(adiff) > 90:
                print("adiff = %f" % adiff)
            return



        asgn = sign(adiff)
        aval = abs(adiff)

        p = 4.0

        st = p*aval
        if st > 100:
            st = 100
        st = asgn*g.speedsign*st

        if False:
            st_d = st - steering
            if st_d > 10:
                st = steering + 10
            elif st_d < -10:
                st = steering - 10

        steer(st)

        tolog("gotoa4 steer %f" % (st))

        send_to_ground_control("dpos %f %f %f %f 0 %f" % (g.ppx,g.ppy,g.ang,time.time()-g.t0, g.finspeed))

        time.sleep(0.1)

def stopx(i, t = 3.0):
    g.braking = True

    dir = sign(g.inspeed)

    if True:
        # -dir*1 is too little (it has no effect), and -dir*15 can
        # cause the car to actually obey the speed when having
        # reversed.
        drive(-dir*5)
        time.sleep(0.5)
        drive(0)
        time.sleep(t-0.5)
    else:
        drive(0)
        time.sleep(t)

    g.braking = False

def dospeak(s, p):
    if '#' in s:
        s = s.replace('#', str(g.speakcount))
    os.system("espeak -a500 -p%d '%s' >/dev/null 2>&1" % (p, s))

def speak(str):
    p = 50
    if g.VIN == "car2":
        p = 80
    start_new_thread(dospeak, (str, p))

def setleds(mask, code):
    print("setleds %d %d" % (mask, code))

    if False:
        cmd = "/home/pi/can-utils/cansend can0 '461#060000006D3%d3%d00'" % (
            mask, code)
        os.system(cmd)
    else:
        g.ledcmd = (mask, code)

# User command
def trip(path, first=0):
    g.speakcount = 1

    setleds(0, 7)

    i = 0
    while True:
        if g.rc_button:
            setleds(1, 6)
            stop()
            break
        j = 0
        if first > 0:
            path1 = path[first:]
        else:
            path1 = path
        for cmd in path1:
            if cmd[0] == 'go':
                sp = cmd[1]
                sp = int(sp*g.parameter/100.0)
                x = cmd[2]
                y = cmd[3]
                spdiff = sp-outspeed
                if spdiff > 20:
                    # this should be done in a separate thread
                    drive(sp-spdiff/2)
                    time.sleep(0.5)
                    drive(sp)
                else:
                    drive(sp)
                goto_1(x, y)
            elif cmd[0] == 'stop':
                if len(cmd) > 1:
                    t = float(cmd[1])
                else:
                    t = 3
                g.paused = True
                stopx(i, t)
                send_to_ground_control("stopat %d" % j)
                while g.paused:
                    time.sleep(1)
            elif cmd[0] == 'speak':
                speak(cmd[1])
            else:
                print("unknown path command: %s" % cmd)
            j += 1
        first = 0
        g.speakcount += 1


def small():
    while True:
        drive(7)
        time.sleep(1)
        stop()
        time.sleep(1)
        drive(-10)
        time.sleep(1)
        stop()
        time.sleep(1)

def square():
    drive(7)
    a = 2
    b = 2
    while True:
        goto_1(0, b)
        goto_1(a, b)
        goto_1(a, 0)
        goto_1(0, 0)


def triangle():
    drive(7)
    a = 2
    b = 2
    while True:
        goto_1(a, b)
        goto_1(a, 0)
        goto_1(0, 0)

# This function has problems: 1) it may happen that we land outside the
# allowed area and then steer so we stay outside it, and while this happens
# we may bump into the wall.
# 2) if we make the buffer area big enough so we don't hit the wall due
# to braking distance and inaccuracy in positioning, the allowed area
# becomes so small that we can leave it during the first, curving phase.
def wander():
    a = 1.0
    drive(0)
    dir = 1
    while True:
        st = int(random.random()*200-100)
        print("%f %f: steering %d" % (g.ppx, g.ppy, st))
        steer(st)
        drive(dir*20)
        n = 0
        # first note that we begin moving; then whether we stop
        while n < 30:
            print("%f %f" % (g.ppx, g.ppy))
            time.sleep(0.1)
            n += 1
        steer(0)
        while g.inspeed != 0 and (g.ppx < 3.0-a and g.ppx > a and
                                  g.ppy < 19.7-a and g.ppy > 11.0 + a):
            print("%f %f" % (g.ppx, g.ppy))
            time.sleep(0.1)
        print("%f %f" % (g.ppx, g.ppy))
        drive(0)
        time.sleep(2)
        dir = -dir

# We should also have a lower limit for the distance to the new point
# as it is, we can have an angle less than 45 degrees to a point we
# cannot reach. With a suitable limit, we can increase 45.
def wander2():
    a = 0.7
    x1 = a
    x2 = 3.0-a
    lx = x2-x1
    y1 = 11.0+a
    y2 = 19.7-a
    ly = y2-y1
    dir = -1
    while True:
        drive(0)
        time.sleep(4)
        while True:
            k = random.random()
            l = k*(ly+lx+ly+lx)
            if l < ly:
                x = x1
                y = y1+l
            elif l < ly+lx:
                x = x1+l-ly
                y = y2
            elif l < ly+lx+ly:
                x = x2
                y = y1+l-lx-ly
            else:
                x = x1+l-ly-lx-ly
                y = y1
            ang2 = atan2(y-g.ppy, x-g.ppx)*180/pi
            ang2 = 90-ang2
            angdiff = ang2-g.ang
            angdiff = angdiff%360
            if angdiff > 180:
                angdiff -= 360
            print("x = %f y = %f ang2 = %f angdiff = %f k*dir = %f" % (
                    x, y, ang2, angdiff, k*dir))
            if dir > 0:
                if abs(angdiff) < 45:
                    break
            else:
                if abs(angdiff) > 180-45:
                    break
            if dir > 0:
                drive(5)
            else:
                drive(-5)
        print("going to %f %f" % (x, y))
        goto_1(x, y)
        dir = -dir

def whole():
    a = 0.5
    b = 0.8
    x1 = a
    x2 = 3.0-a
    y1 = 12+a
    y2 = 19.7-a
    drive(0)
    time.sleep(2)
    drive(20)
    while True:
        goto_1(x2, y2-b),
        goto_1(x2-b, y2),
        goto_1(x1+b, y2),
        goto_1(x1, y2-b),
        goto_1(x1, y1+b),
        goto_1(x1+b, y1),
        goto_1(x2-b, y1),
        goto_1(x2, y1+b)

def whole2():
    a = 0.5
    b = 0.8
    x1 = a
    x2 = 3.0-a
    y1 = 12+a
    y2 = 19.7-a
    y12 = (y1+y2)/2
    drive(0)
    time.sleep(2)
    drive(20)
    while True:
        goto_1(x2, y12),
        goto_1(x2, y2-b),
        goto_1(x2-b, y2),
        goto_1(x1+b, y2),
        goto_1(x1, y2-b),
        goto_1(x1, y12),
        goto_1(x1, y1+b),
        goto_1(x1+b, y1),
        goto_1(x2-b, y1),
        goto_1(x2, y1+b)

def whole3():
    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    a = 0.5
    b = 0.8
    x1 = a
    x2 = 3.0-a
    y1 = 12+a
    y2 = 19.7-a
    y12 = (y1+y2)/2
    drive(0)
    time.sleep(2)
    drive(20)
    while True:
        goto_1(x2, y1+b + (y12-(y1+b))/3),
        goto_1(x2, y1+b + 2*(y12-(y1+b))/3),
        goto_1(x2, y12),
        goto_1(x2, y12 + (y2-b-y12)/3),
        goto_1(x2, y12 + 2*(y2-b-y12)/3),
        goto_1(x2, y2-b),

        goto_1(x2-b+cos(30*pi/180)*b,
               y2-b+sin(30*pi/180)*b),
        goto_1(x2-b+cos(60*pi/180)*b,
               y2-b+sin(60*pi/180)*b),

        goto_1(x2-b, y2),

        goto_1(x1+b, y2),

        if True:
            goto_1(x1+b-cos(60*pi/180)*b,
                   y2-b+sin(60*pi/180)*b),
            goto_1(x1+b-cos(30*pi/180)*b,
                   y2-b+sin(30*pi/180)*b),

        goto_1(x1, y2-b),
        goto_1(x1, y12),
        goto_1(x1, y1+b),

        goto_1(x1+b-cos(30*pi/180)*b,
               y1+b-sin(30*pi/180)*b),
        goto_1(x1+b-cos(60*pi/180)*b,
               y1+b-sin(60*pi/180)*b),

        goto_1(x1+b, y1),
        goto_1(x2-b, y1),

        goto_1(x2-b+cos(60*pi/180)*b,
               y1+b-sin(60*pi/180)*b),
        goto_1(x2-b+cos(30*pi/180)*b,
               y1+b-sin(30*pi/180)*b),

        goto_1(x2, y1+b)


user_pause = False

def pause():
    global user_pause
    user_pause = True

def cont():
    global user_pause
    user_pause = False


signalling = False

def signal():
    while signalling:
        os.system("(python tone2.py 8000 3000 1200;python tone2.py 8000 3000 1000) 2>/dev/null")

def goto(x, y, state):
    start_new_thread(gotoaux, (x, y, state))

def gotoaux(x, y, state):
    global signalling

    print("gotoaux %f %f %s" % (x, y, state))
    drive(0)
    if state == "accident":
        signalling = True
        start_new_thread(signal, ())

    time.sleep(4)
    drive(30)
    goto_1(x, y)
    signalling = False
    drive(0)

def whole4(dir):
    start_new_thread(whole4aux, (dir,))


def rev(l0):
    l = l0[:]
    l.reverse()
    return l


randdict = dict()

def randsel(a, b):
    astr = str(a)
    bstr = str(b)
    if astr not in randdict:
        randdict[astr] = 0
    if bstr not in randdict:
        randdict[bstr] = 0

    an = randdict[astr]
    bn = randdict[bstr]

    k = int(random.random()*(an+bn+2))
    if k <= bn:
        randdict[astr] = randdict[astr] + 1
        return a
    else:
        randdict[bstr] = randdict[bstr] + 1
        return b

def piece2path(p, dir, offset):
    path1 = [(i, nodes[i]) for i in p]
    path = makepath(dir*offset, path1)
    return path

def whole4aux(dir):

    # 'dir' is obsolete - it should always be -1

    speed0 = 20

    drive(0)
    time.sleep(4)
    drive(speed0)

    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    path0 = rev(piece3b) + [23]

    print("path0 = %s" % str(path0))

    # idea: from the current position, determine which piece we can
    # start with

    path = piece2path(path0, dir, 0.25)
    lpath = piece2path(path0, dir, 0.15)
    rpath = piece2path(path0, dir, 0.35)

    lx = None
    ly = None
    rx = None
    ry = None

    if dir == 1:
        path.reverse()

    i1 = -1

    while True:
        i10 = path[-1][2]
        i2 = path[-2][2]

        if (i10, i2) == (23, 26):
            nextpiece = randsel(piece2b, rev(piece3a))
        elif (i10, i2) == (6, 13):
            nextpiece = piece1
        elif (i10, i2) == (36, 30):
            nextpiece = randsel(piece2a + [23], piece5 + piece4)
        elif (i10, i2) == (23, 27):
            nextpiece = randsel(rev(piece3a), piece2b)
        elif (i10, i2) == (5, 10):
            nextpiece = rev(piece4)
        elif (i10, i2) == (33, 31):
            nextpiece = randsel(rev(piece3b) + [23],
                                rev(piece5) + rev(piece1))

        elif (i10, i2) == (23, 19):
            nextpiece = randsel(rev(piece2a), piece3b)
        elif (i10, i2) == (23, 16):
            nextpiece = randsel(rev(piece2a), piece3b)
        elif (i10, i2) == (4, 12):
            nextpiece = randsel(piece6 + piece1, piece3a + [23])
        elif (i10, i2) == (7, 11):
            nextpiece = randsel(rev(piece6) + rev(piece4),
                                rev(piece2b) + [23])
        elif (i10, i2) == (35, 32):
            nextpiece = rev(piece1)
        elif (i10, i2) == (34, 29):
            nextpiece = piece4
        else:
            drive(0)
            return

        #print("nextpiece = %s" % str(nextpiece))

        for j in range(0, len(path)):
            (_, _, i, x, y) = path[j]
            if g.remote_control:
                print("whole4 finished")
                return
            i2 = i1
            i1 = i
            if j == len(path)-1:
                i3 = nextpiece[0]
            else:
                (_, _, i3, _, _) = path[j+1]
            send_to_ground_control("between %d %d %d" % (i2, i1, i3))
            lxprev = lx
            rxprev = rx
            lyprev = ly
            ryprev = ry
            (_, _, _, lx, ly) = lpath[j]
            (_, _, _, rx, ry) = rpath[j]
            if lxprev != None:
                if False:
                    print("keep between (%f,%f) - (%f,%f) and (%f,%f) - (%f,%f)" % (
                            lxprev, lyprev, lx, ly,
                            rxprev, ryprev, rx, ry))
                g.currentbox = [(lxprev, lyprev, lx, ly),
                                (rxprev, ryprev, rx, ry)]
            goto_1(x, y)

        # idea: let the connecting node always be a part in both
        # pieces; then we get a free check whether they actually go
        # together

        path = piece2path(nextpiece, dir, 0.25)
        lpath = piece2path(nextpiece, dir, 0.15)
        rpath = piece2path(nextpiece, dir, 0.35)

def gopath(path0, dir=-1):
    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    path = piece2path(path0, dir, 0.25)
    lpath = piece2path(path0, dir, 0.15)
    rpath = piece2path(path0, dir, 0.35)

    lx = None
    ly = None
    rx = None
    ry = None

    i1 = -1

    for j in range(0, len(path)):
        (_, _, i, x, y) = path[j]
        if g.remote_control:
            print("whole4 finished")
            return
        i2 = i1
        i1 = i
        if j == len(path)-1:
            i3 = -1
        else:
            (_, _, i3, _, _) = path[j+1]
        send_to_ground_control("between %d %d %d" % (i2, i1, i3))
        lxprev = lx
        rxprev = rx
        lyprev = ly
        ryprev = ry
        (_, _, _, lx, ly) = lpath[j]
        (_, _, _, rx, ry) = rpath[j]
        if lxprev != None:
            if False:
                print("keep between (%f,%f) - (%f,%f) and (%f,%f) - (%f,%f)" % (
                        lxprev, lyprev, lx, ly,
                        rxprev, ryprev, rx, ry))
            g.currentbox = [(lxprev, lyprev, lx, ly),
                            (rxprev, ryprev, rx, ry)]
        goto_1(x, y)


# we handle the area from y=10 to max y (19.7)
def tomiddleline():
    if abs(g.ppx - 1.5) < 0.3:
        print("already near middle line: x = %f" % g.ppx)
        return

    if g.ppy < 16:
        g.targety = g.ppy + 2.0
    else:
        g.targety = g.ppy - 2.0

    sign = 1

    if g.ppx > 1.5:
        sign = -sign

    a = g.ang%360
    if a > 180:
        a -= 360

    print("a %f" % a)

    if a < 0:
        sign = -sign

    drive(0)
    time.sleep(4)
    drive(sign*20)
    goto_1(1.5, g.targety)
    drive(0)

def tomiddleline2():
    g.detectcrashes = False

    while True:
        if g.ppx < 0.5 or g.ppx > 2.5:
            tomiddleline()

        time.sleep(2)

# assume starting at xmax, ymax, pointing W
# go to y = 12
def zigzag():
    N = 20
    y = 19.5
    dy = (19.5-12)/N
    for i in range(0, N):
        drive(0)
        time.sleep(4)
        drive(20)
        y -= dy/2
        goto_1(0.2, y)
        drive(0)
        time.sleep(4)
        drive(-30)
        y -= dy/2
        goto_1(2.2, y)
        drive(0)


def roundblock(speed = 20):
    drive(speed)
    while True:
        goto_1(8,12.8)
        goto_1(8.5,17.7)
        goto_1(2,18.2)
        goto_1(1.0,13.5)
        goto_1(2.0,12.8)

def initpos():
    g.markercnt = 0
    g.angleknown = False
    g.crash = False
    g.crashacc = None
    g.remote_control = False

def gohome():
    l = godir(g.ppx, g.ppy, g.ang, 2.5, 12, 0, None)
    print(l)
    if l == False or l == []:
        return l
    forw2 = -1
    sp = 30
    first = True
    dir0 = 0
    for (x, y, dir) in l[1:]:
        if dir != dir0:
            drive(0)
            time.sleep(4)
            drive(sp*dir)
        dir0 = dir
        print("going to %f %f %d" % (x, y, dir))
        goto_1(x, y)
    drive(0)

def calmag():
    first = True

    drive(0)
    time.sleep(4)
    steer(-100)
    drive(20)

    t0 = time.time()

    while time.time() < t0 + 20:
        w = bus.read_i2c_block_data(address, 0x4c, 6)
        print(w)
        mx = make_word(w[1], w[0])
        my = make_word(w[3], w[2])
        if first:
            g.mxmin = mx
            g.mxmax = mx
            g.mymin = my
            g.mymax = my
            first = False
        if g.mxmin > mx:
            g.mxmin = mx
        if g.mxmax < mx:
            g.mxmax = mx
        if g.mymin > my:
            g.mymin = my
        if g.mymax < my:
            g.mymax = my

    drive(0)
    print((g.mxmin, g.mxmax, g.mymin, g.mymax))

def reset():
    g.remote_control = False
    g.rc_button = False
    g.warningblinking = False
    setleds(0,7)
    g.markerno = 0
    g.markercnt = 0
    g.angleknown = False
    g.crash = False
    g.crashacc = None
    # should we reset the connect_to_ecm thread somehow?

def corridor():
    drive(0)
    time.sleep(4)
    while True:
        drive(30)
        goto_1(22, 5.1)
        drive(0)
        time.sleep(4)
        drive(-40)
        goto_1(16, 5.1)
        drive(0)
        time.sleep(4)

servotime = 0.5

def turnservo():
    plupp = 2
    while True:
        if plupp == 0:
            for i in range(-127, -100):
                steer(i)
                time.sleep(1.0/50)
            for i in range(101, 128):
                steer(i)
                time.sleep(1.0/50)

            for i in range(127, 100, -1):
                steer(i)
                time.sleep(1.0/50)
            for i in range(-101, -128, -1):
                steer(i)
                time.sleep(1.0/50)
        elif plupp == 1:
            for i in range(-127, -100, 8):
                steer(i)
                time.sleep(1.0/5)
            for i in range(101, 128, 8):
                steer(i)
                time.sleep(1.0/5)

            for i in range(127, 100, -8):
                steer(i)
                time.sleep(1.0/5)
            for i in range(-101, -128, -8):
                steer(i)
                time.sleep(1.0/5)
        elif plupp == 2:
            steer(-127)
            time.sleep(servotime)
            steer(127)
            time.sleep(servotime)

# we start at point 6
def overtake():
    drive(0)
    time.sleep(4)
    drive(20)
    while True:
        gopath([6,5,4,12,18,22,25,31,33])
        gopath([34,35],1)
        gopath([36,30,28,24,17,11,7])

