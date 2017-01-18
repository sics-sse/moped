import threading
import time
from math import *

from util import *

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

        # Set to a distance if an obstacle appears
        self.obstacledist = None

        # Time base
        self.t0 = time.time()

        # Set this to True to stop all threads
        self.alldone = False

def worldmodel(vehicle):
    wm = vehicle.wm

    print("worldmodel")

    threading.Thread(target=report, args=(wm,)).start()
    threading.Thread(target=checkobstacles, args=(vehicle,)).start()

    t0 = time.time()
    while not wm.alldone:
        time.sleep(0.01)

        t1 = time.time()
        dt = t1-t0
        t0 = t1
        wm.x += sin(wm.ang*pi/180)*wm.v*dt
        wm.y += cos(wm.ang*pi/180)*wm.v*dt
        wm.ang += wm.angv*dt

def checkobstacles(vehicle):
    wm = vehicle.wm
    while not wm.alldone:
        for v in vehicle.theclass.vehicles:
            if v == vehicle:
                continue
            
            a = atan2(v.wm.x-vehicle.wm.x, v.wm.y-vehicle.wm.y)*180/pi
            d = a - vehicle.wm.ang
            d = d%360
            if d > 180:
                d -= 360


            if d < -20 or d > 100:
                continue
            # This sort of covers both a car in front of us in the same
            # lane, and a car coming from the right.
            # But we may miss a leader in a left curve, and falsely detect 
            # an approaching car in the other lane.
            # Looking at the other car's direction would help, but we will
            # miss an impending frontal collision.

            di = dist(vehicle.wm.x, vehicle.wm.y,
                      v.wm.x, v.wm.y)

            a2 = atan2(vehicle.wm.x-v.wm.x, vehicle.wm.y-v.wm.y)*180/pi
            d2 = a2 - v.wm.ang
            d2 = d2%360
            if d2 > 180:
                d2 -= 360
            
            if False:
                # attempt to make the car which is on the right continue
                # but at one point, the criterion becomes false, and it stops
                # and the other car went too far before stopping, anyway
                if d2 > 45 and d2 < 100:
                    continue

            if di < 0.5 and False:
                print("%s other vehicle %s dist %f" % (
                        vehicle.name, v.name, di))
            if di > 1.5:
                vehicle.wm.obstacledist = None
            else:
                vehicle.wm.obstacledist = di - 0.5
                if vehicle.wm.obstacledist < 0:
                    vehicle.wm.obstacledist = 0

        time.sleep(0.1)

def report(wm):
    t0 = time.time()
    while not wm.alldone:
        time.sleep(0.2)
        t1 = time.time()
        dt = t1-t0
        if dt > 2.0:
            print("(%f,%f) ang %f v %f" % (wm.x, wm.y, wm.ang, wm.v))
            t0 = t1
