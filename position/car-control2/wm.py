import time
import re
import subprocess

from nav_tc import send_to_ground_control

from nav_log import tolog, tolog0

from nav_util import dist

import nav_signal
import driving

mp = dict()

mp[1] = (0.255, 0.376)
mp[55] = (0.295, 2.241)
mp[4] = (0.348, 4.745)
mp[35] = (0.307, 6.566)
mp[9] = (0.338, 8.958)
mp[39] = (0.329, 11.150)
mp[17] = (0.370, 13.155)
mp[43] = (0.343, 15.386)
mp[22] = (0.277, 17.566)
mp[5] = (0.306, 19.376)
mp[14] = (2.215, 1.043)
mp[3] = (2.017, 3.375)
mp[29] = (2.176, 5.400)
mp[6] = (1.974, 7.721)
mp[19] = (2.205, 9.766)
mp[10] = (2.266, 12.063)
mp[7] = (2.179, 14.351)
mp[25] = (2.255, 16.770)
mp[4] = (2.225, 18.725)

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

            if ((g.markerno > -1 and quality > g.minquality
                 and g.markerno not in g.badmarkers
                 and (g.goodmarkers == None or g.markerno in g.goodmarkers)
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

                if g.markerno in mp:
                    mdist = dist(x, y, mp[g.markerno][0], mp[g.markerno][1])
                    if mdist > 1.0:
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
                    tolog0("POS: position then: %f %f" % (thenx, theny))
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
                    #nav_mqtt.send_to_mqtt(x, y)
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

                                    ppxdiff1 /= 2
                                    ppydiff1 /= 2
                                    #angdiff1 /= 2
                                    if True:
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
                    g.lastpost = it0
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
    part = b""
    part2 = b""
    while True:
        data = g.canSocket.recv(1024)
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
                        nav_signal.speak("obstacle")
                        send_to_ground_control("obstacle")
                        print("obstacle")
                        driving.drive(0)

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
                if len(part2) > 18:
                    part2x = part2[19:]
                    part2s = part2x.decode('ascii')
                    l = part2[18]
                    part2s2 = part2s[0:l]

                    m = re.search("([0-9]+) ([0-9]+)", part2s2)
                    if m:
                        cnt = int(m.group(1))
                        d = int(m.group(2))

                        #print((cnt,d))
                        g.can_ultra = d/100.0
                        # not used:
                        can_ultra_count = cnt
                    part2 = b""
            part2 += data[9:]

        time.sleep(0.00001)            

def wminit():
    g.outspeed = 0.0
    g.outspeedcm = None
    g.steering = 0

