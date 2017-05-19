import sys
import time
import random
import queue

import nav_log
from nav_log import tolog, tolog0

from nav_util import sign, dist, start_new_thread, rev, min

import nav_map
import driving
import nav2
import nav_tc

def nav1init():
    g.user_pause = False
    g.randdict = dict()
    g.lev = 0


def out(lev, s):
    if lev <= g.lev:
        sys.stdout.write(s + "\n")
        sys.stdout.flush()

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

thengoal0 = None

def whole4aux(path0):
    global thengoal0

    qfromplanner = queue.Queue(2)
    qtoplanner = queue.Queue(2)

    start_new_thread(planner0, (qfromplanner, qtoplanner))

    qtoplanner.put(path0)

    if g.simulate:
        speed0 = 30
    else:
        speed0 = 20

    driving.drive(0)
    if not g.simulate:
        # can be no sleep at all if we already did drive(0) earlier,
        # but must maybe be 4 if we didn't.
        pass
        #time.sleep(4)
    driving.drive(speed0)

    g.last_send = None

    print("speedsign = %d" % g.speedsign)
    g.speedsign = 1

    qfromlower = queue.Queue(2)
    qtolower = queue.Queue(2)

    start_new_thread(executor1, (qtolower, qfromlower))

    nextplan = None
    thengoal0 = None

    while True:
        if nextplan == None:
            nextplan = qfromplanner.get()
            qfromplanner.task_done()
        p = nextplan
        nextplan = None
        thengoal0 = None
        if p == 'stop':
            out(1, "0 executor got stop")
            driving.drive(0)
            return
        for status in executor0(p, qtolower, qfromlower):
            if status == 0:
                out(0, "0 executor failed, whole4aux exits")
                driving.drive(0)
                return
            else:
                out(1, "0 executor0 reported %d" % (status))
                if not qfromplanner.empty():
                    if nextplan == None:
                        nextplan = qfromplanner.get()
                        qfromplanner.task_done()
                        out(2, "1 next plan for executor0 is %s" % (str(nextplan)))
                        if nextplan != 'stop':
                            thengoal0 = nextplan[1]
                            out(1, "0 extending with %s" % str(thengoal0))
                        # ugly hack, for testing
                    else:
                        out(2, "0 another new plan for executor0 exists, but we already have one: %s" % str(nextplan))

def sendplan(q, plan):
    out(1, "0 planner produces %s" % str(plan))
    q.put(plan)

def planner0x(qfromplanner, qtoplanner):
    select = 1

    if select == 1:
        while True:
            sendplan(qfromplanner, [35, 6])
            sendplan(qfromplanner, [6, 23, 34])
            sendplan(qfromplanner, [34, 5])
            sendplan(qfromplanner, [5, 23, 35])
            sendplan(qfromplanner, [35, 6, 5])
            sendplan(qfromplanner, [5, 34, 23])
            sendplan(qfromplanner, [23, 6, 35])
            sendplan(qfromplanner, [35, 23, 5])
            sendplan(qfromplanner, [5, 34, 35])
    elif select == 2:
        sendplan(qfromplanner, [34, 35])
        sendplan(qfromplanner, [35, 6])
    elif select == 3:
        sendplan(qfromplanner, [34, 35])
        sendplan(qfromplanner, [35, 6])
        sendplan(qfromplanner, [6, 5, 34])
        sendplan(qfromplanner, [34, 23, 6])
    elif select == 4:
        while True:
            sendplan(qfromplanner, [35, 6])
            sendplan(qfromplanner, [6, 23, 35])
    else:
        print("unsupported select value %d" % select)
        return

    sendplan(qfromplanner, 'stop')

def planner0z(qfromplanner, qtoplanner):
    path0 = qtoplanner.get()
    qtoplanner.task_done()
    out(1, "0 planner got %s" % path0)

    sendplan(qfromplanner, path0)
    sendplan(qfromplanner, 'stop')

def planner0(qfromplanner, qtoplanner):
    # idea: from the current position, determine which piece we can
    # start with

    lx = None
    ly = None
    rx = None
    ry = None

    i1 = -1

    path0 = qtoplanner.get()
    qtoplanner.task_done()
    out(1, "0 planner got %s" % path0)

    nextpiece = path0

    while True:
        i10 = nextpiece[-1]
        i2 = nextpiece[-2]

        thispiece = nextpiece

        #print("(%d, %d)" % (i10, i2))

        if (i10, i2) == (23, 34):
            # not possible: 35
            #nextpiece = randsel([23, 6], [23, 5])
            nextpiece = [23, 5]
        elif (i10, i2) == (6, 23):
            # not possible: 5
            nextpiece = [6, 35]
        elif (i10, i2) == (35, 6):
            nextpiece = randsel([35, 23], [35, 34, 5])
            #nextpiece = [35, 34, 5]
        elif (i10, i2) == (23, 35):
            # not possible: 34
            #nextpiece = randsel([23, 5], [23, 6])
            nextpiece = [23, 6]
        elif (i10, i2) == (5, 23):
            # not possible: 6
            nextpiece = [5, 34]
        elif (i10, i2) == (34, 5):
            nextpiece = randsel([34, 23], [34, 35, 6])
            # (don't turn: only allow for one kind of give-way situation)
            #nextpiece = [34, 35, 6]
        elif (i10, i2) == (23, 6):
            # not possible: 5
            #nextpiece = randsel([23, 35], [23, 34])
            nextpiece = [23, 35]
        elif (i10, i2) == (23, 5):
            # not possible: 6
            # temporarily avoid going 16-23-27 (now named 5-23-35)
            #nextpiece = randsel([23, 34], [23, 35])
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

        #print("nextpiece %s" % str(nextpiece))
        #print("thispiece %s" % str(thispiece))

        sendplan(qfromplanner, thispiece)
        
def executor0(path, qtolower, qfromlower):

    out(1, "0 executor got plan %s" % str(path))

    for i in range(0, len(path)-1):
        g.nextdecisionpoint = 0
        path1 = [path[i], path[i+1]]
        if i < len(path)-2:
            path1.append(path[i+2])
            g.nextdecisionpoint = path[i+2]

        # path1 has either 2 or 3 elements
        qtolower.put(path1)
        while True:
            out(2, "0 executor thengoal = %s" % str(thengoal0))
            status = qfromlower.get()
            qfromlower.task_done()
            if status == 0:
                out(0, "0 executor1 failed; aborting")
                yield status
            elif status == 1:
                out(1, "0 executor1 reported %d" % status)
                break
            else:
                out(1, "0 executor1 reported %d" % (status))
                if len(path1) and thengoal0 != None:
                    out(2, "0 executor0 extending plan")
                    qtolower.put(('extend', thengoal0))
                    path1.append(thengoal0)
                else:
                    qtolower.put('pass')
                yield 2

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
        out(1, "1 executor got task %s" % (str(path1)))
        thengoal = None

        qtoplanner.put(('path', path1))
        (p, plen) = qfromplanner.get()
        qfromplanner.task_done()
        contflag = False
        while True:
            px = [i for (i, _) in p]

            # printed by gopath too
            #out(1, "1 executor got plan %d %s len %d" % (p[0][1], str(px), plen))
            for status in gopath(p, plen):
                if status == 0:
                    out(0, "1 gopath failed; aborting")
                    qtohigher.put(status)
                    # I think we should go the top of the outer loop,
                    # but returning at least aborts for real
                    return
                else:
                    # out(2, "1 gopath reports %d" % status)
                    # we should let the planner create this plan in advance,
                    # shouldn't we?
                    qtoplanner.put(('next', thengoal))
                    (p, plen) = qfromplanner.get()
                    qfromplanner.task_done()
                    out(2, "1 executor suggested new plan %s len %d" % (
                            str(p), plen))
                    qtohigher.put(2)
                    ack = qfromhigher.get()
                    qfromhigher.task_done()
                    #out(2, "1 higher acked %s" % str(ack))
                    if ack == 'pass':
                        pass
                    else:
                        (_, thengoal) = ack

                    if len(p) < 2:
                        # it seems len(p) is always 0 here
                        #out(1, "len(p) < 2: %s" % (str(p)))
                        continue
                    else:
                        contflag = True
                        break

            if not contflag:
                break
            contflag = False
        # we will just have put 2, too
        qtohigher.put(1)

def planner1(qfromplanner, qtoplanner):
    plann = 0
    path1_0 = None
    while True:
        info = qtoplanner.get()
        qtoplanner.task_done()
        if info[0] == 'path':
            plann += 1
            path1_0 = info[1][:]
            path1 = path1_0[0:2]
            reachedmiddle = False
            thengoal = None
            path1_e = nav_map.insert_waypoints_l(path1)

            pathlen = len(path1_e)
            curpathlen = pathlen

            path2_e = nav_map.insert_waypoints_l(path1_0)

            path2_e = [(i, plann) for i in path2_e]
            path1_e = [(i, plann) for i in path1_e]

            out(2, "1 path1 %s -> %s" % (str(path1), str(path1_e)))
            out(2, "1 path2 %s -> %s" % (str(path1_0), str(path2_e)))


            out(2, "1 planner1: task %s then %s" % (str(path1), str(path1_0[2:])))
            out(2, "1 -> plan %s" % str(path1_e))
        elif info[0] == 'next':
            # now we only pick the next point in our original plan,
            # but we should make a new plan each time.
            plann += 1

            item = info[1]
            out(2, "1 planner1: thengoal = %s" % str(item))
            if thengoal == None and item != None:
                if len(path1_0) == 2:
                    # since we use the last element in path2_e below,
                    # don't override an already existing nextpoint
                    path2_e.append((item, plann))
                    out(1, "1 planner1 extended plan with %s" % (item))
                    thengoal = item


            # pretend we did:
            path2_e = [(i, plann) for (i, _) in path2_e]

            if len(path2_e) > 1:
                # we want npath to also include the intermediate
                # decision points, if we haven't passed it
                # no, we are not supposed to pass it
                if path2_e[1][0] == path1_e[-1][0]:
                    out(1, "1 reached middle")
                    reachedmiddle = True
                if reachedmiddle:
                    out(1, "1 middle2")
                    npath = [path2_e[1][0], path2_e[-1][0]]
                    path3_e = []
                else:
                    npath = [path2_e[1][0], path1_e[-1][0], path2_e[-1][0]]
                    out(2, "1 path3_e %s -> " % (npath))
                    path3 = nav_map.insert_waypoints_l(npath)
                    path3_e = [(i, plann) for i in path3]
                    out(2, "1 -> %s" % (path3_e))
                path2_e = path3_e
            else:
                path2_e = []

            curpathlen -= 1

        else:
            out(0, "1 planner1 got unexpected command: %s" % (str(info)))

        # our caller doesn't know how long the part of the plan is which
        # corresponds to the actual goal, so we tell it how long
        # that part is

        qfromplanner.put((path2_e, curpathlen))

lastwaypoint = None

def gopath(path00, plen):
    global lastwaypoint

    g.last_send = None

    path0 = [i for (i, _) in path00]

    #out(2, "path00 = %s" % (str(path00)))
    outstr = "1 gopath: %d %s" % (path00[0][1], str(path0[0:plen]))
    if path00[plen:] != []:
        outstr += " + %s" % (str(path0[plen:]))
    out(1, outstr)

    #out(2, "1 speedsign = %d" % g.speedsign)
    g.speedsign = 1

    # two lanes:
    #path = nav_map.piece2path(path0, -0.25)
    # experimental, to make it crash more seldom:
    #path = nav_map.piece2path(path0, -0.1)
    # single lane:
    path = nav_map.piece2path(path0, 0)

    boxp = False
    if boxp:
        # the simulation can't make it without bigger margins
        lpath = nav_map.piece2path(path0, -0.15)
        rpath = nav_map.piece2path(path0, -0.35)

        lx = None
        ly = None
        rx = None
        ry = None

        i1 = -1

#    for j in range(0, len(path)):
    for j in range(0, plen):
        (i, x, y) = path[j]
        # pretend this is level 2:
        out(1, "2 goto %s = %s" % (str(i), str(path00[j])))
        tolog("nav1 goto_1 %d %f %f" % (i, x, y))
        if i == lastwaypoint:
            continue
            #pass
        if lastwaypoint != None:
            nav_tc.send_to_ground_control("between %d %d" % (
                    lastwaypoint, i))
        lastwaypoint = i

        if g.remote_control:
            print("1 remote control/crash")
            yield 0

        if False:
            i2 = i1
            i1 = i
            if j == len(path)-1:
                i3 = -1
            else:
                (i3, _, _) = path[j+1]
            nav_tc.send_to_ground_control("between %d %d %d" % (i2, i1, i3))

        if boxp:
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
            out(2, "1 goto_1 returned %d for node %d; we are at (%f, %f)" % (
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
    routes_p = nav_map.paths_p(n0, n1, n2, nz)
    if routes_p == []:
        print("no route found")
        return False

    # Value judgment: pick the shortest
    routes_p.sort()
    (d1, r1) = routes_p[0]
    r2 = nav_map.insert_waypoints_l(r1)

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
