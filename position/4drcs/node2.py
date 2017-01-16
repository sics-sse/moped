import logging
import time
from math import *

from util import dist

from node import Node

class Node2(Node):
    def task(self, t):
        print("Node2: task %s" % str(t))

        # t = [('goto', x, y, eta), ...]
        # There may be a future goal at t[1] but we won't use it,
        # since we don't make subgoals here.
        t1 = t[0]
        x = t1[1]
        y = t1[2]
        eta = t1[3]
        print("eta = %f" % eta)

        while True:
            # This is the wrong place to set speed
            self.wm.v = 0.5

            a = atan2(x-self.wm.x, y-self.wm.y)*180/pi
            d = a - self.wm.ang
            d = d%360
            if d > 180:
                d -= 360

            maxangv = 360/((2*pi*0.8)/self.wm.v)
            c = self.wm.v*2
            self.wm.angv = min(d*c, maxangv)

            logging.print("%f %f %f %f" % (self.wm.x, self.wm.y, self.wm.ang, d))
            time.sleep(0.1)
            # Report status, neutrally, but should we not check whether we
            # are done?
            yield((dist(x, y, self.wm.x, self.wm.y), d))
