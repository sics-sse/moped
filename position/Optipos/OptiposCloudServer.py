'''
Created on 26 mar 2015

@author: Jakob Axelsson

This module implements a server which can receive images from clients. The images are saved on the server,
and are processed using Optipos to get a position, which is sent back to the client.

The code is inspired by:
http://picamera.readthedocs.org/en/release-1.9/recipes1.html#capturing-to-a-network-stream
The web server part uses code from:
http://www.webdeveloper.com/forum/showthread.php?269125-Auto-Refresh-an-Image-only.

The web service protocol of the server is:
GET /index - returns a html page listing the MAC addresses of all devices that have connected, with links to specific information and showing which map they are using
GET /downloadimage?mac=m&view=v - returns a html page showing the latest image from the MAC address m, showing the view no v
POST /processimage?mac=m <image data> - uploads an image from MAC address m, return the position calculated by OptiPos
POST /selectmap?mac=m&map=f - tells the processing algorithm to use the map with filename f for data from MAC address m (assuming file f is already on the server)

STATUS:
Viewing images does not support several views.
Creating the index page from the Connected directory remains.

Installation: 
OpenCV 3.0 can be installed directly as binary for python. See http://stackoverflow.com/questions/26489867/opencv-for-python-3-x-under-windows.
'''

import OptiposLib

import cv2
import numpy as np
import datetime
import os
import shutil
import http.server
import socketserver
import threading
import urllib.parse
import paho.mqtt.client as mqtt
import traceback
import re
import time

# Imports for testing video processing
import socket
import subprocess
import threading


def cleanMACAddress(macAddress: str) -> str:
    """
    This utility function removes all ":" or %3A in a MAC address to avoid encoding problems.
    """
    return macAddress.replace(":", "").replace("%3A", "")


class GenericWebServiceHandler(http.server.SimpleHTTPRequestHandler):

    protocol_version = "HTTP/1.1"

    """
    GenericWebServiceHandler provides generic implementations of do_GET and do_POST methods.
    This methods dispatch all service requests to another method (implemented in a sub class)
    which has the same name as the requested service, but prefixed with get_ or post_.
    To add a new service, only those methods need to be provided in the subclass.
    It works in a similar way as the basic functionality of cherrypy, but much simpler.
    """

    def do_GET(self):
        """
        Generic dispatch function for GET requests. It first tries to call a function implementing the service requested.
        If no such function exists, it instead tries to download a file with the name indicated in the url.
        If no such file exists, it returns a 404 response.
        """
        print("GET") 
        url = urllib.parse.urlparse("http://localhost" + self.path)
        query = urllib.parse.parse_qs(url[4])
        
        responseCode = 200
        result = b""
        try:
            # First try to execute a specific get_ method for the url query
            result = getattr(self, "get_" + url.path[1:])(query)
        except:
            try:
                # If no such method exists, try to do simple file transfer of image files
                print("downloading of image...")
                with open(url.path[1:], "rb") as file:
                    result = file.read()
            except:
                # If the url did not exist as a file, return an error
                responseCode = 404

        self.send_response(responseCode)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-length", len(result))
        self.end_headers()
        self.wfile.write(result)
        return
    
    
    def do_POST(self):
        """
        Generic dispatch function for dealing with POST commands. It is a dispatcher, which sends the parameters on to a method with the same name as the service.
        If no such method exists, it returns an error.
        """
        url = urllib.parse.urlparse("http://localhost" + self.path)
        query = urllib.parse.parse_qs(url[4])
        
        responseCode = 200
        result = b""

        try:
            # First try to execute a specific post_ method for the url query
            content_len = int(self.headers['content-length'])
            post_body = self.rfile.read(content_len)
            result = getattr(self, "post_" + url.path[1:])(query, post_body)
        except Exception as e:
            print(e)
            # If the url did not match any method, return an error
            responseCode = 404
        
        # Send back response to client
        self.send_response(responseCode)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-length", len(result))
        self.end_headers()
        self.wfile.write(result)


class OptiposHTTPHandler(GenericWebServiceHandler):
    
    # The following class variables are assumed. They are set to pass parameters to the instances of the class.
    
    # File paths
    imagePath = ""
    settingsPath = ""
    mapPath = ""

    # Associate each mac address with an Optipos object that is used for processing images
    clientHandler = {}
    
    # Client used for publishing MQTT data 
    mqttClient = None

    
    def get_index(self, query):
        """
        Response to the request "/index".
        This should maybe check that the query is empty.
        """
        content = "Index page TBD"
        return content.encode("utf-8")
    
    
    def get_downloadimage(self, query):
        """
        Response to the request "/downloadimage?mac=m"
        Creates an http file which shows the image from the connected device macAddress, 
        and which is updated regularly with the latest image.
        This should maybe check that the query only contains the key "mac".
        """
        macAddress = cleanMACAddress(query["mac"][0])
        print("downloadimage?mac=" + macAddress)
        updateFrequency = 500
        content = """
                    <!DOCTYPE HTML>
                    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
                    <head>
                    <title>""" + macAddress + """</title>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                    </head>
                    <body>
                    Latest image from """ + macAddress + """<BR>
                    <img src="Connected/""" + macAddress + """.jpg?t=" 
                         onload='setTimeout(function() {src = src.substring(0, (src.lastIndexOf("t=")+2))+(new Date()).getTime()}, """ + str(updateFrequency) + """)' 
                         onerror='setTimeout(function() {src = src.substring(0, (src.lastIndexOf("t=")+2))+(new Date()).getTime()}, """ + str(updateFrequency) + """)' />
                    </body>
                    </html>
                   """
        return content.encode("utf-8")
    
    
    def post_processimage(self, query, post_body):
        """
        Response to the command POST processimage/mac=m with a body containing an image.
        """ 
        macAddress = cleanMACAddress(query["mac"][0])    

        # The image part of the POST body appears to be in lines [4:-2]
        # There is probably a safer way of doing this, such as looking at self.headers['boundary'].
        post_body_lines = post_body.split(b"\r\n")
        image_data = b"\r\n".join(post_body_lines[4:-2])
                                  
        ### Save the same image to a file whose name is just the camera's MAC address, i.e. it will always hold the latest image
        with open("Connected/" + macAddress + ".jpg", "wb") as image_file:
            image_file.write(image_data)
        
        # Also save the received image to a file whose name is constructed from the camera's MAC address and the current date/time
        now = datetime.datetime.now()
        imageFileName = self.imagePath + macAddress + "-" + now.isoformat().replace(":", "") + ".jpg"
        shutil.copy("Connected/" + macAddress + ".jpg", imageFileName)

        print('Image ' + imageFileName + ' received from ' + macAddress)

        # Check that there is a client handler associated, and otherwise create a new one
        mapFileName = "unknown"
        if not (macAddress in self.clientHandler):
            mapFileName = os.path.normpath(self.mapPath + "SSECorridorMap.json")
            settingsFileName = os.path.normpath(self.settingsPath + macAddress + ".json")
            self.clientHandler[macAddress] = OptiposLib.Optipos(settingsFileName, mapFileName)

        result = False
        # Process the image using Optipos
        try:
            # Get the image directly from image_data, instead of from a file.
            buffer = np.frombuffer(image_data, dtype = "uint8")
            image = cv2.imdecode(buffer, 1)
            result = self.clientHandler[macAddress].processImage(image, now)
        except Exception as e:
            print(e)
            print("Error processing image " + imageFileName + " using the map in " + mapFileName + " and settings in " + macAddress + ".json:")
            print(traceback.format_exc())

        if result:
            ((x, y), orientation, markerType, qualityFactor) = result
            resultString = str(x) + " " + str(y) + " " + str(orientation) + " " + str(markerType) + " " + str(qualityFactor)
        else:
            resultString = b"No position"
        print(resultString)
        self.mqttClient.publish("Optipos/" + macAddress, str(resultString))
        return resultString.encode("utf-8")
        
    
    def post_selectmap(self, query, post_body):
        """
        Response to the service POST /selectmap?mac=m&map=f.
        If there is already an Optipos object associated with this mac address, its map file is changed.
        If there is no Optipos object associated yet, a new one is created.
        """
        macAddress = cleanMACAddress(query["mac"][0])    
        mapFileName = query["map"][0]
        mapFileName = os.path.normpath(self.mapPath + mapFileName)
        try:
            self.clientHandler[macAddress].setMarkerMap(mapFileName)
        except:
            settingsFileName = os.path.normpath(self.settingsPath + macAddress + ".json")
            self.clientHandler[macAddress] = OptiposLib.Optipos(settingsFileName, mapFileName)
        print("Map of " + macAddress + " set to " + mapFileName)
        return b"Selected map"
            

def runWebServer():
    httpd = socketserver.ThreadingTCPServer(("", 8080), OptiposHTTPHandler)
    print("Optipos web server running at port", 8080)
    httpd.serve_forever()    


def videoServer():
    """
    This is a function to test sending H.264 video from the RPi to speed up image processing.
    It is based on: http://picamera.readthedocs.org/en/release-1.10/recipes1.html#recording-to-a-network-stream.
    """
    
    def imageProcessing(videoFile):
        """
        This is the body of the thread that does the image processing of the video.
        """
        print("Image processing started")
        cap = cv2.VideoCapture(videoFile)
        while(cap.isOpened()):
            (ret, frame) = cap.read()
            print("Read a frame")
        
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        
            cv2.imshow('frame',gray)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
        cap.release()
        cv2.destroyAllWindows()
    
    # Start a socket listening for connections on 0.0.0.0:8000 (0.0.0.0 means
    # all interfaces)
    server_socket = socket.socket()
    server_socket.bind(('0.0.0.0', 8080))
    server_socket.listen(0)
    
    # Accept a single connection and make a file-like object out of it
    connection = server_socket.accept()[0].makefile('rb')
    try:
        videoFile = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Images/video.mp4"
        with open(videoFile, "wb") as f:
            # Start a thread that reads frames from the video file, and does some processing
            threading.Thread(target=imageProcessing, args=(videoFile,)).start()
            while True:
                # Repeatedly read 1k of data from the connection and write it to
                # the media player's stdin
                data = connection.read(1024)
                if not data:
                    break
                f.write(data)
    finally:
        connection.close()
        server_socket.close()


def watchdisk():
    while True:
        time.sleep(60)

        p = subprocess.Popen("df .", stdout=subprocess.PIPE, shell=True);
        res = p.communicate()

        t = res[0].decode('ascii')

        m = re.search("([0-9]+)%", t)
        if not m:
            print("couldn't parse df output: %s" % t)
            continue

        perc = int(m.group(1))
        # Don't do anything even with old files if there is more than 10%
        # disk left
        if perc < 90:
            continue
            
        # Remove image files older than 200 minutes
        n = os.system("find Images -amin +200 | xargs rm");
        if n != 0:
            print("removing image files returned status %d" % n)

    

def main():
    # Testing of video server 
#    videoServer()
#    exit(0)
    
    socketserver.TCPServer.allow_reuse_address = True
    # Set up paths to the different files used
    OptiposHTTPHandler.imagePath = "Images/"
    OptiposHTTPHandler.settingsPath = "Settings/"
    OptiposHTTPHandler.mapPath = "Maps/"
    
    # Associate each mac address with an Optipos object that is used for processing images
    OptiposHTTPHandler.clientHandler = {}
    
    # Start an MQTT client to publish positions and store it as a class variable of the HTTP handler. 
    # To get positions from all vehicles, subscribe to Optipos/#
    OptiposHTTPHandler.mqttClient = mqtt.Client()
    OptiposHTTPHandler.mqttClient.connect("iot.eclipse.org")
    OptiposHTTPHandler.mqttClient.loop_start()

    threading.Thread(target=watchdisk, args=()).start()

    # Start a web server which returns images from connected devices
    webServer = threading.Thread(target = runWebServer)
    webServer.start()

    
if __name__ == '__main__':
    main()
