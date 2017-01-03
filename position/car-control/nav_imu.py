import smbus
import time

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
