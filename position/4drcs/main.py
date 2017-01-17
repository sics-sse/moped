import sys
import os
import time

import logging

from vehicle import Vehicle

v = Vehicle()

logging.done()

v.wm.alldone = True

if False:
    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        os._exit(0)
