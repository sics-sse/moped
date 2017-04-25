import time
import re
import subprocess
import random

from math import sqrt, sin, cos, pi

from nav_tc import send_to_ground_control

from nav_log import tolog, tolog0

from nav_util import dist

import nav_signal
import driving
import nav_mqtt

mp = dict()

mp[1] = (0.255, 0.376)
mp[55] = (0.295, 2.241)
mp[4] = (0.348, 4.745)
mp[35] = (0.307, 6.566)
mp[2] = (0.32, 14.32)
mp[9] = (0.338, 8.958)
mp[39] = (0.329, 11.150)
mp[17] = (0.370, 13.155)
mp[43] = (0.343, 15.386)
mp[22] = (0.277, 17.566)
mp[5] = (1.08, 19.12)
mp[14] = (2.215, 1.043)
mp[3] = (2.017, 3.375)
mp[29] = (2.176, 5.400)
mp[6] = (1.974, 7.721)
mp[19] = (2.205, 9.766)
mp[10] = (2.266, 12.063)
mp[7] = (2.179, 14.351)
mp[25] = (2.255, 16.770)
mp[4] = (2.225, 18.725)

def pbool(p):
#    print("1")
    return p

def readmarker():
    while True:
        tolog("starting readmarker")
        try:
            readmarker0()
        except Exception as e:
            tolog("readmarker exception %s" % str(e))
            print("readmarker exception %s" % str(e))

def readmarker0():
    TOOHIGHSPEED = 2.0

    recentmarkers = []

    markertime = None
    goodmarkertime = None
    markerage = None

    while True:
        p = subprocess.Popen("tail -1 /tmp/marker0", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()
        m = res[0].decode('ascii')
        m = m.split('\n')[0]
        if m == g.lastmarker0:
            tolog0("no new marker0")
            continue

        g.lastmarker0 = m

        if g.ignoremarkers:
            continue

        m1 = m.split(" ")
        if len(m1) != 7:
            print("bad marker line")
            continue
        if m1 == "":
            pass
        else:
            t = time.time()

            doadjust = False

            #nav_signal.blinkleds()

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

#            if markertime == None or t-markertime > 5:
            if markertime == None:
                #markertime = t
                skipmarker = False
            else:
                skipmarker = True

            x += g.shiftx

            minq = g.minquality

            # g.ang here below should be thenang, I think
            badmarker = False
            for (badm, bada) in g.badmarkers:
                if g.markerno == badm:
                    if bada == 'all':
                        bada = g.ang
                    angm = (g.ang - bada)%360
                    if angm > 180:
                        angm -= 360
                    if abs(angm) < 25:
                        badmarker = True
                        break

            if g.goodmarkers != None:
                goodmarker = False
                for (goodm, gooda, goodq) in g.goodmarkers:
                    if g.markerno == goodm:
                        if gooda == 'all':
                            gooda = g.ang
                        angm = (g.ang - gooda)%360
                        minq = goodq
                        if angm > 180:
                            angm -= 360
                        if abs(angm) < 25:
                            goodmarker = True
                            break
            else:
                goodmarker = True

            if (g.markerno > -1
                and goodmarker
                and ((x > -0.3 and x < 3.3 and y > 0 and y < 19.7)
                     or (x > 3.0 and x < 30 and y > 2.3 and y < 5.5))):
                    tolog0("marker0 %s %f" % (m, quality))

            # complain somewhere if good and bad overlap

            if ((pbool(g.markerno > -1) and pbool(quality > minq)
                 #and abs(g.steering) < 30
                 #and (x < 1.0 or x > 2.0)
                 and pbool(goodmarkertime == None or
                      t-goodmarkertime > g.markertimesep)
                 and pbool(not badmarker)
                 and pbool(goodmarker)
                 and ((x > -0.3 and x < 3.3 and y > 0 and y < 19.7)
                      or (x > 3.0 and x < 30 and y > 2.3 and y < 5.5)))
                and not skipmarker):

                close = True
                if not g.angleknown:
                    g.ang = ori
                    g.ppx = x
                    g.ppy = y
                    g.oldpos = dict()
                g.angleknown = True

                mdist = -1
                if g.markerno in mp:
                    mdist = dist(x, y, mp[g.markerno][0], mp[g.markerno][1])
                    if mdist > g.maxmarkerdist:
                        #print("dist = %f" % mdist)
                        continue

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
                    tolog0("POS: position then: %f %f %f" % (thenx, theny,
                                                             thenang))
                elif g.oldpos != None:
                    tolog0("POS: can't use oldpos")                    
                    continue

                if True:
                    if g.lastpos != None:
                        (xl, yl) = g.lastpos
                        dst = dist(thenx, theny, xl, yl)
                        tolog0("local speed %f" % (dst/(t-g.lastpost)))
                        if dst/(t-g.lastpost) > TOOHIGHSPEED:
                            close = False

                dst = dist(g.ppx, g.ppy, x, y)
                # even if somewhat correct: it causes us to lose
                # position when we reverse
                if dst > 2.0 and g.markercnt > 10:
                    close = False
                tolog0("marker dist %f" % dst)

                if not close:
                    msg = "bad marker %d not close" % g.markerno
                    if msg != g.markermsg:
                        tolog(msg)
                        g.markermsg = msg
                else:
                    accepted = True

                    goodmarkertime = t

                    g.markercnt += 1
                    tolog0("marker1 %s %f %f %f %f %f %f" % (m, g.ang, ori, thenx, theny, quality, mdist))
                    if doadjust:
                        doadjust_n = 1
                    else:
                        doadjust_n = 0
                    send_to_ground_control("mpos %f %f %f %f %d %f" % (x,y,g.ang,time.time()-g.t0, doadjust_n, g.inspeed))
                    nav_mqtt.send_to_mqtt(x, y, ori)
                    g.lastpos = (thenx,theny)
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

# Unclear to me whether we should halve or not: after a long time, we should
# treat the marker as the truth, but when markers come soon after one
# another, one is not more likely than the other to be right, so we go to the
# middle point.
                                    #ppxdiff1 /= 2
                                    #ppydiff1 /= 2
                                    #angdiff1 /= 2
                                    if True:
                                        g.ppxdiff = ppxdiff1
                                        g.ppydiff = ppydiff1
                                    #print("3 ppydiff := %f" % g.ppydiff)
                                    g.angdiff = angdiff1

                                    adjdist = sqrt(
                                        ppxdiff1*ppxdiff1 +
                                        ppydiff1*ppydiff1)
                                    if g.maxadjdist < adjdist:
                                        g.maxadjdist = adjdist
#                                        print("new maxadjdist %f"
#                                              % g.maxadjdist)
                                    if g.adjdistlimit != None and adjdist > g.adjdistlimit:
                                        g.poserror = True
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
                    g.lastpost = it0
                    #g.ang = ori
            else:
                if g.adjust_t != None:
                    markerage = time.time() - g.adjust_t
                    if markerage > 10:
                        tolog("marker age %f" % markerage)

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
                tolog0("marker5 %s %f %f" % (m, g.ang, ori))
        time.sleep(0.00001)


# The code here is a bit ad-hoc. We need to find out why the various
# constants and offsets appear.
def readspeed2():
    part = b""
    part2 = b""
    while True:
        try:
            # 64 was 1024
            data = g.canSocket.recv(64)
            if (data[0], data[1]) == (100,4) and data[4] == 2:
                # length of packet is 2
                print((data[8], data[9]))
                g.rc_button = True
                time.sleep(0.00001)
            elif (data[0], data[1]) == (100,4):
                if data[8] == 16:
                    parts = str(part)

                    m = re.search("speed x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)x([0-9 ]+)", parts)
                    if m:
                        #print((time.time(),parts))
                        oinspeed = g.inspeed
                        g.inspeed = g.speedsign * int(m.group(1))

                        alpha = 0.8
                        g.inspeed_avg = (1-alpha)*g.inspeed + alpha*oinspeed

                        if (g.inspeed == 0 and g.speedtime != None and
                            time.time() - g.speedtime > 7.0):
                            nav_signal.speak("obstacle")
                            send_to_ground_control("obstacle")
                            g.obstacle = True

                        g.odometer = int(m.group(2))
                        if g.odometer != g.lastodometer:
                            send_to_ground_control("odometer %d" % (g.odometer))
                            g.lastodometer = g.odometer
                        #print("rsp-odo %d %d" % (g.inspeed, g.odometer))

                        g.finspeed = int(m.group(3))
                        g.finspeed *= g.speedsign

                        g.fodometer = int(m.group(4))
                        #print("fsp-odo %d %d" % (g.finspeed, g.fodometer))
                        g.leftspeed = int(m.group(5))
                        g.fleftspeed = int(m.group(6))

                    part = b""
                    time.sleep(0.00001)
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
                time.sleep(0.00001)            
            elif (data[0], data[1]) == (108,4):
                # Reading DistPub this way is not a good idea, since those
                # messages come often and slow down the other threads (or the
                # whole process?).
                # DistPub
                # note that non-ASCII will appear as text \x07 in 'parts'
                if data[8] == 16:
                    if len(part2) > 18:
                        part2x = part2[19:]
                        part2s = part2x.decode('ascii')
                        l = part2[18]
                        part2s2 = part2s[0:l]

                        m = re.search("^([0-9]+) ([0-9]+) $", part2s2)
                        if m:
                            #print((part2s2, len(part2), part2))
                            #print(part2s2)
                            cnt = int(m.group(1))
                            d = int(m.group(2))

                            #print((cnt,d))
                            g.can_ultra = d/100.0
                            # not used:
                            can_ultra_count = cnt
                        part2 = b""
                        time.sleep(0.00001)            
                part2 += data[9:]
        except Exception as a:
            print(a)

def wminit():
    g.outspeed = 0.0
    g.outspeedcm = 0.0
    g.steering = 0

    g.markerno = 0
    g.markercnt = 0
    g.angleknown = False
    g.lastodometer = None

    g.markermsg = None
    g.lastmarker0 = None
    g.lastpos = None
    g.lastpost = None

def putcar(x, y, ang):
    g.angleknown = True
    g.ppx = x
    g.ppy = y
    g.ang = ang

def simulatecar():
    dt = 0.1

    # acc 0.0 in fact represents infinity
    g.simulmaxacc = 0.6
    acc = 0.0

    while True:

        desiredspeed = g.outspeedcm
        if g.limitspeed != None and desiredspeed > g.limitspeed:
            desiredspeed = g.limitspeed

        if abs(g.finspeed - desiredspeed) < 5 or g.simulmaxacc == 0.0:
            acc = 0.0
            g.finspeed = desiredspeed
        elif g.finspeed > desiredspeed:
            acc = -g.simulmaxacc
        elif g.finspeed < desiredspeed:
            acc = g.simulmaxacc
        else:
            acc = 0.0

        if acc != 0.0:
            ospeed = g.finspeed
            g.finspeed += 100*acc*dt
            if ospeed > 0 and g.finspeed < 0:
                g.finspeed = 0

        g.inspeed = g.finspeed

        g.dang = g.steering/100.0 * 1.5 * desiredspeed
        g.ang += g.dang*dt
        g.ppx += g.finspeed/100*dt*sin(g.ang*pi/180)
        g.ppy += g.finspeed/100*dt*cos(g.ang*pi/180)

        # make the steering a little flaky
        f = random.random()
        g.ang += (2*f-1) * 0

        time.sleep(dt*g.speedfactor)
        send_to_ground_control("dpos %f %f %f %f 0 %f" % (
                g.ppx, g.ppy, g.ang, 0, g.finspeed))
        nav_mqtt.send_to_mqtt(g.ppx, g.ppy, g.ang)
