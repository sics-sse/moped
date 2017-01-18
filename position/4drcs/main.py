import sys
import os
import time

import logging

from vehicle import Vehicle

v1 = Vehicle("1")

v2 = Vehicle("2")
v2.wm.y = 1.0

if False:
    # go the same route, v2 has a head start
    v1.task(('goto', 2))
#time.sleep(10)
    v2.task(('goto', 2))
    v2.wm.speedlimit = 0.4
else:
    # go different routes, cross at some point: v2 should give way to v1
    v1.task(('goto', 1))
#time.sleep(10)
    v2.task(('goto', 2))

v1.thread.join()
v2.thread.join()

logging.done()

v1.wm.alldone = True
v2.wm.alldone = True

if False:
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        os._exit(0)
