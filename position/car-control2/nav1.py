import time
import random

import nav_log
from nav_log import tolog, tolog0

from nav_util import sign, dist, start_new_thread

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


def goto(x, y, state):
    start_new_thread(gotoaux, (x, y, state))

def gotoaux(x, y, state):
    print("gotoaux %f %f %s" % (x, y, state))
    driving.drive(0)
    if state == "accident":
        g.signalling = True
        start_new_thread(signal, ())

    time.sleep(4)
    driving.drive(30)
    nav2.goto_1(x, y)
    g.signalling = False
    driving.drive(0)

def whole4(dir):
    start_new_thread(whole4aux, (dir,))


def rev(l0):
    l = l0[:]
    l.reverse()
    return l


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

    driving.drive(0)
    time.sleep(4)
    driving.drive(speed0)

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
            driving.drive(0)
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
            nav_tc.send_to_ground_control("between %d %d %d" % (i2, i1, i3))
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
            nav2.goto_1(x, y)

        # idea: let the connecting node always be a part in both
        # pieces; then we get a free check whether they actually go
        # together

        path = eight.piece2path(nextpiece, dir, 0.25)
        lpath = eight.piece2path(nextpiece, dir, 0.15)
        rpath = eight.piece2path(nextpiece, dir, 0.35)

