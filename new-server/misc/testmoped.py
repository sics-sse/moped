
def ack(client):
    while True:
        x = client.service.get_ack_status("20UYA31581L000000", 1)
        if x == True:
            break
