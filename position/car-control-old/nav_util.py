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
    threading.Thread(target=f, args=args, daemon=True).start()

