import re
import time

import urllib
import urllib.parse
import paho.mqtt.client as mosquitto

import nav_tc
from nav_tc import send_to_ground_control

def on_message(mosq, obj, msg):
    p = str(msg.payload)
    while p[-1] == '\n' or p[-1] == '\t':
        p = p[:-1]

    # ignore our own position
    if g.VIN in msg.topic:
        return

    #sys.stdout.write(msg.topic + " " + str(msg.qos) + " " + p + "\n")
# "adc","current_value":"7.7142 7.630128199403445"

    m = re.search('"adc","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % g.VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"adc","current_value":"[0-9.]+ ([0-9.]+)"', p)
            if m:
            #print("battery %s" % m.group(1))
                g.battery = float(m.group(1))
                if g.battery < 20:
                    send_to_ground_control("battery %f" % g.battery)
        return

    # We still read this, but we don't use 'ultra' - we use 'can_ultra'
    m = re.search('"DistPub","current_value":"', p)
    if m:
        m1 = re.search('"vin":"%s"' % g.VIN, p)
        if m1:
            # since the second value can be garbage
            m = re.search('"DistPub","current_value":"[0-9]+ ([0-9]+)"', p)
            if m:
                g.ultra = float(m.group(1))
        return


def mqtt_init():
    url_str = "mqtt://test.mosquitto.org:1883"
    url_str = "mqtt://iot.eclipse.org:1883"
    url = urllib.parse.urlparse(url_str)
    g.mqttc = mosquitto.Mosquitto()
    g.mqttc.on_message = on_message
    g.mqttc.connect(url.hostname, url.port)
    # will match /sics/moped/position/car2, for example
    g.mqttc.subscribe("/sics/moped/+/+", 0)
    g.mqttc.subscribe("/sics/moped/value", 0)

def send_to_mqtt(x, y, ori):
    g.mqttc.publish("/sics/moped/position/%s" % g.VIN,
                    "%f %f %f %f" % (x, y, ori, time.time()))
    pass

def handle_mqtt():
    while True:
        try:
            mqtt_init()

            i = 0
            rc = 0
            while rc == 0:
                rc = g.mqttc.loop(5.0)
                i += 1

            print("mqttc.loop returned %d" % rc)
            if rc == 7 or rc == 1:
                g.mqttc = mosquitto.Mosquitto()
                mqtt_init()
        except Exception as e:
            time.sleep(5000)

def mqttinit():
    g.mqttc = None
