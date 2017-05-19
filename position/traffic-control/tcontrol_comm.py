import thread
import socket
import time
import math
import json
import sys

from math import sin, cos, sqrt, pi, atan2

sys.path.append("car-control2")
import eight
import nav_map

from tcontrol_car import Car, cars

from tcontrol_globals import g

HOST = ''                 # Symbolic name meaning all available interfaces
PORT = 50009              # Arbitrary non-privileged port

g.d = dict()

global event_nr

event_nr = 0

def tolog(s0):
    s = "(%f) %s" % (time.time()-g.t0, s0)
    print(s)
    g.logf.write(s + "\n")

def tolog0(s0):
    s = "(%f) %s" % (time.time()-g.t0, s0)
    g.logf.write(s + "\n")

def update_carpos1(x, y, ang, c):
    global event_nr

    event_nr += 1
    g.d[event_nr] = ("pos", x, y, ang, c)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def set_markerpos(x, y, c, adj):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("mpos", x, y, c, adj)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def set_badmarkerpos(x, y, c):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("badmarker", x, y, c)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def update_mark(x, y):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("mark", x, y)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)



# put in a tcontrol_util instead
def dist(x1, y1, x2, y2):
    return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))



def linesplit(socket):
    buffer = socket.recv(4096)
    buffering = True
    while buffering:
        if "\n" in buffer:
            (line, buffer) = buffer.split("\n", 1)
            #print "yielding %s from %s" % (line, str(socket))
            yield line + "\n"
        else:
            more = socket.recv(4096)
            if not more:
                buffering = False
            else:
                buffer += more
    if buffer:
        yield buffer


safedist = 15.5

def check_other_cars1(c):
    l = []
    for ci in cars:
        c2 = cars[ci]
        if c2 == c:
            continue
        if c2.x == None:
            continue
        d = dist(c.x, c.y, c2.x, c2.y)
        if d > safedist:
            continue
        other = 180/pi*atan2(c2.y-c.y, c2.x-c.x)
        other = 90-other
        angdiff = other - c.ang%360
        angdiff = angdiff%360
        if angdiff > 180:
            angdiff -= 360
        #print (c2.y-c.y, c2.x-c.x)
        #print (c.ang%360, other)
        if False:
            print "%d (%.2f,%.2f): other car %d (%.2f,%.2f) dist %.2f dir %.2f" % (
                c.n, c.x, c.y,
                c2.n, c2.x, c2.y,
                d, angdiff)
        if angdiff > -45 and angdiff < 45:
            l = l + [(angdiff, d, c2.x, c2.y, c2.n)]

    fronts = "carsinfront %d" % len(l)
    for tup in l:
        fronts = fronts + " " + ("%f %f %f %f %d" % tup)
    c.conn.send(fronts + "\n")

def converging(c1, c2):
    n1 = c1.nextnode
    n2 = c2.nextnode
    l1 = c1.lastnode
    l2 = c2.lastnode
    if n1 != n2:
        return False
    if n1 == 5:
        return (l1 != 4 and l2 != 4)
    if n1 == 6:
        return (l1 != 7 and l2 != 7)
    if n1 == 35:
        return (l1 != 36 and l2 != 36)
    if n1 == 34:
        return (l1 != 33 and l2 != 33)
    return False

global doprint
doprint = None

def following(tup, tup2):
    (piece, odo) = tup
    (piece2, odo2) = tup2

    onlyif = 0

    if piece == piece2 and odo < odo2:
        tolog0("clause f1")
        #print("following %s %f %f" % (str(piece), odo, odo2))
        return (True, odo2-odo, onlyif)

    (a, b) = piece
    (a2, b2) = piece2

    onlyif = b2

    if b == a2 and b2 != a:
        # (34, 23) pretends to follow (23, 35) here; that's wrong but maybe
        # harmless
        (_, piecelen) = eight.pieces[piece]
        tolog0("clause f2")
        return (True, odo2+piecelen - odo, onlyif)

    # subsumes the first clause above: even if we are going different
    # ways from the same point, assume we can crash
    # if odo2 is sufficiently large, that need not be the case
    if a == a2 and odo < odo2:
        tolog0("clause f3")
        return (True, odo2-odo, 0)

    if False:
        # something is wrong here
        (_, piecelen) = eight.pieces[piece]
        (_, piecelen2) = eight.pieces[piece2]
        if b == b2 and a != a2 and piecelen-odo < piecelen2-odo2:
            return (True, (piecelen2-odo2)-(piecelen-odo), 0)

    return False

def giveway(po, po2):
    (piece, odo) = po
    (piece2, odo2) = po2
    (a, b) = piece
    (a2, b2) = piece2

    st = False
    onlyif = 0

    if piece == (6, 23) and piece2 == (34, 35):
        tolog0("clause g1")
        # not always true
        st = True
    if piece == (34, 23) and piece2 == (6, 5):
        tolog0("clause g2")
        # not always true
        st = True

    if piece == (34, 23) and piece2 == (35, 6):
        tolog0("clause g2a")
        # not always true
        st = True
    if piece == (6, 23) and piece2 == (5, 34):
        tolog0("clause g2b")
        # not always true
        st = True

    if piece == (23, 35) and piece2 == (5, 34):
        tolog0("clause g2.1a")
        # not always true
        st = True
    if piece == (23, 5) and piece2 == (35, 6):
        tolog0("clause g2.1b")
        # not always true
        st = True

    if piece == (6, 23) and piece2 == (23, 5) and odo2 < 1.2:
        tolog0("clause g12")
        st = True
    if piece == (34, 23) and piece2 == (23, 35) and odo2 < 1.2:
        tolog0("clause g12bis")
        st = True

    if not st:

        if b != b2:
            return False

        pp = (a, a2, b)
        if pp == (23, 34, 35):
            tolog0("clause g3")
            st = True
        elif pp == (5, 23, 6):
            tolog0("clause g4")
            st = True
        elif pp == (23, 6, 5):
            tolog0("clause g4bis")
            st = True
        elif pp == (35, 23, 34):
            tolog0("clause g5")
            st = True

        # the four next ones only occur when we go in both directions
        # in our eight.

        # sometimes, we don't need to give way if we know where we are going
        elif pp == (6, 5, 23):
            tolog0("clause g6")
            # here, we need to know where the other one is going, too
            st = True
        elif pp == (34, 35, 23):
            tolog0("clause g7")
            # here, we need to know where the other one is going, too
            st = True
        elif pp == (5, 34, 23):
            tolog0("clause g8")
            st = True
        elif pp == (35, 6, 23):
            tolog0("clause g9")
            onlyif = 6
            st = True

        # don't meet in the central portion when there is only one lane:
        elif pp == (34, 6, 23) and odo <= 0.5:
            tolog0("clause g10")
            st = True
        elif pp == (6, 34, 23) and odo2 > 0.5:
            tolog0("clause g11")
            st = True

    if st:
        #print("giveway %s %s" % ((str(piece), str(piece2))))
        return (st, onlyif)
    return False

def sign(x):
    if x < 0:
        return -1
    if x > 0:
        return 1
    return 0

# Return the cross product of the vectors p1-p2 and p1-p3
def cross(p1, p2, p3):
    (x1, y1) = p1
    (x2, y2) = p2
    (x3, y3) = p3
    dxa = x1-x2
    dya = y1-y2
    dxb = x1-x3
    dyb = y1-y3
    return dxa*dyb-dya*dxb

def check_other_cars(c):

    global doprint
    l = []

    if c.lastnode != -1 and c.nextnode != -1:
        knownnodes = [(c.lastnode, c.nextnode)]
        knownnodesalt = c.betweenlist
    else:
        knownnodes = None
        knownnodesalt = None
    tolog0("%s x y ang %f %f %f known %s" % (c.info, c.x, c.y, c.ang%360, str(knownnodes)))
    pos = nav_map.findpos(c.x, c.y, c.ang, knownnodes)
    posalt = nav_map.findpos(c.x, c.y, c.ang, knownnodesalt)
    if pos == None:
        fronts = "carsinfront 0"
        c.conn.send(fronts + "\n")
        return
    #tolog0("%s pos %s %s" % (c.info, str(pos), str(knownnodes)))
    tolog0("%s posalt %s %s" % (c.info, str(posalt), str(knownnodesalt)))
    pos = posalt
    (ac, bc, tup) = pos
    q = tup[6]
    if (ac, bc) != (tup[0], tup[1]):
        # we have reversed the direction
        q = 1-q

    (piece, odo) = nav_map.findpiece(ac, bc, q)

    for ci in cars:

        c2 = cars[ci]
        if c2 == c:
            continue

        if c2.lastnode != -1 and c2.nextnode != -1:
            knownnodes2 = [(c2.lastnode, c2.nextnode)]
            knownnodes2alt = c2.betweenlist
        else:
            knownnodes2 = None
            knownnodes2alt = None
        pos2 = nav_map.findpos(c2.x, c2.y, c2.ang, knownnodes2)
        pos2alt = nav_map.findpos(c2.x, c2.y, c2.ang, knownnodes2alt)
        pos2 = pos2alt
        if pos2 == None:
            continue
        #print("%s pos %s" % (c.info, str(pos)))
        (ac2, bc2, tup2) = pos2
        q2 = tup2[6]

        (piece2, odo2) = nav_map.findpiece(ac2, bc2, q2)
        (_, piecelen) = eight.pieces[piece]
        (_, piecelen2) = eight.pieces[piece2]
        tolog0(str((c.info, piecelen, piece, odo,
                    c2.info, piecelen2, piece2, odo2)))

        d = dist(c.x, c.y, c2.x, c2.y)
        if d < 0.35:
            if c.info < c2.info and False:
                print("%d car distance %f %s" % (
                        time.time(), d, str((c.info, piece, q, odo,
                                             c2.info, piece2, q2, odo2))))

        if d > safedist:
            continue

        ok = True

        if d < sqrt(0.5*0.5+0.3*0.3) and c.info < c2.info:
            cornerlist = []
            for caro in (c, c2):
                cara = caro.ang
                carx = caro.x
                cary = caro.y
                corners = []
                for (orix, oriy) in [(-1,-1), (-1,1), (1,1), (1,-1)]:
                    dx = orix*0.15
                    dy = oriy*0.25
                    corners.append((carx
                                    +cos(cara*pi/180)*dx
                                    +sin(cara*pi/180)*dy,
                                    cary
                                    -sin(cara*pi/180)*dx
                                    +cos(cara*pi/180)*dy))
                cornerlist.append(corners)

            #print(cornerlist)

            ok = False

            for (i0, i1) in [(0,1), (1,0)]:
                if ok:
                    break

                car1points = cornerlist[i0]
                car2points = cornerlist[i1]

                for i in range(0, 4):
                    sides = []
                    pi1 = i
                    pi2 = (i+1)%4
                    pi3 = (i+2)%4
                    pi4 = (i+3)%4
                    p1 = car1points[pi1]
                    p2 = car1points[pi2]
                    for car2p in car2points:
                        side = cross(car2p, p1, p2)
                        sides.append(side)
                    for car2p in [car1points[pi3], car1points[pi4]]:
                        side = cross(car2p, p1, p2)
                        sides.append(side)
                    #print("sides %d %d %s" % (i0, i, str(sides)))
                    signs = [sign(nu) for nu in sides]
                    if (signs == [1,1,1,1,-1,-1] or 
                        signs == [-1,-1,-1,-1,1,1]):
                        ok = True
                        break
            #tolog("overlap %s" % (str(not ok)))

        # for now: ignore the crash detection
        ok = True

        onlyif = 0

        foll = following((piece, odo), (piece2, odo2))
        if foll:
            (_, o, onlyif) = foll
            if o > safedist:
                continue

        give = giveway((piece, odo), (piece2, odo2))

        stopd = 0.3*c.finspeed/60
        tolog0("%s stopd %f" % (c.info, stopd))

        if c2.finspeed == 0.0:
            tolog0("c2 = %s speed 0 following = %s" % (
                    c2.info, str(c2.following)))
            if c2.following:
                # simple fix for some deadlocks, namely that c2 is behind
                # another car which in turn waits for us
                # try to restrict this to cases where c2 waits behind
                # another car
                give = False

        if give:
            (_, onlyif) = give

            dpieces = []
            dpieces2 = []
            if (piece == (6, 23) or
                piece == (35, 6) and piecelen-odo < 0.6 or
                piece == (23, 35) and odo < 0.4-stopd):
                if piece == (6, 23):
                    odoleft = piecelen - odo + 0.4-stopd
                elif piece == (35, 6):
                    odoleft = 0
                dpieces.append(1)
            if (piece == (34, 23) or
                piece == (5, 34) and piecelen-odo < 0.6 or
                piece == (23, 6) and odo < 0.4-stopd):
                dpieces.append(2)


            if piece == (34, 23) and piece2 == (6, 23):
                # the distance to the line where we must stop, minus
                # the distance between the front edge and the middle

                d = 0.5 - odo - stopd
                tolog0("clause0a d %f stopd %f" % (d, stopd))
                if d < stopd and False:
                    # we're too close to stop - continue and hope
                    # that the other car stops for us
                    give = False
            if piece == (6, 23) and piece2 == (34, 23):
                # the distance to the line where we must stop, minus
                # the distance between the front edge and the middle
                d = 0.5 - odo - stopd
                tolog0("clause0b d %f stopd %f" % (d, stopd))
                if d < stopd and False:
                    # we're too close to stop - continue and hope
                    # that the other car stops for us
                    give = False

            if (piece == (6, 23) and piece2 == (5, 34)
                or piece == (34, 23) and piece2 == (35, 6)):
                tolog0("clause1bis piecelen2 %f odo2 %f" % (piecelen2, odo2))
                # we should test: if the other car wants to turn 34-23, we
                # should not give way to it
                if piecelen2 - odo2 < 1.0:
                    d = piecelen - odo + 0.4 - 0.25 - stopd
                    if d < 0:
                        tolog0("clause 1.1.1.1")
                        give = False
                    else:
                        tolog0("clause 1.1.1.2")
                else:
                    tolog0("clause 1.1.2")
                    give = False
            if (piece == (23, 35) and piece2 == (5, 34)
                or piece == (23, 5) and piece2 == (35, 6)):
                tolog0("clause1bis2 piecelen2 %f odo2 %f" % (piecelen2, odo2))
                # we should test: if the other car wants to turn 34-23, we
                # should not give way to it
                if piecelen2 - odo2 < 1.0:
                    d = 0.4 - 0.25 - odo - stopd
                    if d < 0:
                        tolog0("clause 1.2.2.1")
                        give = False
                    else:
                        tolog0("clause 1.2.2.2")
                else:
                    tolog0("clause 1.2.1")
                    give = False
            if (piece == (6, 23) and piece2 == (34, 35)
                or piece == (34, 23) and piece2 == (6, 5)):
                tolog0("clause 1.3")
                d = piecelen - odo + 0.4 - 0.25 - stopd
            if (piece[1] == 35 and piece2[1] == 35
                or piece[1] == 5 and piece2[1] == 5):
                d = 0.4 - 0.25 - odo - stopd
                if d < 0:
                    tolog0("clause 1.4.1")
                    give = False
                else:
                    tolog0("clause 1.4.2")
                if False:
                    # the distance to the line where we must stop, minus
                    # the distance between the front edge and the middle
                    #odox = 0.45 - stopd
                    odox = 0.55 - stopd
                    tolog0("%s %s speed %f stopd %f odox %f odo %f" % (
                            c.info, c2.info, c.finspeed, stopd, odox, odo))
                    if odo > odox:
                        # short-circuit this, assuming that if we got here,
                        # we have already done the right thing before, and
                        # should keep doing that
                        # no, that's only true if we should give way, but
                        # wrong if the other car is too far away to be
                        # disturbed by us
                        # and if the other car is close enough, we crash...
                        give = False
                        tolog0("clause1.1")
                    else:
                        tolog0("clause1.2")
                        # 0.20 + half carlength = 0.45
                        d = 0.20-odo+stopd

        if foll or give or not ok:

            relation = ""
            if not ok:
                relation += "/crash"
                d = 0.0
            if foll:
                relation += "/following"
            if give:
                relation += "/givewayto"

            if relation[0] == "/":
                relation = relation[1:]

            tolog0("%d %s %s %f %s %s %f" % (
                    time.time(), c.info, relation,
                    d, str(piece), str(pos), odo))
            tolog0("   %s %s %s %f" % (
                    c2.info, str(piece2), str(pos2), odo2))

            if not ok:
                c2.conn.send("carsinfront 1 " +
                             "crash %f %f %f %d %d\n" %
                             (d, c.x, c.y, c.n, onlyif))

            #print((c.x, c.y, c2.x, c2.y, d))
            stri = "car in front of car %s: %s: %f" % (
                c.info, c2.info, d)
            if doprint != stri:
                #print(stri)
                doprint = stri
            l = l + [(relation, d, c2.x, c2.y, c2.info, onlyif)]

    fronts = "carsinfront %d" % len(l)
    for tup in l:
        fronts = fronts + " " + ("%s %f %f %f %s %d" % tup)
    c.conn.send(fronts + "\n")

    c.following = ("following" in fronts)
    tolog0("%s setting following to %s" % (c.info, str(c.following)))

    tolog0("sent to %s %s" % (c.info, fronts))

def handlebatterytimeout(c):
    while True:
        if time.time() > c.battery_seen + 120:
            c.v4.set("battery unknown")
        time.sleep(1)

def deletecar(c):
    c.alive = False

    if c.currentpos != None:
        (or1, or2, or3, or4, or5, or6) = c.currentpos
        g.w.delete(or1)
        g.w.delete(or2)
        g.w.delete(or3)
        g.w.delete(or4)
        g.w.delete(or5)
        g.w.delete(or6)
    for win in c.windows:
        g.w.delete(win)
    del cars[c.n]

def handleheart(c, conn):
    while c.alive:
        if time.time() > c.heart_seen + c.timeout:
            print("timed out: " + c.info)
            deletecar(c)
            return
        time.sleep(5)

def esend_continue(c):
    c.conn.send("continue\n")

def arravg(l):
    n = len(l)
    sum = 0.0
    for v in l:
        sum += v
    return sum/n

waitingcars = []

def connsend(c, data):
    try:
        c.conn.send(data)
    except Exception as e:
        print("exception %s" % str(e))
        c.alive = False

def handlerun(conn, addr):
    global waitingcars

    dataf = linesplit(conn)
    print 'Connected %s (at %f)' % (addr, time.time())

    data = dataf.next()
    if data[-1] == "\n":
        data = data[:-1]
    if data[-1] == "\r":
        data = data[:-1]
    l = data.split(" ")

    print l

    warnbattery = 0

    if l[0] == "info":
        c = Car()

        c.conn = conn

        thread.start_new_thread(handlebatterytimeout, (c,))
        thread.start_new_thread(handleheart, (c, conn))

        # in case the car waited for us to start
        esend_continue(c)

        car = l[1]
        c.v3.set("car %s" % car)
        c.info = car
        c.addr = addr
        print("car %s" % car)
        if len(l) > 2:
            cartime = float(l[2])
            print("time for %s = %f" % (c.info, cartime))
            if g.timesynched:
                diff = time.time()-g.t0
                connsend(c, "sync 1 %f\n" % diff)
            else:
                g.timesynched = True
                g.t0 = time.time() - cartime
                connsend(c, "sync 0\n")

    elif l[0] == "list":
        iplist = []
        for car in cars.values():
            iplist.append(car.addr[0])
        conn.send(json.dumps(iplist) + "\n")
        conn.close()
        return
    elif l[0] == "cargoto":
        carn = l[1]
        found = None
        for car in cars:
            c = cars[car]
            if carn == c.info:
                found = c
                break
        if not found:
            conn.send("{\"error\": \"no running car named %s\"}\n" % l[1])
        else:
            x = float(l[2])
            y = float(l[3])
            if x < 1.5:
                x += 0.6
            else:
                x -= 0.6
            l[2] = str(x)
            conn.send(c, "%s\n" % " ".join(l))

        conn.close()
        return
    else:
        conn.send("{\"error\": \"expected keyword 'info', 'list' or 'cargoto', got '%s'\"}\n" % l[0])
        conn.close()
        return

    spavg = []
    spavgn = 50

    t1 = time.time()

    for data in dataf:

        if not c.alive:
            conn.close()
            return

        if data[-1] == "\n":
            data = data[:-1]

        #print "received (%s)" % data
        l = data.split(" ")
        # mpos = from marker; d = from dead reckoning
        #print (c, l)
        if l[0] == "mpos" or l[0] == "dpos":
            #print (time.time(), c, l)
            x = float(l[1])
            y = float(l[2])
            ang = float(l[3])
            if (x, y, ang) == (c.x, c.y, c.ang):
                samepos = True
            else:
                samepos = False

            # if samepos is set to True, disconnecting cars doesn't
            # erase them for some reason
            samepos = False

            c.x = x
            c.y = y
            c.ang = ang
            time1 = float(l[4])
            adj = int(l[5])
            # comes in as a float, but has only integer accuracy
            insp = float(l[6])
            c.finspeed = insp
            spavg = [insp] + spavg
            if len(spavg) > spavgn:
                spavg = spavg[0:spavgn]
            spavg1 = arravg(spavg)

            c.v2.set("time %.1f" % time1)

            t = time.time()

            #c.v5.set("speed %d" % insp)
            if t-t1 > 1.0:
                c.v5.set("speed %d" % round(spavg1))
                t1 = t

            if (l[0] == "dpos" or l[0] == "mpos" and g.show_markpos
                and not samepos):
                update_carpos1(x, y, ang, c)
            if l[0] == "mpos" and g.show_markpos1:
                set_markerpos(x, y, c, adj)
            check_other_cars(c)

            tolog0("%s %f %f %s" % (c.info, x, y, l[0]))

        elif l[0] == "badmarker":
            x = float(l[1])
            y = float(l[2])
            set_badmarkerpos(x, y, c)
        elif l[0] == "waitallcars":
            waitingcars.append(c.n)
            if len(waitingcars) == len(cars):
                for n in waitingcars:
                    cx = cars[n]
                    connsend(cx, "waitallcarsdone\n")
                waitingcars = []
        elif l[0] == "odometer":
            odo = int(l[1])
            c.v.set("%d pulse%s = %.2f m" % (
                    odo, "" if odo == 1 else "s",
                    float(odo)/5*pi*10.2/100))
        elif l[0] == "heart":
            #print("heart %s" % c.info)
            c.heart_seen = time.time()
            c.heart_t = float(l[1])
            c.heart_n = int(l[2])
            connsend(c, "heartecho %.3f %.3f %d\n" % (
                    c.heart_seen - c.t0, c.heart_t, c.heart_n))
        elif l[0] == "message":
            s = " ".join(l[1:])
            c.v8.set(s)
        elif l[0] == "stopat":
            i = int(l[1])
            i -= 1
            print "%s stopped at %d" % (c.info, i)
#            if i == 4 or i == 7 or i == 9 or i == 12:
#            if i == 2 or i == 4 or i == 6 or i == 8 or i == 10 or i == 12 or i == 14 or i == 16 or i == 18 or i == 0:
            # fits path3:
            if i == 2 or i == 4 or i == 6 or i == 8 or i == 10 or i == 12 or i == 14 or i == 0:
                if False:
                    if i == 4:
                        j = 7
                    elif i == 7:
                        j = 9
                    elif i == 9:
                        j = 12
                    elif i == 12:
                        j = 4
                else:
                    j = i+2
                    if i == 14:
                        j = 0

                print "occupied: %s" % str(occupied)
                print "waiting: %s" % str(waiting)
                for ci in cars:
                    carx = cars[ci]
                    print "%s %s" % (carx.info, str(carx.waitingat))

                if j not in occupied:
                    print " %s not occupied" % str(j)
                    print " continuing %s" % c.info
                    esend_continue(c)
                    occupied[j] = c

                    if i in occupied:
                        c1 = occupied[i]
                        print " %d was occupied by %s" % (i, c1.info)
                        del occupied[i]
                        wi = i
                        while wi in waiting:
                            print " %d was waited for" % wi
                            c2 = waiting[wi]
                            print " %s waited for %d" % (c2.info, wi)
                            print " continuing %s" % c2.info
                            occupied[wi] = c2
                            wi2 = c2.waitingat
                            if wi2 in occupied:
                                del occupied[wi2]
                            c2.waitingat = None
                            esend_continue(c2)
                            del waiting[wi]
                            wi = wi2
                else:
                    print " %s occupied, waiting" % str(j)
                    waiting[j] = c
                    print " waitingat %d" % i
                    c.waitingat = i

                s1 = ""
                if len(occupied.keys()) != 0:
                    s1 += " occupied: "
                    print(occupied)
                    for k in occupied:
                        c1 = occupied[k]
                        s1 += " %s@%d" % (c1.info, k)
                if len(waiting.keys()) != 0:
                    s1 += " waiting: "
                    for k in waiting:
                        c1 = waiting[k]
                        s1 += " %s@%d" % (c1.info, k)
                v5.set(s1)

            else:
                esend_continue(c)

        elif l[0] == "battery":
            b = float(l[1])
            c.battery_seen = time.time()
            if b < 6.8:
                warnbattery = (warnbattery + 1) % 2
            else:
                warnbattery = 0
            if warnbattery == 0:
                c.v4.set("battery %.2f" % b)
            else:
                c.v4.set("")
        elif l[0] == "markers":
            s = " ".join(l[1:])
            if False:
                delim = "/-\\|"[c.markern]
                c.markern = (c.markern + 1) % 4
            else:
                delim = "- "[c.markern]
                c.markern = (c.markern + 1) % 2
            s = delim + " " + s
            c.v7.set(s)
        elif l[0] == "between":
            i1 = int(l[1])
            i2 = int(l[2])
            c.lastnode = i1
            c.nextnode = i2
            c.betweenlist.append((i1, i2))
            if len(c.betweenlist) > 2:
                c.betweenlist = c.betweenlist[-3:]
            if len(l) > 3:
                c.nextnode2 = int(l[3])
            else:
                c.nextnode2 = -1
            
            print("%f %s between %d and %d (then %d)" % (
                    time.time()-g.t0, c.info, i1, i2, c.nextnode2))
        else:
            print "received (%s)" % data

    conn.close()
    print("connection closed %d %s" % (c.n, c.info))
    deletecar(c)

def run():
    totdata = ""

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(1)
    while True:
        print 'Listening (at %f)' % time.time()
        (conn, addr) = s.accept()
        conn.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
        thread.start_new_thread(handlerun, (conn, addr))

thread.start_new_thread(run, ())
