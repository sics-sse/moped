import sys
import threading

from math import sqrt

def dist(x1, y1, x2, y2):
    return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

def sign(x):
    if x < 0:
        return -1
    if x > 0:
        return 1
    return 0

def start_new_thread(f, args):
    return start_new_thread_really(f, args)

def start_new_thread_really(f, args):
    # can be done much better with packaging.Version or something
    version = sys.version.split(" ")[0].split(".")
    if int(version[1]) < 3:
        threading.Thread(target=f, args=args).start()
    else:
        threading.Thread(target=f, args=args, daemon=True).start()

def rev(l0):
    l = l0[:]
    l.reverse()
    return l

def min(x, y):
    if x < y:
        return x
    else:
        return y
