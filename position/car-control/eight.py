from math import cos, sin, pi, atan2

def eightpoint(cy, ang):
    cx = 1.5
    R = 1.0
    x = cx + R*sin(ang*pi/180)
    y = cy + R*cos(ang*pi/180)
    return (x, y)

piece1 = [7, 11, 17, 24, 28, 30, 36]
piece2 = [35, 32, 27, 23, 19, 13, 6]
piece3 = [5, 10, 16, 23, 26, 29, 34]
piece4 = [33, 31, 25, 22, 18, 12, 4]

nodenumbers = piece1 + piece2 + piece3 + piece4

# 'ways' is not used in nav.py
ways = dict()

ways[1] = piece1 + [35, 34] + piece4 + [5, 6] + [piece1[0]]
ways[3] = piece2
ways[4] = piece3

nodes = dict()

def eightarc(nodenumbers, cy, angleoffset):
    for i in range(-3, 3+1):
        ang = 30*i
        (x, y) = eightpoint(cy, ang+angleoffset)
        nr = nodenumbers[0]
        nodenumbers = nodenumbers[1:]
        if nr not in nodes:
            nodes[nr] = (x, y)
    return nodenumbers

def eightpath(y1, y2, y3):
    R = 1.0

    l = nodenumbers

    l = eightarc(l, y1 - R, 0)
    l = eightarc(l, y2 + R, 180)
    l = eightarc(l, y2 - R, 0)
    l = eightarc(l, y3 + R, 180)

    for nr in nodes:
#        print("%d %f %f" % (nr, nodes[nr][0], nodes[nr][1]))
        pass

def makepath(offset, path):
    path1 = []
    x1 = None
    y1 = None
    n = 0
    i1 = None
    for (i, (x0, y0)) in path:
        
        if x1 == None:
            pass
        else:
            dx = x0-x1
            dy = y0-y1
            angle = atan2(dx, -dy)

            x = x1
            y = y1

            path1.append(('go', 40, i1,
                          x+offset*cos(angle),
                          y+offset*sin(angle)))

        i1 = i
        x1 = x0
        y1 = y0
        n += 1

    # use the same angle as for the previous point
    path1.append(('go', 40, i1,
                  x1+offset*cos(angle),
                  y1+offset*sin(angle)))

    return path1

