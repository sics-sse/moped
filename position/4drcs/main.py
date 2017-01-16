import signal
import sys
import os
import time
import threading

import logging

from node1 import Node1
from node2 import Node2

import worldmodel

wm = worldmodel.WM()

n1 = Node1(wm)
n2 = Node2(wm)

n1.lower = n2
n2.upper = n1

threading.Thread(target=worldmodel.worldmodel, args=(wm,)).start()

res = n1.task(('goto', 2))
print("task returned %s" % str(res))

logging.done()

wm.alldone = True

if False:
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        os._exit(0)
