def send_to_ground_control(str):
    if not g.ground_control:
        return

    try:
        str1 = str + "\n"
        g.ground_control.send(str1.encode('ascii'))
    except Exception as e:
        print("send1 %s" % e)
        g.ground_control = None
#        connect_to_ground_control()

