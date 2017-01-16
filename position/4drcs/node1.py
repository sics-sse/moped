import time

from util import *

from node import Node

path = [(1, 1),
        (3, 1),
        (3, 3),
        (2, 4),
        (1, 3)]

class Node1(Node):
    def task(self, t):
        print("Node1: task %s" % str(t))


        # t = ('goto', n)
        n = t[1]


        x0 = self.wm.x
        y0 = self.wm.y

        # missing: generating several plans, and having them judged
        p = []
        eta0 = 0.0
        for coords in path:
            x = coords[0]
            y = coords[1]
            d = dist(x, y, x0, y0)
            x0 = x
            y0 = y
            # The 1.1 here allows for some swaying
            eta = eta0 + d/self.wm.speedlimit*1.1
            eta0 = eta
            p.append(('goto', x, y, eta))

        # missing: doing the planning in a separate thread and giving the
        # best plan to the executor


        # why do we make a plan with many items if we replan during the
        # first one?

        for i in range(0, len(p)):
            if i == len(p)-1:
                # Don't forget the last task, when there really are none
                # following.
                tasks = p[i:i+1]
            else:
                tasks = p[i:i+2]

            # We could just wait for the subgoal, but then we don't get to
            # judge intermediate status
            # We could run it in a separate thread, but now we use a
            # generator instead.
            for status in self.lower.task(tasks):
                (di, bearing) = status
                if di < 0.1:
                    print("subgoal achieved, t = %f" % (time.time()-self.wm.t0))
                    break
                # This criterion could be too simplistic
                if abs(bearing) > 90 and di < 0.8:
                    print("missed the point")
                    break

        # This is the wrong place to set speed
        self.wm.v = 0.0
