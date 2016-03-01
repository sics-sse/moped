import smbus
import time
import thread
import os
import subprocess
import math

marker = None
inspeed = 0.0
odometer = 0
age = -1

TARGETDIST = 0.3
DEFAULTSPEED = 7
TURNSPEED = 20
TOOHIGHSPEED = 2.0

MINRADIUS = 0.83

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

#gscale = 32768.0/250
gscale = 32768.0/1000
ascale = 1670.0

angdiff = 0.0
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
    global ang, dang
    global gyron
    global t0
    global px, py, pz
    global ppx, ppy, ppz
    global vx, vy, vz
    global angdiff

    try:

        t1 = time.time()

        while True:
            gyron += 1

            high = bus.read_byte_data(address, 0x47)
            low = bus.read_byte_data(address, 0x48)
            r = make_word(high, low)

            r -= rbias

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

            # the signs here assume that x goes to the right and y forward

            x = x0*math.cos(math.pi/180*ang) - y0*math.sin(math.pi/180*ang)
            y = x0*math.sin(math.pi/180*ang) + y0*math.cos(math.pi/180*ang)

            vx += x*dt
            vy += y*dt
            vz += z*dt

            px += vx*dt
            py += vy*dt
            pz += vz*dt

            vvx = inspeed/100.0*math.sin(math.pi/180*ang)
            vvy = inspeed/100.0*math.cos(math.pi/180*ang)

            ppx += vvx*dt
            ppy += vvy*dt

            # don't put too many things in this thread

            accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
                    x, y, vx, vy, px, py, x0, y0, vvx, vvy, ppx, ppy, ang))

            
            j = angdiff/1000
            ang += j
            angdiff -= j

    except Exception as e:
        tolog("exception in readgyro: " + str(e))


def tolog2(str0, stdout):
    str = "(speed %d time %.3f) %s" % (inspeed, time.time() - t0, str0)
    logf.write(str + "\n")
    if stdout:
        print str

def tolog0(str):
    tolog2(str, False)

def tolog(str):
    tolog2(str, True)

global markermsg
markermsg = None

lastmarker0 = None

lastpos = None
lastpost = None

#badmarkers = [7,55,25,43]

def readmarker():
    global marker, age, lastmarker0, markermsg
    global px, py
    global ppx, ppy
    global ang
    global lastpos

    while True:
        p = subprocess.Popen("tail -1 /tmp/marker0", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()
        m = res[0]
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
            if abs(odiff) > 45.0 and markerno != -1:
                tolog("wrong marker %d %f" % (markerno, odiff))
                markerno = -1
            if (markerno > -1 and quality > 0.25):
                close = True
                if lastpos != None:
                    (xl, yl) = lastpos
                    dst = dist(x, y, xl, yl)
                    tolog0("local speed %f" % (dst/(t-lastpost)))
                    if dst/(t-lastpost) > TOOHIGHSPEED:
                        close = False

                if not close:
                    msg = "bad marker %d not close" % markerno
                    if msg != markermsg:
                        tolog(msg)
                        markermsg = msg
                    age += 1
                else:
                    tolog0("marker1 %s %d %f %f" % (m, age, ang, ori))
                    send_to_ground_control("pos %f %f" % (x,y))
                    lastpos = (x,y)
                    px = x
                    py = y
                    ppx = x
                    ppy = y
                    vx = math.sin(ang*math.pi/180)*inspeed/100
                    vy = math.cos(ang*math.pi/180)*inspeed/100
                    lastpost = t
                    marker = m
                    age = 0
                    #ang = ori
            else:
                age += 1
            tolog0("marker2 %d %f %f %d %f %d %f" % (-1, px, py, int(ang), 0.5, age, ang))
            tolog0("marker3 %d %f %f %d %f %d %f" % (-1, ppx, ppy, int(ang), 0.5, age, ang))

def readspeed():
    global inspeed, odometer

    while True:
        p = subprocess.Popen("tail -1 /tmp/speed", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()
        m = res[0]
        line = m.split('\n')[0]
        l = line.split(" ")
        inspeed = float(l[0])
        odometer = int(l[1])

def report():
    while True:
        time.sleep(2)
        print marker + " " + str(inspeed)

outspeed = 0.0
steering = 0.0

# error here: we normalize the argument, but not the other value

def drive(sp):
    global outspeed

    outspeed = sp
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
    if st < 0:
        st += 256
    if sp < 0:
        sp += 256
    tolog("motor %d steer %d" % (outspeed, st))
    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
        sp, st)
    #print (outspeed, st, cmd)
    os.system(cmd)

def stop(txt = ""):
    global steering, outspeed

    steering = 0.0
    outspeed = 0.0
    tolog("(%s) motor %d steer %d" % (txt, outspeed, steering))
    os.system("/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
            outspeed, steering))

def init():
    global logf, goodmarkers, markermsg, rbias, xbias, ybias, zbias, t0
    global px, py, pz
    global ppx, ppy, ppz
    global vx, vy, vz
    global accf

    logf = open("navlog", "w")
    accf = open("acclog", "w")
    #accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
    #x, y, vx, vy, px, py, x0, y0, vvx, vvy, ppx, ppy, ang))
    accf.write("x y vx vy px py x0 y0 vvx vvy ppx ppy ang\n")

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

    print "rbias = %f, xbias = %f, ybias = %f, zbias = %f" % (rbias, xbias, ybias, zbias)


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

    thread.start_new_thread(readmarker, ())
    thread.start_new_thread(readspeed, ())
    thread.start_new_thread(readgyro, ())
    #thread.start_new_thread(report, ())

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

def path(x1, y1, ang1, x2, y2):
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
    if dd > 2*MINRADIUS:
        return
    if dd > MINRADIUS and abs(steering) < 90:
        return
    a = ang % 360
    if a > 180:
        a -= 360
    d = MINRADIUS*(1-sign(steering)*math.sin(math.pi/180*a))
    if dd < d:
        tolog("will hit the wall %f %f %f %f" % (dd, d, steering, ang))

def getdist(x2, y2):
    m = marker.split(" ")
    x1 = float(m[1])
    y1 = float(m[2])
    d = dist(x1, y1, x2, y2)
    tolog("we are at (%f, %f), distance to (%f, %f) is %f" % (
            x1, y1, x2, y2, d))

    checkwall()
    return d

# Add to marker: how long ago we got an acceptable reading

# Give commands to follow the path. Current position and speed and
# orientation are relevant.
# Currently, we ignore speed

def makepath(x2, y2):
    global ang, angdiff
    m = marker.split(" ")
    x1 = float(m[1])
    y1 = float(m[2])
    ori = float(m[3])
    #print ('angles', ang, ori)
    ori1 = ang
    a = math.atan2(y2-y1, x2-x1)
    adeg = 180/math.pi*a
    adeg = 90-adeg
    adiff = (ori1-adeg+360)%360
    angdiff = (ori-ang)%360
    if angdiff > 180:
        angdiff -= 360
    tolog0("angles %f %f %f %f %f %d" % (
            ori, ang, adeg, adiff, angdiff, int(m[0])))

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
    x = x1 + MINRADIUS*math.sin(a)
    y = y1 + MINRADIUS*math.cos(a)
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
        d = getdist(x2, y2)
        if d < TARGETDIST:
            break
        adiff = makepath(x2, y2)
        if adiff > 180:
            adiff -= 360
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
            if (not inarea(cx+MINRADIUS, cy+MINRADIUS) or
                not inarea(cx-MINRADIUS, cy+MINRADIUS) or
                not inarea(cx+MINRADIUS, cy-MINRADIUS) or
                not inarea(cx-MINRADIUS, cy-MINRADIUS)):
                print "can't turn!"
                stop("can't turn")

                return False

        p = 4.0
        #steer(turn*maxst)
        str = turn*min(p*abs(adiff), 90)
        tolog("steering %d adiff %d" % (str, adiff))
        steer(str)
        #print adiff
        # wrong: adiff will be for example 359, not -1
        # so correct it
        if abs(adiff) < limit:
            if not nostop:
                stop("1")
            break

        if inspeed == 0.0:
            tolog("speed is 0 (while turning), obstacle?")
            stop("2")
            return False

    tolog("turned (%d steps) %f %f, adiff = %f, dist = %f" % (
            i, x2, y2, adiff, d))
    return True

def gotoc(x2, y2, speed = DEFAULTSPEED):
    return goto0(x2, y2, True, speed)

def goto(x2, y2, speed = DEFAULTSPEED):
    return goto0(x2, y2, False, speed)

def goto0(x2, y2, nostop, speed):
    if not inarea(x2, y2):
        print "goal point (%f,%f) is outside area" % (x2, y2)
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


import socket
import sys

HOST = 'localhost'    # The remote host
PORT = 50008              # The same port as used by the server

s = None
for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
    af, socktype, proto, canonname, sa = res
    try:
        s = socket.socket(af, socktype, proto)
    except socket.error, msg:
        s = None
        continue
    try:
        s.connect(sa)
    except socket.error, msg:
        s.close()
        s = None
        continue
    break
if s is None:
    print 'could not open socket'
    sys.exit(1)

ground_control = s

def send_to_ground_control(str):
    s.send(str)
