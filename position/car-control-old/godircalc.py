from math import *

def dist(x1, y1, x2, y2):
    return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

# going clockwise from a1, does a2 come before a3?
def between(a1, a2, a3):
    a21 = (a2-a1)%360
    if a21 < 0:
        a21 += 360
    a31 = (a3-a1)%360
    if a31 < 0:
        a31 += 360
    b = (a21 < a31)
    print("between %f %f %f -> %f %f -> %s" % (a1, a2, a3, a21, a31, str(b)))
    return b

def nbetween(l):
    for i in range(0, len(l)-2):
        if not between(l[i], l[i+1], l[i+2]):
            return False
    return True

def godir(ppx, ppy, ang, x1, y1, ang1, w):
    if w:
        w.draw_car(x1, y1, ang1)
        w.draw_car(ppx, ppy, ang)

    # The "final line" is the line passing through (x1, y1) with angle ang1.
    # When the car is on it with angle ang1, it can simply go straight to
    # (x1, y1).

    print((ppx, ppy, ang))

    R = 0.93 # for car5 reverse

    # can the car turn without hitting the wall?
    # use only the original corridor, not the eastern one
    dx = R*cos(ang*pi/180)
    dy = -R*sin(ang*pi/180)
    turnings = []
    for dir in [-1, 1]:
        cx = ppx + dir*dx
        cy = ppy + dir*dy
        if w:
            w.addcircle(cx, cy, R, "blue")
        if cx > R and cx < 3.0-R and cy > 12.0+R and cy < 19.7-R:
            turnings.append((dir, cx, cy, -180, 180))
        # convention: first angle to second angle, going clockwise, spans
        # the available arc (so min and max are not useful words)
        elif cx < R and cy > 12.0+R and cy < 19.7-R:
            turnings.append((dir, cx, cy,
                             -asin(cx/R)*180/pi,
                             180+asin(cx/R)*180/pi))
        elif cx > 3.0-R and cy > 12.0+R and cy < 19.7-R:
            turnings.append((dir, cx, cy,
                             asin((3.0-cx)/R)*180/pi,
                             180-asin((3-cx)/R)*180/pi))
        # TODO: upper and lower edge, and hitting two edges

    print(turnings)

    if turnings == []:
        return False

    # On the line of circle centers which goes parallel to the final
    # line, find a point which is further away than R from one of the
    # centers in 'turnings'.

    # Assume ang1 = 0 now.

    # One line of circle centers is x = x1-R.

    sp = 30

    ymid = (12.0+19.7)/2
    cx2 = x1-R

    if cy > ymid:
        cy2 = (2*12.0+ymid)/3
    else:
        cy2 = (2*19.7+ymid)/3

    for (dir, cx, cy, minang, maxang) in turnings:

        # We happen to pick the last values of cx,cy from above.
        # If we do what we should do, pick the local cx,cy here, the
        # paths may not be possible. The possible cy2 circles depend
        # on dir. Therefore we set cy2_1 here and not cy2.
        if cy > ymid:
            cy2_1 = (2*12.0+ymid)/3
        else:
            cy2_1 = (2*19.7+ymid)/3

        # forw2 is the final direction when arriving at (x1, y1)
        if cy2 > y1:
            forw2 = -1
        else:
            forw2 = 1

        # Currently, we can always turn so that we are in the correct
        # direction from the start.
        forw1 = forw2

        print("cx2,cy2 = %f %f" % (cx2, cy2))

        print("dir = %d forw2 = %d" % (dir, forw2))
        if forw2 == 1:
            pass
        else:
            cang = atan2(cx2-cx, cy2-cy)
            if dir == 1:
                cxmid = (cx+cx2)/2
                cymid = (cy+cy2)/2
                cang2 = acos(R/dist(cxmid, cymid, cx2, cy2))
                print("cang cang2 %f %f" % (cang*180/pi, cang2*180/pi))
                print((cang*180/pi, cang2*180/pi))
                cang += cang2

                ang21 = ang-90
                ang2 = 180/pi*cang
                l = [minang, ang2, ang21, maxang]
                x2 = cx + R*sin(cang)
                y2 = cy + R*cos(cang)
                x3 = cx2 - R*sin(cang)
                y3 = cy2 - R*cos(cang)
            else:
                ang21 = ang+90
                ang2 = 180/pi*cang-90
                l = [minang, ang21, ang2, maxang]
                x2 = cx - R*cos(cang)
                y2 = cy + R*sin(cang)
                x3 = cx2 - R*cos(cang)
                y3 = cy2 + R*sin(cang)

            # We should draw this only after we have decided that this
            # is our path.
            if w:
                # we should determine how far this line can go
                w.addline(x1, y1, x1+20*sin(ang1*pi/180),y1+20*cos(ang1*pi/180),
                          fill="green")

                w.addcircle(cx2, cy2, R, "orange")

                # TODO: we must also consider the twin circle to the right of the final
                # line. use it if x2 > x1
                w.addcircle(x1+R, cy2, R, "orange")

            print("angles %f %f %f %f" % tuple(l))
            if minang != -180 or maxang != 180:
                if not nbetween(l):
                    continue

            print(((x2, y2), (x3, y3)))
            return [(ppx, ppy, -1),
                    (x2, y2, -1),
                    (x3, y3, -1),
                    (x1, y1, -1)]

    return False

# When we fail, because both circles are partial and we would have to pass
# through the wall, either we can go forward instead and then reverse when
# we are on the final line, or go forward on the final line and change
# directions by a cusp maneuver. In the latter case, going forward
# to the final line and then reversing seems more rational.
