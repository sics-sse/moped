import re
import smbus
import time
import threading
import os
import subprocess
import math
import socket
import struct
import sys
import ast

import urllib
import urllib.parse
import paho.mqtt.client as mosquitto

def start_new_thread(f, args):
    threading.Thread(target=f, args=args).start()

global VIN
#VIN = sys.argv[1]

parameter = 164
parameter = 100

oldpos = dict()
adjust_t = None

paused = False

speedtime = None

speakcount = 1

targetangle = None

markerno = 0
markercnt = 0

speedsign = 1
braking = False

angleknown = False
marker = None
inspeed = 0.0
odometer = 0
lastodometer = None
age = -1

can_steer = 0
can_speed = 0

ground_control = None

targetx = None
targety = None

#TARGETDIST = 0.3
TARGETDIST = 0.15
DEFAULTSPEED = 7
TURNSPEED = 20
TOOHIGHSPEED = 2.0

R = 0.83

YMAX = 19.7

global logf
global accf

bus = smbus.SMBus(1)

address = 0x68

bus.write_byte_data(address, 0x6b, 0)
bus.read_byte_data(address, 0x75)

#bus.write_byte_data(address, 0x1a, 5)
#bus.write_byte_data(address, 0x1b, 0)

bus.write_byte_data(address, 0x1a, 1)
bus.write_byte_data(address, 0x1b, 16)

dt = 1.0/836
# when reading gyro in the main thread:
dt = 1.0/50
# when using a thread for reading gyro:
dt = 1.0/678*1.3487

# px etc. is the dead reckoning from the IMU acc and gyro
# ppx etc. is the dead reckoning from wheel speed and gyro
global px, py, pz
global ppx, ppy, ppz
global vx, vy, vz

ppx = 0
ppy = 0

#gscale = 32768.0/250
gscale = 32768.0/1000
ascale = 1670.0

angdiff = 0.0
ppxdiff = 0.0
ppydiff = 0.0
ang = 0.0
dang = None
gyron = 0

rbias = 0

t0 = None

def make_word(high, low):
    x = high*256+low
    if x >= 32768:
        x -= 65536
    return x

def readgyro():
    while True:
        tolog("starting readgyro")
        readgyro0()

def readgyro0():
    global ang, dang
    global gyron
    global t0
    global px, py, pz
    global ppx, ppy, ppz
    global vx, vy, vz
    global angdiff, ppxdiff, ppydiff

    try:

        tlast = time.time()
        t1 = time.time()

        while True:
            gyron += 1

            high = bus.read_byte_data(address, 0x47)
            low = bus.read_byte_data(address, 0x48)
            r = make_word(high, low)

            r -= rbias

            high = bus.read_byte_data(address, 0x45)
            low = bus.read_byte_data(address, 0x46)
            ry = make_word(high, low)

            high = bus.read_byte_data(address, 0x43)
            low = bus.read_byte_data(address, 0x44)
            rx = make_word(high, low)

            # make the steering and the angle go in the same direction
            # now positive is clockwise
            r = -r

            t2 = time.time()
            dt = t2-t1
            t1 = t2

            dang = r/gscale*dt
            ang += dang

            w = bus.read_i2c_block_data(address, 0x3b, 6)
            x = make_word(w[0], w[1])
            x -= xbias
            y = make_word(w[2], w[3])
            y -= ybias
            z = make_word(w[4], w[5])
            z -= zbias

            x /= ascale
            y /= ascale
            z /= ascale

            x0 = -x
            y0 = -y
            z0 = z

            # the signs here assume that x goes to the right and y forward

            x = x0*math.cos(math.pi/180*ang) - y0*math.sin(math.pi/180*ang)
            y = x0*math.sin(math.pi/180*ang) + y0*math.cos(math.pi/180*ang)

            vx += x*dt
            vy += y*dt
            vz += z*dt

            px += vx*dt
            py += vy*dt
            pz += vz*dt

            corr = 1.0

            vvx = inspeed*corr/100.0*math.sin(math.pi/180*ang)
            vvy = inspeed*corr/100.0*math.cos(math.pi/180*ang)

            ppx += vvx*dt
            ppy += vvy*dt

            t2_10 = int(t2*10)/10.0
            oldpos[t2_10] = (ppx, ppy, ang)

            # don't put too many things in this thread

            accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
                    x, y, vx, vy, px, py, x0, y0, vvx, vvy, ppx, ppy, ang,
                    dang, can_steer, can_speed, inspeed, outspeed, odometer,
                    z0, r, rx, ry))

            
            if (t2-tlast > 0.1):
                tolog0("")
                tlast = t2

            j = angdiff/1000
            ang += j
            angdiff -= j

            #print("pp diff %f %f" % (ppxdiff, ppydiff))

            j = ppxdiff/100
            ppx += j
            ppxdiff -= j

            j = ppydiff/100
            ppy += j
            ppydiff -= j


    except Exception as e:
        tolog("exception in readgyro: " + str(e))


def tolog2(str0, stdout):

    if targetx and targety:
        d = dist(ppx, ppy, targetx, targety)
    else:
        d = -1

    str = "(speed %d time %.3f %f %f %f %f %f %3f) %s" % (
        inspeed,
        time.time() - t0,
        ppx,
        ppy,
        ang,
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
#badmarkers = [7]

battery = 0.0
ultra = 0.0
can_ultra = 0.0

global mqttc

global gototarget
gototarget = None

def on_message(mosq, obj, msg):
    global battery, ultra

    p = str(msg.payload)
    while p[-1] == '\n' or p[-1] == '\t':
        p = p[:-1]

    # ignore our own position
    if VIN in msg.topic:
        return

    #sys.stdout.write(msg.topic + " " + str(msg.qos) + " " + p + "\n")
# "adc","current_value":"7.7142 7.630128199403445"

    m = re.search('"adc","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"adc","current_value":"[0-9.]+ ([0-9.]+)"', p)
            if m:
            #print("battery %s" % m.group(1))
                battery = float(m.group(1))
                send_to_ground_control("battery %f" % battery)
        return

    # We still read this, but we don't use 'ultra' - we use 'can_ultra'
    m = re.search('"DistPub","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"DistPub","current_value":"[0-9]+ ([0-9]+)"', p)
            if m:
                ultra = float(m.group(1))
        return

    # assume it's a position
    pl = p.split(" ")
    try:
        x = float(pl[0])
        y = float(pl[1])
        #goto(x, y)
    except Exception as e:
        pass

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
    mqttc.publish("/sics/moped/position/%s" % VIN, "%f %f" % (x, y))
    pass

def handle_mqtt_old():
    # improve this: handle if the server resets us, for example
    rc = 0
    while rc == 0:
        rc = mqttc.loop()
    print("rc: " + str(rc))

def handle_mqtt():
    global mqttc

    mqtt_init()

    i = 0
    rc = 0
    while rc == 0:
        rc = mqttc.loop(5.0)
        i += 1

    if rc == 7:
        mqttc = mosquitto.Mosquitto()
        mqtt_init()


def readmarker():
    while True:
        tolog("starting readmarker")
        try:
            readmarker0()
        except Exception as e:
            tolog("readmarker exception %s" % str(e))

def readmarker0():
    global marker, age, lastmarker0, markermsg
    global px, py
    global ppx, ppy
    global ang
    global lastpos, lastpost
    global angleknown
    global markerno
    global markercnt
    global ppxdiff, ppydiff, angdiff
    global adjust_t

    while True:
        p = subprocess.Popen("tail -1 /tmp/marker0", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()
        m = res[0].decode('ascii')
        m = m.split('\n')[0]
        if m == lastmarker0:
            tolog0("no new marker0")
            continue

        lastmarker0 = m

        tolog0("marker0 %s age %d" % (m, age))
        m1 = m.split(" ")
        if m1 == "":
            age += 1
        else:
            t = time.time()

            doadjust = False

            markerno = int(m1[0])
            x = float(m1[1])
            y = float(m1[2])
            quality = float(m1[4])
            ori = float(m1[3])
            odiff = ori - (ang%360)
            if odiff > 180:
                odiff -= 360
            if odiff < -180:
                odiff += 360
            accepted = False
#            if angleknown and abs(odiff) > 45.0 and markerno != -1:
#                tolog("wrong marker %d %f" % (markerno, odiff))
#                markerno = -1
            if (markerno > -1 and quality > 0.25 and markerno not in badmarkers):
                close = True
                if not angleknown:
                    ang = ori
                angleknown = True

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
                if adjust_t and it0 < adjust_t and False:
                    tolog0("POS: picture too old, we already adjusted %f %f" % (
                            it0, adjust_t))
                    send_to_ground_control("mpos %f %f %f %f 0 %f" % (x,y,ang,time.time()-t0, inspeed))
                    continue
                elif it0_10 in oldpos:
                    (thenx, theny, thenang) = oldpos[it0_10]
                    doadjust = True
                    tolog0("POS: position then: %f %f" % (thenx, theny))
                else:
                    tolog0("POS: can't use oldpos")                    
                    continue

                if True:
                    if lastpos != None:
                        (xl, yl) = lastpos
                        dst = dist(thenx, theny, xl, yl)
                        tolog0("local speed %f" % (dst/(t-lastpost)))
                        if dst/(t-lastpost) > TOOHIGHSPEED:
                            close = False

                dst = dist(ppx, ppy, x, y)
                # even if somewhat correct: it causes us to lose
                # position when we reverse
                if dst > 2.0 and markercnt > 10:
                    close = False
                tolog0("marker dist %f" % dst)

                if not close:
                    msg = "bad marker %d not close" % markerno
                    if msg != markermsg:
                        tolog(msg)
                        markermsg = msg
                    age += 1
                else:
                    accepted = True
                    markercnt += 1
                    tolog0("marker1 %s %d %f %f" % (m, age, ang, ori))
                    if doadjust:
                        doadjust_n = 1
                    else:
                        doadjust_n = 0
                    send_to_ground_control("mpos %f %f %f %f %d %f" % (x,y,ang,time.time()-t0, doadjust_n, inspeed))
                    #send_to_mqtt(x, y)
                    lastpos = (thenx,theny)
                    px = x
                    py = y
                    if True:
#                    if markercnt % 10 == 1:
#                    if markercnt == 1:
                        if doadjust:
                            adjust_t = time.time()
                            tolog0("adjusting pos %f %f -> %f %f" % (ppx, ppy,
                                                                     x, y))
                        if markercnt != 1:
                            angdiff = (ori-ang)%360
                            if True:
                                if doadjust:
                                    tolog0("old pp diff %f %f" % (
                                            ppxdiff, ppydiff))
                                    ppxdiff = x-ppx
                                    ppydiff = y-ppy

                                    ppxdiff = x-thenx
                                    ppydiff = y-theny
                                    angdiff = (ori-thenang)%360

                                    ppxdiff /= 2
                                    ppydiff /= 2
                                    #angdiff /= 2
                            else:
                                ppx = x
                                ppy = y
                            if angdiff > 180:
                                angdiff -= 360
                        else:
                            ppx = x
                            ppy = y
                    #vx = math.sin(ang*math.pi/180)*inspeed/100
                    #vy = math.cos(ang*math.pi/180)*inspeed/100
                    lastpost = it0
                    marker = m
                    age = 0
                    #ang = ori
            else:
                age += 1
            if not accepted:
                send_to_ground_control("badmarker %f %f" % (x,y))
                tolog0("marker5 %s %d %f %f" % (m, age, ang, ori))
#            tolog0("marker2 %d %f %f %d %f %d %f" % (-1, px, py, int(ang), 0.5, age, ang))
#            tolog0("marker3 %d %f %f %d %f %d %f" % (-1, ppx, ppy, int(ang), 0.5, age, ang))

# The code here is a bit ad-hoc. We need to find out why the various
# constants and offsets appear.
def readspeed2():
    global inspeed, odometer, lastodometer
    global speedsign
    global can_steer, can_speed
    global can_ultra

    part = b""
    part2 = b""
    while True:
        data = canSocket.recv(1024)
        if (data[0], data[1]) == (100,4):
            if data[8] == 16:
                parts = str(part)
                m = re.search("rear speed x([0-9 ]+)x([0-9 ]+)", parts)
                if m:
                    inspeed = int(m.group(1))
                    inspeed *= speedsign

                    if (inspeed == 0 and speedtime != None and
                        time.time() - speedtime > 7.0):
                        speak("obstacle")
                        send_to_ground_control("obstacle")
                        drive(0)

                    odometer = int(m.group(2))
                    if odometer != lastodometer:
                        send_to_ground_control("odometer %d" % (odometer))
                        lastodometer = odometer
                    #print("sp-odo 1 %d %d" % (sp, odo))
                part = b""
            part += data[9:]
        elif (data[0], data[1]) == (1,1):
            #print("CAN %d %d" % (data[8], data[9]))
            sp = data[8]
            if sp > 128:
                sp -= 256
            can_speed = sp
            if not braking:
                if sp < 0:
                    speedsign = -1
                elif sp > 0:
                    speedsign = 1
            st = data[9]
            if st > 128:
                st -= 256
            can_steer = st
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
                part2 = b""
            part2 += data[9:]
            

def report():
    while True:
        time.sleep(2)
        print(marker + " " + str(inspeed))

outspeed = 0.0
steering = 0.0

# error here: we normalize the argument, but not the other value

def drive(sp):
    global outspeed
    global speedsign
    global speedtime

    # do this in readspeed2 instead
    # maybe, but then steer will zero the speed
    outspeed = sp

    if abs(sp) >= 7:
        speedtime = time.time()
    else:
        speedtime = None

    if sp != 0 and not braking:
        speedsign = sign(sp)

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

def steercmd(cmd):
    os.system(cmd)
    tolog0("motor2 done %s" % cmd)

def steer(st):
    global steering

    steering = st
    sp = outspeed
    if st < 0:
        st += 256
    if sp < 0:
        sp += 256
    tolog("motor %d steer %d" % (outspeed, st))
    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
        sp, st)
    #print (outspeed, st, cmd)
    start_new_thread(steercmd, (cmd,))
#    os.system(cmd)
    tolog("motor2 %d steer %d" % (outspeed, st))

def stop(txt = ""):
    global steering, outspeed
    global speedtime

    steering = 0.0
    outspeed = 0.0

    speedtime = None

    tolog("(%s) motor %d steer %d" % (txt, outspeed, steering))
    os.system("/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
            outspeed, steering))

def connect_to_ground_control():
    global ground_control

    while True:
        if not ground_control:
            s = open_socket()
            if not s:
                print("no connection")
            else:
                print("connection opened")
                ground_control = s
                start_new_thread(from_ground_control, ())
                send_to_ground_control("info %s" % VIN)
        time.sleep(5)

def from_ground_control():
    global path
    global paused
    global parameter

    while True:
        if ground_control:
            data = ground_control.recv(1024)
            data = data.decode('ascii')
            #print(data)
            l = data.split(" ")
            #print(l)
            #print(data)
            if l[0] == "go":
                x = float(l[1])
                y = float(l[2])
                print(("goto", x, y))
                goto(x, y)
            elif l[0] == "path":
                path = ast.literal_eval(data[5:])
                print(path)
            elif l[0] == "continue":
                paused = False
            elif l[0] == "parameter":
                parameter = int(l[1])
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

def heartbeat():
    while True:
        send_to_ground_control("heart")
        time.sleep(5)

def init():
    global VIN
    global logf, goodmarkers, markermsg, rbias, xbias, ybias, zbias, t0
    global px, py, pz
    global ppx, ppy, ppz
    global vx, vy, vz
    global accf
    global angleknown

    VIN = readvin()
    print("VIN %s" % VIN)

    angleknown = False

    start_new_thread(connect_to_ground_control, ())

    logf = open("navlog", "w")
    accf = open("acclog", "w")
    #accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
    #x, y, vx, vy, px, py, x0, y0, vvx, vvy, ppx, ppy, ang))
    accf.write("x y vx vy px py x0 y0 vvx vvy ppx ppy ang dang steering speed inspeed outspeed odometer z0 r rx ry\n")

    t0 = time.time()

    tolog("init")

    rbias = 0
    xbias = 0
    ybias = 0
    zbias = 0

    # computing angbias would be better
    ncalibrate = 100
    for i in range(0, ncalibrate):
        high = bus.read_byte_data(address, 0x47)
        low = bus.read_byte_data(address, 0x48)
        r = make_word(high, low)
        rbias += r

        w = bus.read_i2c_block_data(address, 0x3b, 6)
        x = make_word(w[0], w[1])
        y = make_word(w[2], w[3])
        z = make_word(w[4], w[5])
        xbias += x
        ybias += y
        zbias += z


    rbias = rbias/float(ncalibrate)
    xbias = xbias/float(ncalibrate)
    ybias = ybias/float(ncalibrate)
    zbias = zbias/float(ncalibrate)

    print("rbias = %f, xbias = %f, ybias = %f, zbias = %f" % (rbias, xbias, ybias, zbias))


    px = 0.0
    py = 0.0
    pz = 0.0

    ppx = 0.0
    ppy = 0.0
    ppz = 0.0

    vx = 0.0
    vy = 0.0
    vz = 0.0

    goodmarkers = []

    start_new_thread(readmarker, ())
    start_new_thread(handle_mqtt, ())
    start_new_thread(readspeed2, ())
    start_new_thread(readgyro, ())
    start_new_thread(keepangle, ())
    start_new_thread(heartbeat, ())
    #start_new_thread(report, ())

def keepangle():
    while True:
        time.sleep(0.1)
        if targetangle == None:
            continue

        adiff = (targetangle-ang)%360
        if adiff > 180:
            adiff -= 360
        print("adiff %f" % adiff)
        s = sign(adiff)
        st = 10*abs(adiff)
        if st > 90:
            st = 90
        steer(st*s*speedsign - 10)

# treats the car as a point
def inarea(x, y):
    xmax = 3.0
    ymax = YMAX

    if y > 17.0 and y < 19.0 and x < 9.5:
        return True

    if x < 0 or x > xmax:
        return False
    if y < 0 or y > ymax:
        return False

    if y < 1.0:
        return False
    if y < 4.0 and x > 2.0:
        return False
    # this should not be a rectangle (the leftmost point is at x=1.2)
    if y > 13.5 and y < 17.0 and x > 1.5:
        return False
    # we ought to cut out something here: the boundary is not a rectangle

    if dist(x, y, 2.5, 9.5) < 0.4:
        return False

    return True



# compute a path from (x1,y1) with orientation ang1, to (x2,y2)
# a "path" is a circular arc
# Next: use information from a map so that we don't go outside the safety
# margins of the map.

def comppath(x1, y1, ang1, x2, y2):
    pass

def dist(x1, y1, x2, y2):
    return math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

def sign(x):
    if x < 0:
        return -1
    if x > 0:
        return 1
    return 0

# special case: the northern wall
# assume we turn at maximum steering (90)
def checkwall():
    m = marker.split(" ")
    y = float(m[2])
    dd = YMAX - y
    if dd > 2*R:
        return
    if dd > R and abs(steering) < 90:
        return
    a = ang % 360
    if a > 180:
        a -= 360
    d = R*(1-sign(steering)*math.sin(math.pi/180*a))
    if dd < d:
        tolog("will hit the wall %f %f %f %f" % (dd, d, steering, ang))

def getdist(x2, y2):
    if False:
        m = marker.split(" ")
        x1 = float(m[1])
        y1 = float(m[2])
    else:
        # NEW
        x1 = ppx
        y1 = ppy

    d = dist(x1, y1, x2, y2)
    tolog("we are at (%f, %f), distance to (%f, %f) is %f" % (
            x1, y1, x2, y2, d))

#    checkwall()
    return d

# rot > 0 -> turn left
# a0 = current ang
def check_circle(x0, y0, a0, rot):
    obs = False

    if rot < 0:
        for i in range(0, int(-rot)):
            a = a0+i
            x = x0 + R*math.sin(math.pi/180*a)
            y = y0 + R*math.cos(math.pi/180*a)
            if not inarea(x, y):
                obs = True
                break
    else:
        for i in range(0, int(rot)):
            a = a0-i
            x = x0 + R*math.sin(math.pi/180*a)
            y = y0 + R*math.cos(math.pi/180*a)
            if not inarea(x, y):
                obs = True
                break
    if obs:
        print("obstacle if turning %f" % rot)
    return obs

# Add to marker: how long ago we got an acceptable reading

# Give commands to follow the path. Current position and speed and
# orientation are relevant.
# Currently, we ignore speed

def makepath(x2, y2):
    global ang, angdiff
    m = marker.split(" ")
    x1 = float(m[1])
    y1 = float(m[2])

    tolog0("makepath %f %f to %f %f" % (x1, y1, x2, y2))

    # NEW
    #x1 = ppx
    #y1 = ppy

    ori = float(m[3])
    #print ('angles', ang, ori)
    ori1 = ang
    a = math.atan2(y2-y1, x2-x1)
    adeg = 180/math.pi*a
    adeg = 90-adeg
    adiff = (ori1-adeg+360)%360
    if adiff > 180:
        adiff -= 360
    angdiff = (ori-ang)%360
    if angdiff > 180:
        angdiff -= 360
    tolog0("angles %f %f %f %f %f %d" % (
            ori, ang, adeg, adiff, angdiff, int(m[0])))

    if adiff > 0:
        adiff2 = adiff-360
    else:
        adiff2 = adiff+360
    obs1 = check_circle(x1, y1, ang, adiff)
    if False:
        if obs1:
            obs2 = check_circle(x1, y1, ang, adiff2)
            if obs2:
                return False
            else:
                adiff = adiff2

    return adiff

# find the center of the circle which the car will go in if turning now
# radius of smallest possible circle is known = 83 cm
def smallcircle(dir):
    m = marker.split(" ")
    x1 = float(m[1])
    y1 = float(m[2])
    ang = float(m[3])

    # ang = 0 means growing y, and growing ang goes clockwise
    a = math.pi/180*(ang + dir*90)
    x = x1 + R*math.sin(a)
    y = y1 + R*math.cos(a)
    return (x, y)

# angle = the amount to turn
# ang is supposed to contain the correct orientation
def maketurn(angle, speed = TURNSPEED):
    if angle == 0:
        return True

    target = ang + angle
    t1 = time.time()

    if angle < 0:
        steer(-90)
    else:
        steer(90)

    # best to set both at the same time?
    drive(speed)

    # busy loop, do in a thread instead?
    # if we are not done in 30 s, give up
    tolog("turning %d ang %f target %f" % (angle, ang, target))
    while time.time() < t1 + 30:
        adiff = ang - target
        adiff = adiff % 360
        if adiff > 180:
            adiff -= 360
        if adiff < -180:
            adiff += 360
        #tolog("turning1 ang %f adiff %f" % (ang, adiff))
        if abs(adiff) < 10:
            return True
    return False

# angle = the direction to face
def goangle(angle, speed = TURNSPEED):

    target = angle
    t1 = time.time()

    if angle < 0:
        steer(-90)
    else:
        steer(90)

    # best to set both at the same time?
    drive(speed)

    # busy loop, do in a thread instead?
    # if we are not done in 30 s, give up
    tolog("turning %d ang %f target %f" % (angle, ang, target))
    while time.time() < t1 + 30:
        adiff = ang - target
        adiff = adiff % 360
        if adiff > 180:
            adiff -= 360
        if adiff < -180:
            adiff += 360
        #tolog("turning1 ang %f adiff %f" % (ang, adiff))
        if abs(adiff) < 10:
            return True
    return False


def turn(x2, y2, nostop, maxst, limit):
    tolog("turn %f %f" % (x2, y2))
    adiff = 0.0
    for i in range(0,100):
        tolog("turn-%d %f %f" % (i, x2, y2))
        d = getdist(x2, y2)
        if d < TARGETDIST:
            tolog("reached target (distance %f)" % d)
            break
        tolog("turn1 %d" % (i))
        adiff = makepath(x2, y2)
        tolog("turn2 %d" % (i))
        if adiff == False:
            tolog("can't turn towards target")
            stop()
            return False

#        if adiff > 180:
#            adiff -= 360
# here, adiff may be between -360 and 360
        if adiff > 0:
            turn = -1
        else:
            turn = 1

        tolog("adiff = %f, turning %d" % (adiff, turn))

        if False:
            (cx, cy) = smallcircle(turn)

            # not complete
            # in particular, too restrictive: we should only check the
            # part of the circle where we would be going
            if (not inarea(cx+R, cy+R) or
                not inarea(cx-R, cy+R) or
                not inarea(cx+R, cy-R) or
                not inarea(cx-R, cy-R)):
                print("can't turn!")
                stop("can't turn")

                return False

        p = 6.0
        #steer(turn*maxst)
        str = turn*min(p*abs(adiff), 80)
        str += -10
        tolog("steering %d adiff %d" % (str, adiff))
        steer(str)
        #print adiff
        # wrong: adiff will be for example 359, not -1
        # so correct it
        if abs(adiff) < limit or abs(adiff-360) < limit:
            if not nostop:
                stop("1")
            break

        tolog("turn3 %d" % (i))

        if inspeed == 0.0 and i > 10:
            tolog("speed is 0 (while turning), obstacle?")
            stop("2")
            return False

    tolog("turned (%d steps) %f %f, adiff = %f, dist = %f" % (
            i, x2, y2, adiff, d))
    return True

def gotoc(x2, y2, speed = DEFAULTSPEED):
    return goto0(x2, y2, True, speed)

def goto(x2, y2, speed = DEFAULTSPEED):
    global gototarget

    if gototarget != None:
        print("Already going somewhere")
        return False

    gototarget = (x2, y2)
    s = goto0(x2, y2, False, speed)
    gototarget = None
    return s

def goto0(x2, y2, nostop, speed):

    if not inarea(x2, y2):
        print("goal point (%f,%f) is outside area" % (x2, y2))
        stop("3")
        return False

    tolog("goto %f %f speed %d" % (x2, y2, speed))
    drive(speed)
    maxst = 90
    limit = 20
    loops = 0
    while True:
        loops += 1
        tolog("goto: loop %d" % loops)

        # See if a straight line from here to the goal crosses an obstacle
        # primitive: just look at 100 intermediate points
        m = marker.split(" ")
        x1 = float(m[1])
        y1 = float(m[2])
        n = 100
        dx = (x2-x1)/n
        dy = (y2-y1)/n
        for i in range(0, n):
            x = x1 + i*dx
            y = y1 + i*dy
            if not inarea(x, y):
                tolog("going from (%f, %f) to (%f, %f): object in the way at (%f, %f)" % (
                        x1, y1, x2, y2, x, y))
                stop("4")
                return False

        cont = turn(x2, y2, True, maxst, limit)
        if not cont:
            stop("5")
            return False

        #steer(0)

        maxst = 50
        limit = 10

        dist = getdist(x2, y2)
        if dist < TARGETDIST:
            tolog("reached target (distance %f)" % dist)
            if not nostop:
                stop("6")
            return True

        # better to use time than number of loops
        # todo: often, if it hits the wall to the left and then changes to turn
        # to the right, it gets away, so don't give up too fast
        if inspeed == 0.0 and loops > 10:
            tolog("speed is 0 (loop %d), obstacle?" % loops)
            stop("7")
            return False

        # todo: detect that we have gone outside the area, or will do so,
        # and do something about it

# d > 0
def backup(d):
    drive(-10)
    godist(d)
    stop()

def godist(d):
    loops = 0
    o1 = odometer
    o2 = o1 + d*5/(math.pi*10.2/100)
    while True:
        loops += 1
        if outspeed == 0.0:
            tolog("motor needed in godist")
            return False
        if inspeed == 0.0 and loops > 20:
            tolog("speed 0 in godist; obstacle?")
            return False
        o = odometer
        if o >= o2:
            return True
        time.sleep(0.1)

def trip1():
    while True:
        s = goto(0.5,17) and goto(1.5,19) and goto(2,18) and goto(0.7,17) and goto(1,11) and goto(0.5,6) and goto (1,4.5) and goto(2,5) and goto(2.5,5) and goto(2,7) and goto(0.5,9)
        if not s:
            break


def trip2():
    while True:
        s = gotoc(0.5,18.5) and maketurn(220) and gotoc(0.7,17) and gotoc(0.6,11) and gotoc(0.5,6) and maketurn(-180) and gotoc(0.5,9)
        if not s:
            stop("8")
            break

def trip3():
    while True:
        s = gotoc(0.5,18.5) and gotoc(1.7,19.3) and gotoc(2.5,18.3) and gotoc(1.6,17.4) and gotoc(1.1,16.2) and gotoc(1.5,13) and gotoc(2.5,12) and gotoc(2.5,10.5) and gotoc(2.0,10.0) and gotoc(2.0,8.5) and gotoc(2.5,7) and gotoc(2.5,5) and gotoc(2.5,5) and gotoc(1.0,5)
        if not s:
            stop("8")
            break

def trip4():
    s = gotoc(0.5,17.0) and gotoc(2.5,18.0) and gotoc(6.5,18.0)
    stop()


path5 = [("gotoc", 0.7, 18.5),
         ("maketurn", 220),
         ("gotoc", 0.6, 15),
         ("gotoc", 0.7, 6),
         ("maketurn", -180),
         ("gotoc", 0.7, 9)]

def trip5(p = 0):
    while True:
        for pp in path5[p:]:
            if pp[0] == "gotoc":
                s = gotoc(pp[1], pp[2])
            elif pp[0] == "maketurn":
                s = maketurn(pp[1])
            if not s:
                stop("8")
                return
        p = 0

def trip6():
    while True:
        s = (gotoc(1.0, 11.5) and
             gotoc(2.5, 10.5) and
             gotoc(1.0, 9.5) and
             gotoc(2.5, 7.0) and
             gotoc(1.5, 5.0))             
        if not s:
            stop("8")
            break

import socket
import sys

HOST = 'localhost'    # The remote host
HOST = '192.168.43.73'
PORT = 50008              # The same port as used by the server

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

def send_to_ground_control(str):
    global ground_control

    if not ground_control:
        return

    try:
        str1 = str + "\n"
        ground_control.send(str1.encode('ascii'))
    except Exception as e:
        print("send1 %s" % e)
        ground_control = None
#        connect_to_ground_control()

def averagepos():
    pxa = 0
    pya = 0
    n = 0
    cnt = markercnt
    while True:
        if markercnt > cnt:
            n += 1
            pxa += px
            pya += py
            print("%.2f %.2f" % (pxa/n, pya/n))
            cnt = markercnt

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


def trip10(n = 1):
    global targetangle

    targetangle = 90.0

    for i in range(0, n):

        drive(7)
        time.sleep(20)
        stop()
        time.sleep(2)
        drive(-10)
        time.sleep(15)
        stop()

    targetangle = None

def goto_with_angle(x, y, angt = 90):
    while True:
        dist = getdist(x, y)
        if dist < TARGETDIST:
            #stop("9")
#            drive(-1)
#            time.sleep(0.2)
            drive(0)
            return

        tolog("gotoa1 %f %f -> %f %f" % (ppx, ppy, x, y))

        a = math.atan2(y-ppy, (x-ppx)/6)
        adeg = 180/math.pi*a
        adeg = 90-adeg

        adiff = ang - adeg
        adiff = adiff%360

        tolog("gotoa2 a %f adeg %f adiff %f" % (a, adeg, adiff))

        if speedsign < 0:
            adiff += 180

        if adiff > 180:
            adiff -= 360

        adiff = -adiff
        # now, positive means the target is to the right of us

        tolog("gotoa3 adiff %f" % (adiff))

        #print(adiff)

        asgn = sign(adiff)
        aval = abs(adiff)

        p = 2.0

        st = p*aval
        if st > 90:
            st = 90
        st = asgn*speedsign*st
        steer(st)
        tolog("gotoa4 steer %f" % (st))

        send_to_ground_control("dpos %f %f %f %f 0 %f" % (ppx,ppy,ang,time.time()-t0, inspeed))

        time.sleep(0.1)

def goto_1(x, y):
    global targetx, targety

    targetx = x
    targety = y

    missed = False
    inc = 0
    inc2 = 0
    lastdist = None
    brake_s = 0.0

    while True:
        dist = getdist(x, y)
        if inspeed != 0:
            # Assume we are going in the direction of the target.
            # At low speeds, braking time is about 1.5 s.
            brake_s = 1.5 * abs(inspeed)/100

        # say that braking distance is 1 dm at higher speed, when
        # braking electrically
        if inspeed > 0:
            brake_s = 0.4
        else:
            brake_s = 0.6

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

        if dist < TARGETDIST or dist < brake_s or missed:
            if False:
                #stop("9")
    #            drive(-1)
                # continue a little so it can pass the target if it wasn't
                # there yet
                time.sleep(0.5)
    #            drive(-1)
    #            time.sleep(0.2)
                drive(0)
            return

        tolog("gotoa1 %f %f -> %f %f" % (ppx, ppy, x, y))

        a = math.atan2(y-ppy, x-ppx)
        adeg = 180/math.pi*a
        adeg = 90-adeg

        adiff = ang - adeg
        adiff = adiff%360

        tolog("gotoa2 a %f adeg %f adiff %f" % (a, adeg, adiff))

        if speedsign < 0:
            adiff += 180

        if adiff > 180:
            adiff -= 360

        adiff = -adiff
        # now, positive means the target is to the right of us

        tolog("gotoa3 adiff %f" % (adiff))

        #print(adiff)

        asgn = sign(adiff)
        aval = abs(adiff)

        p = 2.0

        st = p*aval
        if st > 90:
            st = 90
        st = asgn*speedsign*st
        steer(st)
        tolog("gotoa4 steer %f" % (st))

        send_to_ground_control("dpos %f %f %f %f 0 %f" % (ppx,ppy,ang,time.time()-t0, inspeed))

        time.sleep(0.1)

# many assumptions: we are at a point ppx < x0 and ppy < y0,
# far enough away so that the original angle doesn't matter (but it's between
# 0 and 90),
# we are to stop at the angle 90
def gopoint(x0, y0):
    global braking

    R1 = R
    if speedsign < 0:
        R1 = 1.2

    tolog("real target %f %f" % (x0, y0))

    # kludge right now
    sg = speedsign

    a = math.atan2(sg*(y0-ppy-sg*R1), sg*(x0-ppx))

    x = x0 - sg*R1*math.sin(a)
    y = y0 - sg*R1 + sg*R1*math.cos(a)

    tolog("first target %f %f %f" % (x, y, a))
    goto_1(x, y)

    tolog("second target %f %f" % (x0, y0))
    if True:
        goto_1(x0, y0)
        drive(0)
    else:

        steer(90*speedsign)

        missed = False
        inc = 0
        inc2 = 0
        lastdist = None
        while True:
            dist = getdist(x0, y0)
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

            # worst so far: 0.12
            if dist < 0.15 or missed:
                if True:
                    #stop("9")
    #                drive(-1)
                    # continue a little so it can pass the target if it wasn't
                    # there yet
    #                time.sleep(0.5)
                    braking = True
                    drive(-1)
                    time.sleep(0.2)
                    drive(0)
                    braking = False
                return

def trip11():
    while True:
        drive(7)
        goto_with_angle(2.0, 8.2)
        time.sleep(2)
        drive(-10)
        goto_with_angle(1.0, 7.2)
        time.sleep(2)

def trip12():
    while True:
        drive(7);goto_with_angle(5.0, 5.0)
        drive(7);goto_with_angle(7.0, 5.0)
        drive(7);goto_with_angle(8.0, 7.5)
        drive(7);goto_with_angle(8.0, 12.5)
        drive(7);goto_with_angle(1.5, 13)
        drive(7);goto_with_angle(1.0, 5.5)

def trip13():
    while True:
        drive(7)
        goto_with_angle(2.5, 12.0)
        time.sleep(1)
        drive(-10)
        goto_with_angle(0.7, 10.0)
        time.sleep(1)

def stopx(i, t = 3.0):
    global braking

    braking = True

    dir = sign(inspeed)

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

    braking = False

def trip14():
    i = 0
    sp1 = 7
    sp2 = -10

#    sp1 = 20
#    sp2 = -25

    while True:
        i += 1

        drive(sp1);goto_1(1.5,6);

        drive(sp1);goto_1(1.5,10);
        #stopx(i)

        drive(sp1);goto_1(1.5,12);

        drive(sp1);goto_1(2.5,12);
        stopx(i)

        drive(sp2);goto_1(1.5,12);
        drive(sp2);goto_1(1.5,10);

        drive(sp2);goto_1(1.5,6);

        drive(sp2);goto_1(2.5,6);

        stopx(i)

def trip15():
    i = 0
    sp1 = 7
    sp2 = -10

#    sp1 = 20
#    sp2 = -25

    while True:
        i += 1

        drive(sp1);goto_1(1.8,19);
        stopx(i)

        drive(sp2);goto_1(1.8,14);
        stopx(i)

def trip15b():
    i = 0
    sp1 = 7
    sp2 = -10

#    sp1 = 20
#    sp2 = -25

    while True:
        i += 1

        drive(sp1);goto_1(1.0,12);
        stopx(i)

        drive(sp2);goto_1(1.0,7);
        stopx(i)

def trip16():
    i = 0
    sp1 = 7
    sp2 = -10

#    sp1 = 20
#    sp2 = -25

    drive(sp1);goto_1(1.0, 5.7);
    drive(sp1);goto_1(2.6, 5.1);
    drive(sp1);goto_1(20.0, 5.1);
    stopx(i)

def dospeak(s, p):
    global speakcount

    if '#' in s:
        s = s.replace('#', str(speakcount))
    os.system("espeak -a500 -p%d '%s' >/dev/null 2>&1" % (p, s))

def speak(str):
    p = 50
    if VIN == "car2":
        p = 80
    start_new_thread(dospeak, (str, p))

def trip17():
    i = 0
    sp1 = 7
    sp2 = -10

#    sp1 = 20
#    sp2 = -25

    while True:

        drive(sp1);goto_1(1.8, 16.4);
        drive(sp1);goto_1(2.4, 17.5);
        drive(sp1);goto_1(1.5, 19);
        speak('loading')
        stopx(i)
        speak('loading done')

        drive(sp1);goto_1(0.6, 18);
        drive(sp1);goto_1(0.8, 14.4);
        # when I had sp2 by mistake before stopping, it stopped and
        # then didn't continue
        stopx(i)
        drive(sp2);goto_1(2.6, 15.0);
        # make it say its thing just when we have stopped and not before
        speak('dumping')
        stopx(i)
        speak('dumping done')

def trip(path, first=0):
    global paused
    global speakcount

    speakcount = 1

    i = 0
    while True:
        j = 0
        if first > 0:
            path1 = path[first:]
        else:
            path1 = path
        for cmd in path1:
            if cmd[0] == 'go':
                sp = cmd[1]
                sp = int(sp*parameter/100.0)
                x = cmd[2]
                y = cmd[3]
                drive(sp)
                goto_1(x, y)
            elif cmd[0] == 'stop':
                if len(cmd) > 1:
                    t = float(cmd[1])
                else:
                    t = 3
                paused = True
                stopx(i, t)
                send_to_ground_control("stopat %d" % j)
                while paused:
                    time.sleep(1)
            elif cmd[0] == 'speak':
                speak(cmd[1])
            else:
                print("unknown path command: %s" % cmd)
            j += 1
        first = 0
        speakcount += 1



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

