import re
import time
import os
import subprocess
import socket
import sys
import ast
import json

import eight

import nav_imu

import nav_log
from nav_log import tolog, tolog0

from nav_util import sign, dist, start_new_thread

import nav_mqtt
import nav_tc
from nav_tc import send_to_ground_control

import nav_signal
from nav_signal import signal, blinkleds, warningblink, setleds, speak

import nav_comm

import wm

import random

from math import pi, cos, sin, sqrt, atan2, acos, asin, log


class globals:
    pass

g = globals()

nav_imu.g = g
nav_log.g = g
nav_mqtt.g = g
nav_tc.g = g
nav_signal.g = g
nav_comm.g = g
wm.g = g

g.s = None

g.bus = nav_imu.bus
g.imuaddress = nav_imu.imuaddress

g.VIN = None
#g.VIN = sys.argv[1]

g.parameter = 164
g.parameter = 100
g.parameter = 152
g.parameter = 120

g.allangles = False

wm.wminit()

# setting this to True is the same as setting goodmarkers to []?
g.ignoremarkers = False

g.detectcrashes = False
g.detectcrashes = True

#g.oldpos = dict()
g.oldpos = None
g.adjust_t = None

g.paused = False

g.speedtime = None

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

g.logf = None
g.accf = None


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


g.markermsg = None

g.lastmarker0 = None

g.lastpos = None
g.lastpost = None

g.badmarkers = [0]
#g.badmarkers = [5]

g.goodmarkers = [25, 10, 43]
g.goodmarkers = None

g.battery = 0.0
g.ultra = 0.0
g.can_ultra = 0.0

g.mqttc = None

g.ledstate = 0
g.signalling = False
g.warningblinking = None
g.ledcmd = None
g.speakcount = 1

# error here: we normalize the argument, but not the other value

def drive(sp):
    if True:
        if sp != 0 and not g.braking:
            g.speedsign = sign(sp)

        g.outspeedcm = sp*2
        print("outspeedcm = %d" % g.outspeedcm)
    else:

        # do this in readspeed2 instead
        # maybe, but then steer will zero the speed
        g.outspeed = sp

        if abs(sp) >= 7:
            g.speedtime = time.time()
        else:
            g.speedtime = None

        if sp != 0 and not g.braking:
            g.speedsign = sign(sp)

        if sp < 0:
            sp += 256
        st = g.steering
        if st < 0:
            st += 256
        tolog("motor %d steer %d" % (sp, g.steering))
        cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
            sp, st)
        #print (sp, g.steering, cmd)
        os.system(cmd)

def steer(st):
    g.steering = st
    sp = g.outspeed
#    if st < 0:
#        st += 256
#    if sp < 0:
#        sp += 256

#    tolog("motor %d steer %d" % (g.outspeed, st))
#    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
#        sp, st)
    #print (g.outspeed, st, cmd)
    dodrive(sp, st)
#    os.system(cmd)
#    tolog("motor2 %d steer %d" % (g.outspeed, st))

def stop(txt = ""):
    g.steering = 0
    g.outspeed = 0.0

    g.speedtime = None

    tolog("(%s) motor %d steer %d" % (txt, g.outspeed, g.steering))
    dodrive(0, 0)

def connect_to_ecm():
    stopped = False

    s = nav_comm.open_socket2()

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
                s112 = nav_comm.open_socket3()
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

g.heartn = -1
g.heartn_r = -1

def heartbeat():
    while True:
        g.heartn += 1
        send_to_ground_control("heart %.3f %d" % (time.time()-g.t0, g.heartn))

        if g.heartn-g.heartn_r > 1:
            print("waiting for heart echo %d %d" % (g.heartn, g.heartn_r))

        if g.heartn-g.heartn_r > 3:
            if g.limitspeed0 == "notset":
                print("setting speed to 0 during network pause")
                g.limitspeed0 = g.limitspeed
                g.limitspeed = 0.0

        if g.heartn-g.heartn_r < 2:
            if g.limitspeed0 != "notset":
                print("restoring speed limit to %s after network pause" % (str(g.limitspeed0)))
                g.limitspeed = g.limitspeed0
                g.limitspeed0 = "notset"

        time.sleep(5)

g.canSocket = None

def initializeCAN(network):
    """
    Initializes the CAN network, and returns the resulting socket.
    """
    # create a raw socket and bind it to the given CAN interface
    s = socket.socket(socket.AF_CAN, socket.SOCK_RAW, socket.CAN_RAW)
    s.bind((network,))
    return s

def readvin():
    f = open("/home/pi/can-utils/java/settings.properties")
    for line0 in f:
        line = line0[:-1]
        m = re.match("VIN=(.*)", line)
        if m:
            return m.group(1)
    return None

def init():
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

    canNetwork = "can0"
    canFrameID = 1025
    g.canSocket = initializeCAN(canNetwork)

    g.angleknown = False

    eight.eightinit()

    setleds(0, 7)

    start_new_thread(nav_tc.connect_to_ground_control, ())

    g.logf = open("navlog", "w")
    g.accf = open("acclog", "w")
    #g.accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
    #x, y, g.vx, g.vy, g.px, g.py, x0, y0, vvx, vvy, g.ppx, g.ppy, g.ang))
    g.accf.write("x y vx vy px py x0 y0 vvx vvy ppx ppy ang angvel steering speed inspeed outspeed odometer z0 r rx ry acc finspeed fodometer t can_ultra\n")

    g.t0 = time.time()
    print("t0 = %f" % g.t0)

    tolog("init")

    nav_imu.calibrate_imu()

    start_new_thread(wm.readmarker, ())
    start_new_thread(nav_mqtt.handle_mqtt, ())
    start_new_thread(wm.readspeed2, ())
    start_new_thread(nav_imu.readgyro, ())
    start_new_thread(senddrive, ())
    start_new_thread(keepspeed, ())
    start_new_thread(heartbeat, ())
    start_new_thread(connect_to_ecm, ())

def dodrive(sp, st):
    #print("dodrive %d %d" % (sp, st))
    g.send_sp = sp
    g.send_st = st

g.senddriveinhibited = False

def inhibitdodrive():
    g.senddriveinhibited = True


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
            if not g.senddriveinhibited:
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


def keepspeed():
    outspeedi = 0

    # 0 to 9
    speeds = [0, 7, 11, 15, 19, 23, 27, 37, 41, 45, 49,
              # 93 to 100 haven't been run yet
              53, 57, 73, 77, 81, 85, 89, 93, 97, 100]

    while True:
        time.sleep(0.1)

        if g.outspeedcm == None:
            continue

        spi = outspeedi

        desiredspeed = g.outspeedcm

        if g.limitspeed != None and desiredspeed > g.limitspeed:
            desiredspeed = g.limitspeed

        if g.user_pause:
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
                    g.outspeedcm, g.finspeed, outspeedi, spi, sp, g.outspeed))

        if g.outspeed == sp and sp != 0:
#            pass
            continue

        g.outspeed = sp

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
        st = g.steering
#        if st < 0:
#            st += 256
        tolog("motor %d steer %d" % (sp, st))
        dodrive(sp, st)
        time.sleep(sleeptime)

def getdist(x2, y2):
    # NEW
    x1 = g.ppx
    y1 = g.ppy

    d = dist(x1, y1, x2, y2)
    tolog("we are at (%f, %f), distance to (%f, %f) is %f" % (
            x1, y1, x2, y2, d))

    return d

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
    pos = eight.findpos(g.ppx,g.ppy,g.ang)
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

    #TARGETDIST = 0.3
    TARGETDIST = 0.15
    TARGETDIST = 0.25

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
            st_d = st - g.steering
            if st_d > 10:
                st = g.steering + 10
            elif st_d < -10:
                st = g.steering - 10

        steer(st)

        tolog("gotoa4 steer %f" % (st))

        send_to_ground_control("dpos %f %f %f %f 0 %f" % (g.ppx,g.ppy,g.ang,time.time()-g.t0, g.finspeed))

        time.sleep(0.1)

#============================================================
# User commands

g.user_pause = False

def pause():
    g.user_pause = True

def cont():
    g.user_pause = False


def goto(x, y, state):
    start_new_thread(gotoaux, (x, y, state))

def gotoaux(x, y, state):
    print("gotoaux %f %f %s" % (x, y, state))
    drive(0)
    if state == "accident":
        g.signalling = True
        start_new_thread(signal, ())

    time.sleep(4)
    drive(30)
    goto_1(x, y)
    g.signalling = False
    drive(0)

def whole4(dir):
    start_new_thread(whole4aux, (dir,))


def rev(l0):
    l = l0[:]
    l.reverse()
    return l


g.randdict = dict()

def randsel(a, b):
    astr = str(a)
    bstr = str(b)
    if astr not in g.randdict:
        g.randdict[astr] = 0
    if bstr not in g.randdict:
        g.randdict[bstr] = 0

    an = g.randdict[astr]
    bn = g.randdict[bstr]

    k = int(random.random()*(an+bn+2))
    if k <= bn:
        g.randdict[astr] = g.randdict[astr] + 1
        return a
    else:
        g.randdict[bstr] = g.randdict[bstr] + 1
        return b

def whole4aux(dir):

    # 'dir' is obsolete - it should always be -1

    speed0 = 20

    drive(0)
    time.sleep(4)
    drive(speed0)

    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    #path0 = rev(eight.piece3b) + [23]
    path0 = rev(eight.piece5) + rev(eight.piece1)

    print("path0 = %s" % str(path0))

    # idea: from the current position, determine which piece we can
    # start with

    path = eight.piece2path(path0, dir, 0.25)
    lpath = eight.piece2path(path0, dir, 0.15)
    rpath = eight.piece2path(path0, dir, 0.35)

    lx = None
    ly = None
    rx = None
    ry = None

    if dir == 1:
        path.reverse()

    i1 = -1

    while True:
        i10 = path[-1][2]
        # from eight.py:
        if eight.interleave == 2:
            i2 = path[-3][2]
        else:
            i2 = path[-2][2]

        if (i10, i2) == (23, 26):
            nextpiece = randsel(eight.piece2b, rev(eight.piece3a))
            #nextpiece = rev(eight.piece3a)
        elif (i10, i2) == (6, 13):
            nextpiece = eight.piece1
        elif (i10, i2) == (36, 30):
            nextpiece = randsel(eight.piece2a + [23], eight.piece5 + eight.piece4)
            #nextpiece = eight.piece5 + eight.piece4
        elif (i10, i2) == (23, 27):
            nextpiece = randsel(rev(eight.piece3a), eight.piece2b)
            #nextpiece = eight.piece2b
        elif (i10, i2) == (5, 10):
            nextpiece = rev(eight.piece4)
        elif (i10, i2) == (33, 31):
            nextpiece = randsel(rev(eight.piece3b) + [23],
                                rev(eight.piece5) + rev(eight.piece1))
            #nextpiece = rev(eight.piece5) + rev(eight.piece1)
        elif (i10, i2) == (23, 19):
            nextpiece = randsel(rev(eight.piece2a), eight.piece3b)
            #nextpiece = eight.piece3b
        elif (i10, i2) == (23, 16):
            nextpiece = randsel(rev(eight.piece2a), eight.piece3b)
            #nextpiece = rev(eight.piece2a)
        elif (i10, i2) == (4, 12):
            #nextpiece = randsel(eight.piece6 + eight.piece1, eight.piece3a + [23])
            nextpiece = eight.piece3a + [23]
        elif (i10, i2) == (7, 11):
            nextpiece = randsel(rev(eight.piece6) + rev(eight.piece4),
                                rev(eight.piece2b) + [23])
            #nextpiece = rev(eight.piece2b) + [23]
        elif (i10, i2) == (35, 32):
            nextpiece = rev(eight.piece1)
        elif (i10, i2) == (34, 29):
            nextpiece = eight.piece4
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

        path = eight.piece2path(nextpiece, dir, 0.25)
        lpath = eight.piece2path(nextpiece, dir, 0.15)
        rpath = eight.piece2path(nextpiece, dir, 0.35)

def initpos():
    g.markercnt = 0
    g.angleknown = False
    g.crash = False
    g.crashacc = None
    g.remote_control = False

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

