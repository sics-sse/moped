import os

from nav_util import start_new_thread

def signal():
    while g.signalling:
        os.system("(python tone2.py 8000 3000 1200;python tone2.py 8000 3000 1000) 2>/dev/null")

def obstaclebeep():
    os.system("python tone2.py 16000 3000 2400 2>/dev/null")

def dospeak(s, p):
    if '#' in s:
        s = s.replace('#', str(g.speakcount))
    os.system("espeak -a500 -p%d '%s' >/dev/null 2>&1" % (p, s))

def speak(str):
    p = 50
    if g.VIN == "car2":
        p = 80
    start_new_thread(dospeak, (str, p))




def blinkleds():
    g.ledstate = (g.ledstate + 1)%7
    setleds(0, g.ledstate)

def warningblink(state):
    if state == True:
        if g.warningblinking == True:
            return
        setleds(7, 0)
        g.warningblinking = True
    else:
        if g.warningblinking == False:
            return
        setleds(0, 7)
        g.warningblinking = False

def setleds(mask, code):
    print("setleds %d %d" % (mask, code))

    if False:
        cmd = "/home/pi/can-utils/cansend can0 '461#060000006D3%d3%d00'" % (
            mask, code)
        os.system(cmd)
    else:
        g.ledcmd = (mask, code)


def signalinit():
    g.warningblinking = None
    g.ledstate = 0
    g.speakcount = 1

