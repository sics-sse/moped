import time
import random

import nav_log
from nav_log import tolog, tolog0

from nav_util import sign, dist, start_new_thread, rev

import eight
import driving
import nav2
import nav_tc

def nav1init():
    g.user_pause = False
    g.randdict = dict()


def pause():
    g.user_pause = True

def cont():
    g.user_pause = False


def whole4(p = [34, 35, 6]):
    start_new_thread(whole4aux, (p,))


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

def whole4aux(path0):

    speed0 = 20

    driving.drive(0)
    time.sleep(4)
    driving.drive(speed0)

    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    # idea: from the current position, determine which piece we can
    # start with

    lx = None
    ly = None
    rx = None
    ry = None

    i1 = -1

    nextpiece = path0
    nextpiece_e = eight.insert_waypoints_l(nextpiece)

    while True:
        i10 = nextpiece[-1]
        i2 = nextpiece[-2]

        thispiece = nextpiece_e

        print("(%d, %d)" % (i10, i2))

        if (i10, i2) == (23, 34):
            # not possible: 35
            nextpiece = randsel([23, 6], [23, 5])
            #nextpiece = rev(eight.piece3a)
        elif (i10, i2) == (6, 23):
            # not possible: 5
            nextpiece = [6, 35]
        elif (i10, i2) == (35, 6):
            nextpiece = randsel([35, 23], [35, 34, 5])
            #nextpiece = eight.piece5 + eight.piece4
        elif (i10, i2) == (23, 35):
            # not possible: 34
            nextpiece = randsel([23, 5], [23, 6])
            #nextpiece = eight.piece2b
        elif (i10, i2) == (5, 23):
            # not possible: 6
            nextpiece = [5, 34]
        elif (i10, i2) == (34, 5):
            nextpiece = randsel([34, 23],
                                [34, 35, 6])
            #nextpiece = rev(eight.piece5) + rev(eight.piece1)
        elif (i10, i2) == (23, 6):
            # not possible: 5
            nextpiece = randsel([23, 35], [23, 34])
            #nextpiece = eight.piece3b
        elif (i10, i2) == (23, 5):
            # not possible: 6
            #nextpiece = randsel(rev(eight.piece2a), eight.piece3b)
            #nextpiece = rev(eight.piece2a)
            # temporarily avoid going 16-23-27 (now named 5-23-35)
            nextpiece = [23, 34]
        elif (i10, i2) == (5, 34):
            nextpiece = randsel([5, 6, 35], [5, 23])
            #nextpiece = eight.piece3a + [23]
        elif (i10, i2) == (6, 35):
            nextpiece = randsel([6, 5, 34],
                                [6, 23])
            #nextpiece = rev(eight.piece2b) + [23]
        elif (i10, i2) == (35, 23):
            # not possible: 34
            nextpiece = [35, 6]
        elif (i10, i2) == (34, 23):
            # not possible: 35
            nextpiece = [34, 5]
        else:
            print("impossible combination (%d, %d), whole4aux exits" % (
                    i10, i2))
            driving.drive(0)
            return

        nextpiece_e = eight.insert_waypoints_l(nextpiece)
        print("nextpiece %s" % str(nextpiece))
        print("nextpiece_e %s" % str(nextpiece_e))

        print("thispiece = %s" % str(thispiece))

        # For the box computation, we should tell gopath what
        # nextpiece[0] is, too
        success = gopath(thispiece)
        if not success:
            print("gopath failed, whole4aux exits")
            driving.drive(0)
            return

        # idea: let the connecting node always be a part in both
        # pieces; then we get a free check whether they actually go
        # together

def gopath(path0):
    g.last_send = None

    #print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    path = eight.piece2path(path0, -0.25)
    lpath = eight.piece2path(path0, -0.15)
    rpath = eight.piece2path(path0, -0.35)

    lx = None
    ly = None
    rx = None
    ry = None

    i1 = -1

    for j in range(0, len(path)):
        (i, x, y) = path[j]
        if g.remote_control:
            print("remote control/crash")
            return False
        i2 = i1
        i1 = i
        if j == len(path)-1:
            i3 = -1
        else:
            (i3, _, _) = path[j+1]
        nav_tc.send_to_ground_control("between %d %d %d" % (i2, i1, i3))
        lxprev = lx
        rxprev = rx
        lyprev = ly
        ryprev = ry
        (_, lx, ly) = lpath[j]
        (_, rx, ry) = rpath[j]
        if lxprev != None:
            if False:
                print("keep between (%f,%f) - (%f,%f) and (%f,%f) - (%f,%f)" % (
                        lxprev, lyprev, lx, ly,
                        rxprev, ryprev, rx, ry))
            g.currentbox = [(lxprev, lyprev, lx, ly),
                            (rxprev, ryprev, rx, ry)]
        nav2.goto_1(x, y)
    return True

def travel(n0, n1, n2 = None, nz=None):
    routes = eight.paths(n0, n1, n2, nz)
    if routes == []:
        print("no route found")
        return False

    routes_p = eight.paths_p(n0, n1, n2, nz)

    #print(routes)
    #print(routes_p)

    # Value judgment: pick the shortest
    routes.sort()
    (d, r) = routes[0]

    routes_p.sort()
    (d1, r1) = routes_p[0]
    r2 = eight.insert_waypoints_l(r1)

    print((d, r))
    print((d1, r2))

    print("travel1")
    driving.drive(20)
    print("travel2")
    gopath(r)
    print("travel3")
    driving.drive(0)
    print("travel4")

    return True
