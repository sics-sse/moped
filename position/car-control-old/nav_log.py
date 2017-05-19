import time

from nav_util import *

def tolog2(str0, stdout):

    stdout = False

    if g.targetx and g.targety:
        d = dist(g.ppx, g.ppy, g.targetx, g.targety)
    else:
        d = -1

    str = "(speed %d time %.3f %f %f %f %f %f %3f) %s" % (
        g.inspeed,
        time.time() - g.t0,
        g.ppx,
        g.ppy,
        g.ang,
        d,
        g.battery,
        g.can_ultra,
        str0)
    g.logf.write(str + "\n")
    if stdout:
        print(str)

def tolog0(str):
    tolog2(str, False)

def tolog(str):
    tolog2(str, True)

