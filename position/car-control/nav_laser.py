import time
import sys

sys.path.append("lasersensor/python")

import VL53L0X_example as vl

def readdist():
    vl.setup()
    vl.scan(g.dists)
