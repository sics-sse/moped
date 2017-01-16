import logging
import time
from math import *

from util import dist

from node import Node

class Node2(Node):
    def task(self, taskl):
        print("Node2: task %s" % str(taskl))

        # taskl = [('goto', x, y, eta), ...]
        # There may be a future goal at taskl[1] but we won't use it,
        # since we don't make subgoals here.
        task = taskl[0]
        x = task[1]
        y = task[2]
        eta = task[3]
        print("eta = %f" % eta)

        while True:
            di = dist(x, y, self.wm.x, self.wm.y)
            t = time.time()
            if self.wm.v == 0:
                etaerror = 0
            else:
                etaerror = di/self.wm.v - (eta+self.wm.t0-t)

            if abs(etaerror) > 0.1 or self.wm.v == 0:
                vnew = di/(eta+self.wm.t0-t)
                if vnew-self.wm.v > 0.05:
                    vnew = self.wm.v + 0.05
                elif vnew-self.wm.v < -0.05:
                    vnew = self.wm.v - 0.05

                self.wm.v = vnew

            a = atan2(x-self.wm.x, y-self.wm.y)*180/pi
            d = a - self.wm.ang
            d = d%360
            if d > 180:
                d -= 360

            maxangv = 360/((2*pi*0.8)/self.wm.v)
            c = self.wm.v*2
            self.wm.angv = min(d*c, maxangv)

            logging.print("%f %f %f %f %f %f" % (
                    self.wm.x, self.wm.y, self.wm.ang, d, self.wm.v,
                    etaerror))
            time.sleep(0.1)
            # Report status, neutrally, but should we not check whether we
            # are done?
            yield((di, d))
