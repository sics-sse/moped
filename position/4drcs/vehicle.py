import threading

from node1 import Node1
from node2 import Node2

import worldmodel

def taskthread(v, t):
    res = v.n1.task(t)
    print("task returned %s" % str(res))

class Vehicle:
    def __init__(self, name):
        self.wm = worldmodel.WM()

        self.name = name

        self.n1 = Node1(self)
        self.n2 = Node2(self)

        self.n1.lower = self.n2
        self.n2.upper = self.n1

        self.theclass = Vehicle

        Vehicle.vehicles.append(self)

        threading.Thread(target=worldmodel.worldmodel, args=(self,)).start()

    def task(self, t):
        self.thread = threading.Thread(target=taskthread, args=(self, t))
        self.thread.start()

    vehicles = []
