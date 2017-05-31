import time

from nav_util import dist

def tologaux(str0, level):

    if g.targetx and g.targety:
        d = dist(g.ppx, g.ppy, g.targetx, g.targety)
    else:
        d = -1

    str = "(speed %d time %.3f %f %f %f %f %f %3f) %s" % (
        g.finspeed,
        time.time() - g.t0,
        g.ppx,
        g.ppy,
        g.ang,
        d,
        g.battery,
        g.can_ultra,
        str0)
    g.logf.write(str + "\n")
    if level >= 2:
        print(str)

def tolog0(str):
    tologaux(str, 0)

def tolog(str):
    tologaux(str, 1)

def tolog2(str):
    tologaux(str, 2)

def logthread(q):
    n = 0
    lenmax = -1
    while True:
        n += 1
        s = q.get()
        q.task_done()
        g.qlen -= 1
        if lenmax < g.qlen:
            lenmax = g.qlen
        if n%1000 == 0:
            pass
            #print("queue %d %d" % (g.qlen, lenmax))
        g.accf.write(s)
        time.sleep(0.000001)
