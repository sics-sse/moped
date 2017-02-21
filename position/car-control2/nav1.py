import time
import random
import queue

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


def whole4(p = [35, 6]):
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

    qfromplanner = queue.Queue(2)
    qtoplanner = queue.Queue(2)

    start_new_thread(planner0, (qfromplanner, qtoplanner))

    qtoplanner.put(path0)

    speed0 = 20
    driving.drive(0)
    time.sleep(4)
    driving.drive(speed0)

    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    qfromlower = queue.Queue(2)
    qtolower = queue.Queue(2)

    start_new_thread(executor1, (qtolower, qfromlower))

    while True:
        p = qfromplanner.get()
        qfromplanner.task_done()
        if p == 'stop':
            print("executor0 got stop")
            driving.drive(0)
            return
        for status in executor0(p, qtolower, qfromlower):
            if status == 0:
                print("executor0 failed, whole4aux exits")
                driving.drive(0)
                return
            else:
                print("executor0 reports %d" % status)

def planner0(qfromplanner, qtoplanner):
    qfromplanner.put([34,23,5,34,23])
    qfromplanner.put('stop')

def planner0x(qfromplanner, qtoplanner):
    # idea: from the current position, determine which piece we can
    # start with

    lx = None
    ly = None
    rx = None
    ry = None

    i1 = -1

    path0 = qtoplanner.get()
    qtoplanner.task_done()

    nextpiece = path0

    while True:
        i10 = nextpiece[-1]
        i2 = nextpiece[-2]

        thispiece = nextpiece

        print("(%d, %d)" % (i10, i2))

        if (i10, i2) == (23, 34):
            # not possible: 35
            nextpiece = randsel([23, 6], [23, 5])
        elif (i10, i2) == (6, 23):
            # not possible: 5
            nextpiece = [6, 35]
        elif (i10, i2) == (35, 6):
            nextpiece = randsel([35, 23], [35, 34, 5])
            #nextpiece = [35, 34, 5]
        elif (i10, i2) == (23, 35):
            # not possible: 34
            nextpiece = randsel([23, 5], [23, 6])
            nextpiece = [23, 6]
        elif (i10, i2) == (5, 23):
            # not possible: 6
            nextpiece = [5, 34]
        elif (i10, i2) == (34, 5):
            nextpiece = randsel([34, 23], [34, 35, 6])
            #nextpiece = [34, 35, 6]
        elif (i10, i2) == (23, 6):
            # not possible: 5
            nextpiece = randsel([23, 35], [23, 34])
        elif (i10, i2) == (23, 5):
            # not possible: 6
            # temporarily avoid going 16-23-27 (now named 5-23-35)
            nextpiece = randsel([23, 34], [23, 35])
            nextpiece = [23, 34]
        elif (i10, i2) == (5, 34):
            nextpiece = randsel([5, 6, 35], [5, 23])
        elif (i10, i2) == (6, 35):
            nextpiece = randsel([6, 5, 34], [6, 23])
            #nextpiece = [6, 5, 34]
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

        print("nextpiece %s" % str(nextpiece))
        print("thispiece %s" % str(thispiece))

        qfromplanner.put(thispiece)
        
def executor0(path, qtolower, qfromlower):

    for i in range(0, len(path)-1):
        path1 = [path[i], path[i+1]]
        if i < len(path)-2:
            path1.append(path[i+2])

        qtolower.put(path1)
        while True:
            status = qfromlower.get()
            qfromlower.task_done()
            if status == 0:
                print("executor1 failed; aborting")
                yield status
            elif status == 1:
                print("executor1 reported %d" % status)
                break

        yield 1
    return

#============================================================

def executor1(qfromhigher, qtohigher):
    qfromplanner = queue.Queue(2)
    qtoplanner = queue.Queue(2)

    start_new_thread(planner1, (qfromplanner, qtoplanner))

    while True:

        path1 = qfromhigher.get()
        qfromhigher.task_done()
        print("executor1: got task %s" % (str(path1)))

        qtoplanner.put(('path', path1))
        (p, plen) = qfromplanner.get()
        qfromplanner.task_done()
        print("executor1: got plan %s" % (str(p)))
        while True:
            for status in gopath(p, plen):
                if status == 0:
                    print("gopath failed; aborting")
                    qtohigher.put(status)
                    # I think we should go the top of the outer loop,
                    # but returning at least aborts for real
                    return
                else:
                    # print("gopath reports %d" % status)
                    qtoplanner.put(('next',))
                    (p, plen) = qfromplanner.get()
                    qfromplanner.task_done()
                    print("executor1: suggested new plan %s len %d" % (
                            str(p), plen))
                    continue
            break
        qtohigher.put(1)

def planner1(qfromplanner, qtoplanner):
    plann = 0
    while True:
        info = qtoplanner.get()
        qtoplanner.task_done()
        if info[0] == 'path':
            plann += 1
            path1_0 = info[1]
            path1 = path1_0[0:2]
            path1_e = eight.insert_waypoints_l(path1)

            pathlen = len(path1_e)

            path2_e = eight.insert_waypoints_l(path1_0)

            path2_e = [(i, plann) for i in path2_e]
            path1_e = [(i, plann) for i in path1_e]

            print("%s -> %s" % (str(path1), str(path1_e)))
            print("%s -> %s" % (str(path1_0), str(path2_e)))


            print("planner1: task %s then %s" % (str(path1), str(path1_0[2:])))
            print(" -> plan %s" % str(path1_e))
        elif info[0] == 'next':
            # now we only pick the next point in our original plan,
            # but we should make a new plan each time.
            plann += 1
            path2_e = path2_e[1:]
            path2_ex = path2_e
            if len(path2_ex) >= pathlen:
                path2_ex = path2_e[0:pathlen]
            print(" -> updated plan %s" % str(path2_ex))
        else:
            print("planner1 got unexpected command: %s" % (str(info)))
        # our caller doesn't know how long the part of the plan is which
        # corresponds to the actual goal, so we tell it how long
        # the that part was originally
        qfromplanner.put((path2_e, len(path1_e)))


def gopath(path00, plen):
    g.last_send = None

    print("path00 = %s" % (str(path00)))

    path0 = [i for (i, _) in path00]

    #print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    path = eight.piece2path(path0, -0.25)
    if False:
        lpath = eight.piece2path(path0, -0.15)
        rpath = eight.piece2path(path0, -0.35)

        lx = None
        ly = None
        rx = None
        ry = None

    i1 = -1

#    for j in range(0, len(path)):
    for j in range(0, plen):
        (i, x, y) = path[j]
        if g.remote_control:
            print("remote control/crash")
            yield 0

        if True:
            i2 = i1
            i1 = i
            if j == len(path)-1:
                i3 = -1
            else:
                (i3, _, _) = path[j+1]
            nav_tc.send_to_ground_control("between %d %d %d" % (i2, i1, i3))

        if False:
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

        if True:
            status = nav2.goto_1(x, y)
        else:
            time.sleep(1)
            status = 0

        if status != 0:
            print("goto_1 returned %d for node %d; we are at (%f, %f)" % (
                    status, i, g.ppx, g.ppy))
        if status == 2:
            #driving.drive(-20)
            #time.sleep(2)
            driving.drive(0)
            yield 0
        yield 1

    return

# Find the best route from n0 to n1 and go there (first straight to n0)
def travel(n0, n1, n2 = None, nz = None):
    routes_p = eight.paths_p(n0, n1, n2, nz)
    if routes_p == []:
        print("no route found")
        return False

    # Value judgment: pick the shortest
    routes_p.sort()
    (d1, r1) = routes_p[0]
    r2 = eight.insert_waypoints_l(r1)

    print((d1, r2))

    print("travel1")
    driving.drive(20)
    print("travel2")
    # we should use gopath as a generator
    gopath(r)
    print("travel3")
    driving.drive(0)
    print("travel4")

    return True

# Find out where we are, and then go to n1
def travelto(n1, n2 = None, nz = None):
    where = findpos(g.ppx, g.ppy, g.ang)
    print(where)
