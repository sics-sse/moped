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
    # 3.2 and lower don't have 'daemon'
    #threading.Thread(target=f, args=args, daemon=True).start()
    threading.Thread(target=f, args=args).start()

def rev(l0):
    l = l0[:]
    l.reverse()
    return l

def min(x, y):
    if x < y:
        return x
    else:
        return y
