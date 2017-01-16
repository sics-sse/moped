import threading
import time
from math import *

class WM:
    def __init__(self):
        # Current dynamic parameters of the car
        self.x = 0.0
        self.y = 0.0
        self.ang = 0.0
        self.v = 0.0

        # Angular velocity; set this in order to turn
        self.angv = 0.0

        # Static information from the map
        self.speedlimit = 0.5

        # Time base
        self.t0 = time.time()

        # Set this to True to stop all threads
        self.alldone = False

def worldmodel(wm):
    print("worldmodel")

    threading.Thread(target=report, args=(wm,)).start()

    t0 = time.time()
    while not wm.alldone:
        time.sleep(0.01)

        t1 = time.time()
        dt = t1-t0
        t0 = t1
        wm.x += sin(wm.ang*pi/180)*wm.v*dt
        wm.y += cos(wm.ang*pi/180)*wm.v*dt
        wm.ang += wm.angv*dt

def report(wm):
    t0 = time.time()
    while not wm.alldone:
        time.sleep(0.2)
        t1 = time.time()
        dt = t1-t0
        if dt > 2.0:
            print("(%f,%f) ang %f v %f" % (wm.x, wm.y, wm.ang, wm.v))
            t0 = t1
