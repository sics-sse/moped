import threading

from node1 import Node1
from node2 import Node2

import worldmodel

class Vehicle:
    def __init__(self):
        self.wm = worldmodel.WM()

        self.n1 = Node1(self.wm)
        self.n2 = Node2(self.wm)

        self.n1.lower = self.n2
        self.n2.upper = self.n1

        threading.Thread(target=worldmodel.worldmodel, args=(self.wm,)).start()

        res = self.n1.task(('goto', 2))
        print("task returned %s" % str(res))

