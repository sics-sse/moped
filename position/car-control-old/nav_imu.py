import smbus
import time

from math import sqrt, cos, sin, pi, atan2, acos, asin

import nav_log

bus = smbus.SMBus(1)

imuaddress = 0x68

def Write_Sensor(reg, val):
    bus.write_byte_data(imuaddress, reg, val)

def imuinit0():
    smbusinit = False

    for i in range(0, 3):
        try:
            bus.write_byte_data(imuaddress, 0x6b, 0)
            smbusinit = True
        except Exception as e:
            print(e)

    if not smbusinit:
        print("couldn't init IMU")
        exit(0)

imuinit0()

bus.read_byte_data(imuaddress, 0x75)

#bus.write_byte_data(imuaddress, 0x1a, 5)
#bus.write_byte_data(imuaddress, 0x1b, 0)

bus.write_byte_data(imuaddress, 0x1a, 1)
bus.write_byte_data(imuaddress, 0x1b, 16)

MPU9150_SMPLRT_DIV = 0x19 # 25
MPU9150_CONFIG = 0x1a # 26
MPU9150_GYRO_CONFIG = 0x1b
MPU9150_ACCEL_CONFIG = 0x1c
MPU9150_FIFO_EN = 0x23
MPU9150_I2C_MST_CTRL = 0x24
MPU9150_I2C_SLV0_ADDR = 0x25
MPU9150_I2C_SLV0_REG = 0x26
MPU9150_I2C_SLV0_CTRL = 0x27
MPU9150_I2C_SLV1_ADDR = 0x28
MPU9150_I2C_SLV1_REG = 0x29
MPU9150_I2C_SLV1_CTRL = 0x2a
MPU9150_I2C_SLV1_DO = 0x64
MPU9150_I2C_MST_DELAY_CTRL = 0x67 # 103
MPU9150_I2C_SLV4_CTRL = 0x34 # 52
MPU9150_USER_CTRL = 0x6a #106

def sleep(x):
    if True:
        time.sleep(x)

def imuinit():

    bus.write_byte_data(imuaddress, 0x6b, 0x80)
    sleep(0.1)
    bus.write_byte_data(imuaddress, 0x6b, 0)

    b = bus.read_byte_data(imuaddress, 0x49)
    print("read byte %#x" % b)

    sleep(0.1)
    Write_Sensor(MPU9150_I2C_SLV0_ADDR, 0x8C);
    b = bus.read_byte_data(imuaddress, 0x49)
    print("read byte %#x" % b)

    sleep(0.1)
    Write_Sensor(MPU9150_I2C_SLV0_CTRL, 0x88);
    sleep(0.1)
    b = bus.read_byte_data(imuaddress, 0x49)
    print("read byte %#x" % b)

    Write_Sensor(MPU9150_USER_CTRL, 0x20);
    sleep(0.1)

    while True:
        b = bus.read_byte_data(imuaddress, 0x49)
        print("read byte %#x" % b)
        if b == 0x48:
            break

    if True:
        # this did the trick:
        Write_Sensor(MPU9150_CONFIG, 0x02);
        # maybe important:
        Write_Sensor(MPU9150_GYRO_CONFIG, 0x08);
        sleep(0.1)

        Write_Sensor(MPU9150_SMPLRT_DIV, 0x7);
        sleep(0.1)

        Write_Sensor(MPU9150_I2C_SLV1_ADDR, 0x0C);
        sleep(0.1)
        # Set where reading at slave 1 starts
        Write_Sensor(MPU9150_I2C_SLV1_REG, 0x0A);
        sleep(0.1)
        # Enable at set length to 1
        Write_Sensor(MPU9150_I2C_SLV1_CTRL, 0x81);
        sleep(0.1)

        # overvride register
        Write_Sensor(MPU9150_I2C_SLV1_DO, 0x01);
        sleep(0.1)

        # set delay rate
        Write_Sensor(MPU9150_I2C_MST_DELAY_CTRL, 0x03);
        sleep(0.1)
        # set i2c slv4 delay
        Write_Sensor(MPU9150_I2C_SLV4_CTRL, 0x04);
        sleep(0.1)



imuinit()

bus.write_byte_data(imuaddress, MPU9150_CONFIG, 1)
bus.write_byte_data(imuaddress, MPU9150_GYRO_CONFIG, 16)




def make_word(high, low):
    x = high*256+low
    if x >= 32768:
        x -= 65536
    return x

def make_word2(high, low):
    x = high*256+low
    return x

def readgyro():
    while True:
        nav_log.tolog("starting readgyro")
        readgyro0()

def readgyro0():
    #gscale = 32768.0/250
    gscale = 32768.0/1000
    ascale = 1670.0

    x = 0.0
    y = 0.0
    x0 = 0.0
    y0 = 0.0
    z0 = 0.0
    rx = 0.0
    ry = 0.0
    acc = 0.0

    try:

        tlast = time.time()
        t1 = time.time()

        while True:
            w = g.bus.read_i2c_block_data(g.imuaddress, 0x47, 2)
            high = w[0]
            low = w[1]
            r = make_word(high, low)

            r -= g.rbias

            if True:
                high = g.bus.read_byte_data(g.imuaddress, 0x45)
                low = g.bus.read_byte_data(g.imuaddress, 0x46)
                ry = make_word(high, low)
                ry -= g.rybias

                w = g.bus.read_i2c_block_data(g.imuaddress, 0x43, 2)
                high = w[0]
                low = w[1]
                rx = make_word(high, low)
                rx -= g.rxbias

            if False:
                if rx > 120 and g.finspeed != 0 and g.dstatus != 2:
                    inhibitdodrive()
                    g.dstatus = 2
                    cmd = "/home/pi/can-utils/cansend can0 '101#%02x%02x'" % (
                        246, 0)
                    os.system(cmd)
    #                dodrive(0, 0)
                    print("stopped")
                    drive(0)


            # make the steering and the angle go in the same direction
            # now positive is clockwise
            r = -r

            t2 = time.time()
            dt = t2-t1
            t1 = t2

            angvel = r/gscale
            g.dang = angvel*dt
            g.ang += g.dang

            if True:
                w = g.bus.read_i2c_block_data(g.imuaddress, 0x3b, 6)
                x = make_word(w[0], w[1])
                x -= g.xbias
                y = make_word(w[2], w[3])
                y -= g.ybias
                z = make_word(w[4], w[5])
                z -= g.zbias

                x /= ascale
                y /= ascale
                z /= ascale

                acc = sqrt(x*x+y*y+z*z)
                if acc > 9.0 and g.detectcrashes:
                    g.crash = acc

                x0 = -x
                y0 = -y
                z0 = z

                # the signs here assume that x goes to the right and y forward

                x = x0*cos(pi/180*g.ang) - y0*sin(pi/180*g.ang)
                y = x0*sin(pi/180*g.ang) + y0*cos(pi/180*g.ang)

                g.vx += x*dt
                g.vy += y*dt
                g.vz += z*dt

                g.px += g.vx*dt
                g.py += g.vy*dt
                g.pz += g.vz*dt

            corr = 1.0

            vvx = g.inspeed*corr/100.0*sin(pi/180*g.ang)
            vvy = g.inspeed*corr/100.0*cos(pi/180*g.ang)

            ppxi = vvx*dt
            ppyi = vvy*dt

            if True:
                ds = sqrt(ppxi*ppxi+ppyi*ppyi)
                g.totals += ds

            g.ppx += ppxi
            g.ppy += ppyi

            if g.oldpos != None:
                t2_10 = int(t2*10)/10.0
                g.oldpos[t2_10] = (g.ppx, g.ppy, g.ang)

            # don't put too many things in this thread

            if False:
                w = g.bus.read_i2c_block_data(g.imuaddress, 0x4c, 6)
                mx0 = make_word(w[1], w[0])
                my0 = make_word(w[3], w[2])
                mz0 = make_word(w[5], w[4])

                mx = float((mx0-g.mxmin))/(g.mxmax-g.mxmin)*2 - 1
                my = float((my0-g.mymin))/(g.mymax-g.mymin)*2 - 1
                mz = mz0

                quot = (mx+my)/sqrt(2)
                if quot > 1.0:
                    quot = 1.0
                if quot < -1.0:
                    quot = -1.0
                mang = (asin(quot))*180/pi+45
                if mx < my:
                    mang = 270-mang
                    mang = mang%360

                mang = -mang
                mang = mang%360

            if True:
                g.accf.write("%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f\n" % (
                        x, y, g.vx, g.vy, g.px, g.py, x0, y0, vvx, vvy, g.ppx, g.ppy, g.ang,
                        angvel, g.can_steer, g.can_speed, g.inspeed, g.outspeed, g.odometer,
                        z0, r, rx, ry, acc, g.finspeed, g.fodometer, t2-g.t0, g.can_ultra))

            if (t2-tlast > 0.1):
                nav_log.tolog0("")
                tlast = t2

            j = g.angdiff/1000
            g.ang += j
            g.angdiff -= j

            #print("pp diff %f %f" % (g.ppxdiff, g.ppydiff))

            j = g.ppxdiff/100
            g.ppx += j
            g.ppxdiff -= j

            j = g.ppydiff/100
            g.ppy += j
            g.ppydiff -= j

            time.sleep(0.00001)

    except Exception as e:
        nav_log.tolog("exception in readgyro: " + str(e))
        print("exception in readgyro: " + str(e))


def calibrate_imu():
    g.rbias = 0
    g.xbias = 0
    g.ybias = 0
    g.zbias = 0

    # computing angbias would be better
    ncalibrate = 100
    for i in range(0, ncalibrate):
        high = g.bus.read_byte_data(g.imuaddress, 0x47)
        low = g.bus.read_byte_data(g.imuaddress, 0x48)
        r = make_word(high, low)
        g.rbias += r

        if False:
            high = g.bus.read_byte_data(g.imuaddress, 0x43)
            low = g.bus.read_byte_data(g.imuaddress, 0x44)
            r = make_word(high, low)
            g.rxbias += r

            high = g.bus.read_byte_data(g.imuaddress, 0x45)
            low = g.bus.read_byte_data(g.imuaddress, 0x46)
            r = make_word(high, low)
            g.rybias += r

        w = g.bus.read_i2c_block_data(g.imuaddress, 0x3b, 6)
        x = make_word(w[0], w[1])
        y = make_word(w[2], w[3])
        z = make_word(w[4], w[5])
        g.xbias += x
        g.ybias += y
        g.zbias += z

    g.rbias = g.rbias/float(ncalibrate)
    g.rxbias = g.rxbias/float(ncalibrate)
    g.rybias = g.rybias/float(ncalibrate)
    g.xbias = g.xbias/float(ncalibrate)
    g.ybias = g.ybias/float(ncalibrate)
    g.zbias = g.zbias/float(ncalibrate)

    print("rbias = %f, rxbias = %f, rybias = %f, xbias = %f, ybias = %f, zbias = %f" % (g.rbias, g.rxbias, g.rybias, g.xbias, g.ybias, g.zbias))


    g.px = 0.0
    g.py = 0.0
    g.pz = 0.0

    g.ppx = 0.0
    g.ppy = 0.0
    g.ppz = 0.0

    g.vx = 0.0
    g.vy = 0.0
    g.vz = 0.0

# not used
def calmag():
    first = True

    drive(0)
    time.sleep(4)
    steer(-100)
    drive(20)

    t0 = time.time()

    while time.time() < t0 + 20:
        w = g.bus.read_i2c_block_data(g.imuaddress, 0x4c, 6)
        print(w)
        mx = make_word(w[1], w[0])
        my = make_word(w[3], w[2])
        if first:
            g.mxmin = mx
            g.mxmax = mx
            g.mymin = my
            g.mymax = my
            first = False
        if g.mxmin > mx:
            g.mxmin = mx
        if g.mxmax < mx:
            g.mxmax = mx
        if g.mymin > my:
            g.mymin = my
        if g.mymax < my:
            g.mymax = my

    drive(0)
    print((g.mxmin, g.mxmax, g.mymin, g.mymax))
