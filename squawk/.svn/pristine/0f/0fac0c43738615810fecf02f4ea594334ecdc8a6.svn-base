This README and source was taken directly from the https://spots-security.dev.java.net/ project.  The source was modified to be able to run on top of Squawk with IMP support, without need for Sun SPOT.

Take the instructions talked about here as informative rather than accurate.

Build and run by
  d
  d rom cldc imp ssl
  squawk org.sunspotworld.demo.StartApplication
  
Module owner: Eric Arseneau, Vipul Gupta
Last Update: April 1, 2009

--------
Overview
--------

This is the SSL library for the Sun SPOT. It is based on the SSL
implementation in Sun's reference implementation of Java ME MIDP 
(the Java ME profile used for cell phones) but includes several
enhancements:

   1. Support for TLS 1.0 (but the rest of this document continues to
      refer to this code as the SSL library)

   2. Support for server-side SSL/TLS

   3. Support for Elliptic Curve Cryptography cipher suites (for now,
      only ECDH-ECDSA-RC4-SHA and secp160r1 are supported).

      (Client-side authentication at the SSL layer is currently 
      not implemented)

SPOT applications built using this library have access to new GCF
connection types:
 
    1. "sradiostream" (this is to radiostream what HTTPS is to HTTP)
        for secure stream oriented communication between two
        SPOTs. This uses ECC ciphers only because running the server
        side on a SPOT using RSA would be much slower.  

    2. "sslsocket" (secure version of "socket") which can be used to
        create a client-side SSL connection to an Internet host (with
        the latter acting as an SSL server). Supports both RSA and ECC
        ciphers.  

    3. "https" (secure version of "http") which can be used to create
        a client-side HTTPS connection to an Internet host (with the
        latter acting as an HTTPS server). Supports both RSA and ECC
        ciphers.

    [NOTE: for an SSL handhshake to succeed, the client side must
     trust the enitity that issued the server's certificate (recall,
     that our implementation currently does not include client
     auth). Each SPOT maintains a store where it keeps certificates of
     all the entities it trusts. This "trusted keystore" can be
     manipulated using the listtrustedkeys, addtrustedkey,
     deletetrustedkey targets in the ant scripts included with the
     SPOT SDK. Details below.]

-----------------------
How to use the SSL code
-----------------------

You will need to rebuild the SPOT library code to include SSL and the
SPOT-side code implementing new crypto related management commands.
You will also need to install new code to extend SpotClient (the host-side
portion of management commands) to match.

Next, we go through the steps necessary to accomplish this. The process
outlined below has been tested on blue-080430. Please make sure you are
using a version of the SDK no earlier than blue-080430.

1. Connect each of your SPOTs via a USB cable and do "ant deletepublickey" 
   to put it in a keyless state.

2. Download the spots-security code from teh SVN repository at java.net:
     % svn checkout https://spots-security.dev.java.net/svn/spots-security/trunk spots-security --username xyz
   Replace xyz with your registered username at java.net.
   This will create a new directory called spots-security with subdirectories
   named CryptoLibrary, SSL, SpotClientCryptoExtensions etc.

3. Modify .sunspot.properties to add these lines (be sure that no 
extraneous line breaks are inserted in the cut-and-paste process). 
The .sunspot.properties file is created in the user's home directory
as part of the SDK installation process. On some operating system
platforms, native file browsing tools may not display filenames that
start with a dot, e.g. on Windows, you may need to use the commandline 
shell to edit this file.

#---------------------------------
# This line lists all of the JAR files that are used to create the SPOT
# library in response to the 'ant library' command.
spot.library.addin.jars=${sunspot.lib}/multihop_common.jar${path.separator}${path.separator}${sunspot.lib}/transducer_device.jar${path.separator}${sunspot.lib}/SSL_device.jar
# The next two lines add new crypto related management commands on the 
# host side. The SPOT-side code for these commands is already in 
# crypto_common.jar
spotclient.addin.jars=${sunspot.lib}/spotclient_crypto.jar${path.separator}${sunspot.lib}/crypto_common.jar
spotclient.addin.classes=com.sun.spot.client.command.crypto.SpotClientCryptoExtension
# This line exposes crypto related management functionality via new 'ant'
# commands. This file is part of the SpotClientCryptoExtensions module.
# NOTE: *** Edit this line depending on where you've placed this file ***
user.import.paths=<fullPathToSpotClientCryptoExtensions>/crypto-extensions.xml
#-----------------------------------

4. Build a new SPOT library that includes crypto code. Depending on where
   you've unpacked the CryptoLibrary and SSL modules, you may need to 
   pass a different argument to the cd command.

     % cd CryptoLibrary
     % ant jar-app     
     // this will create crypto_common.jar in the sunspot.lib directory
     // (typically SDK_HOME/lib)

     % cd SSL
     % ant jar-app     
     // this will create SSL_device.jar in the sunspot.lib directory
     // (typically SDK_HOME/lib) it will include crypto_common.jar

5. Build the host-side library jar.  Depending on where
   you've unpacked the SpotClientCryptoExtensions module, you may need to 
   pass a different argument to the cd command.

     % cd SpotClientCryptoExtensions
     % ant make-host-jar

6. Build a new library (which includes SSL_device.jar and 
   crypto_common.jar), flash it on the two free-range SPOTs 
   included in your SPOT kit.

     % ant library
     % ant resetlibrary

7. In BounceDemo/BounceDemo-OnSPOT/src/org/sunspotworld/demo/utilities/
   RadioDataIOStream.java, modify the line (around line 57)

<         String url = "radiostream://" + addr + ":" + p;

 to

>         String url = "sradiostream://" + addr + ":" + p;

   and do "ant deploy run" to deploy and run the SSL enabled version of 
   bouncedemo on the two SSL enabled SPOTs. The deploy and run commands
   should work both over USB as well as over-the-air but it might 
   be simplest to do so via USB.

   This will force the SPOT to generate its own key pair, acquire the 
   SDK's key and a certificate containing its own public key signed 
   by the SDK's private key.

   "Uncorking" the LEDs will cause an SSL handshake to occur and 
   any data exchanged over the sradiostream connection will be encrypted.

   You can verify this by starting up the PacketSniffer application on a third
   SPOT (this application resides in SDK_HOME/SPOT-Utilities or SDK_HOME/Demos
   /CodeSamples depending on the SDK version). You can even run the 
   PacketSniffer on the "basestation" SPOT by attaching the latter to your host
   via a USB cable and executing 'ant -Dport=<basestationport> deploy run' from
   the PacketSniffer directory (Note: you'll need to update the library on the
   basestation using 'ant resetlibrary' and later, when you need to restore the
   basestation functionality, you'll need to use the 'ant startbasestation' 
   command). Every time the two SPOTs running the modified 
   BounceDemo-onSPOT application exchange the "LED ball", the PacketSniffer 
   will print radio traffic that looks like:

     RP: rssi: 4 dat  radiostream  port: 43 seq: 240 (81 bytes)
         from 3 : 144f01000006ca to 3 : 144f0100000faf
     0000 - 49 00 17 03 01 00 4b fc-79 7d 82 80 06 3b 18 b5   I.....K.y}...;..
     0010 - 28 de 27 28 fe eb b0 93-b4 f1 80 8e 27 65 a7 d8   (.'(........'e..
     0020 - f7 6f a6 37 c3 ce 4f df-2a 35 49 14 87 42 14 17   .o.7..O.*5I..B..
     0030 - b5 fc 14 7d 0c 1e 23 d1-fc f4 7e e9 5c ff 7b 0c   ...}..#...~.\.{.
     0040 - 56 46 4d 86 08 eb d6 85-fd bd 7a db 2f 82         VFM.......z./.  
     RP: rssi: -7 ack  seq: 240
     RP: rssi: 4 dat  radiostream  port: 43 seq: 241 (9 bytes)
         from 3 : 144f01000006ca to 3 : 144f0100000faf
     0000 - 4a 00 94 67 fb eb                                 J..g..          
     RP: rssi: -8 ack  seq: 241
     RP: rssi: -8 dat  radiostream  port: 43 seq: 65 (81 bytes)
         from 3 : 144f0100000faf to 3 : 144f01000006ca
     0000 - 4c 00 17 03 01 00 4c 6e-5c b2 7e 99 b8 5e 45 98   L.....Ln\.~..^E.
     0010 - 1c 5a 04 63 b7 0e d9 6a-53 a7 bc e3 54 3e 56 ec   .Z.c...jS...T>V.
     0020 - 76 50 1f ca 03 6b 56 40-8a c3 5a e5 7e 15 1e ea   vP...kV@..Z.~...
     0030 - 8e 93 eb df 1f ac 44 4d-ce 5b 53 1c 58 86 36 87   ......DM.[S.X.6.
     0040 - 5a 3b 84 2c 97 73 06 aa-4a 3f 69 2d 80 48         Z;.,.s..J?i-.H  
     RP: rssi: 4 ack  seq: 65

   If you were to run the unmodified BounceDemo-onSPOT application on those
   very SPOTs, the PacketSniffer output would show messages in the clear, e.g.

     RP: rssi: -10 dat  radiostream  port: 43 seq: 189 (60 bytes)
         from 3 : 144f01000006ca to 3 : 144f0100000faf
     0000 - 01 00 00 35 74 61 6b 65-42 61 6c 6c 20 31 2e 30   ...5takeBall 1.0
     0010 - 37 36 31 35 31 35 39 34-31 36 33 33 37 32 31 20   761515941633721 
     0020 - 30 2e 31 32 30 32 31 30-38 37 34 31 38 30 33 32   0.12021087418032
     0030 - 33 33 38 20 34 20 31 30-30                        338 4 100       
     RP: rssi: -6 ack  seq: 189
     RP: rssi: -6 dat  radiostream  port: 43 seq: 246 (5 bytes)
         from 3 : 144f0100000faf to 3 : 144f01000006ca
     0000 - 00 01                                             ..              
     RP: rssi: -10 ack  seq: 246
     RP: rssi: -4 dat  radiostream  port: 43 seq: 247 (60 bytes)
         from 3 : 144f0100000faf to 3 : 144f01000006ca
     0000 - 01 00 00 35 74 61 6b 65-42 61 6c 6c 20 31 2e 30   ...5takeBall 1.0
     0010 - 30 33 39 36 33 35 36 39-35 36 32 35 30 37 35 20   039635695625075 
     0020 - 30 2e 31 31 30 37 36 30-32 39 39 34 30 38 31 34   0.11076029940814
     0030 - 38 32 37 20 34 20 31 30-30                        827 4 100       
     RP: rssi: -10 ack  seq: 247

   The BounceDemo-onSPOT application also uses radiogram communication, e.g.
   for broadcasts. Since radiograms aren't secured by SSL, you'll still see 
   these messages in the clear. Hint: look for the string 'PingForColor' in 
   the PacketSniffer output.

8. Next, we look at using the sslsocket and https connections. Sample
   code for these is included in the WebClient application under
   SpotSecurity/SSL/test.

9. The BounceDemo with sradiostream just works because each SPOT has a
   certificate signed by a common SDK that each one of them
   trusts. For a SPOT to establish a secure connection with an
   Internet host, we need to ensure that the entity (often called a
   Certificate Authority or CA) that issued the host's certificate is
   trusted by the SPOT, e.g. the certificate for
   https://login.yahoo.com/ is signed by the Equifax Secure Certificate 
   Authority. So, in order to complete a successful SSL handshake with
   this site, we'll need to add this CAs to the SPOT's trusted key store. 
   The crypto-enhanced version of spotclient implements
   special commands for manipulating the trusted keystore. These are
   illustrated below:

       % cd SSL/test/WebClient

   First, let's see what's already in the trusted key store

       % ant -DremoteId=0014.4F01.0000.020B listtrustedkeys

   produces an output like:

     [java] -----------------------
     [java] Nickname      Subject                 Issuer             Flags
     [java] *MyCert       CN=0014.4F01.0000.020B  CN=SDK-04ddb829    s
     [java] owner         CN=SDK-04ddb829         CN=SDK-04ddb829    o
     [java] -----------------------

   The first two entries correspond to the SPOT's own certificate and that 
   of its 'owner' SDK. These entries were created automatically as part 
   of Step 6. The flag 's' indicates self and 'o' indicates owner.
   (Currently, our implementation is incomplete and does not really make
   use of flags. In the future, these flags will be used to associate
   different privileges with different trusted keys.)

   Next, let's add Equifax to the trusted key store

      % ant -DremoteId=0014.4f01.0000.020b -Dnickname=Equifax -Dcert=Certs/equifaxSecureCA.der -DtrustFlags=w addtrustedkey

   [The w flag is meant to indicate "trust this key for signing certs
    used by secure Web servers running on the Internet"]

   Now, the list of trusted keys looks as follows

       % ant -DremoteId=0014.4F01.0000.020B listtrustedkeys

   produces an output like:

-----------------------
     [java] Nickname      Subject                 Issuer             Flags
     [java] *MyCert       CN=0014.4F01.0000.020B  CN=SDK-04ddb829    s
     [java] owner         CN=SDK-04ddb829         CN=SDK-04ddb829    o
     [java] Equifax       C=US;O=Equifax;OU=Equifax Secure Certificate AuthorityC=US;O=Equifax;OU=Equifax Secure Certificate Authorityw

     [java] -----------------------

 10. Now, deploy the WebClient application on to one of the SSL enabled SPOTs
     by executing the following command inside the WebClient directory.

       % ant deploy

    Start the socket proxy on a host machine in a different terminal window

       % ant socket-proxy-gui (don't forget to click the 'Start' button)

11. Run the WebClient application

       % ant run

12. You should see the SPOT establishing a successful secure connection to 
    an SSL-enabled Internet host -- login.yahoo.com -- since it presents
    a certificate signed by an entity the SPOT trusts. If you delete the 
    entry for Equifax, connection attempt to login.yahoo.com will fail 
    with an error message indicating that the certificate is issued by 
    an unrecognized entity.

       % ant -DremoteId=0014.4F01.0000.020B -Dnickname=Equifax deletetrustedkey
       % ant -DremoteId=0014.4F01.0000.020B listtrustedkeys
-----------------------
     [java] Nickname      Subject                 Issuer             Flags
     [java] *MyCert       CN=0014.4F01.0000.020B  CN=SDK-04ddb829    s
     [java] owner         CN=SDK-04ddb829         CN=SDK-04ddb829    o
     [java] -----------------------

      % ant run 

       .....

       Caught java.io.IOException: Certificate was issued by an unrecognized entity

13. The complete list of commands for manipulating the trusted key store on
    a SPOT is:

    listtrustedkey 
    listtrustedkeys
    addtrustedkey
    deletetrustedkey
    cleartrustedkeys
    genspotkeysncert
    deletespotkeysncert

    [TODO: Create ant help for these commands]

Example 1:

  Add the entity whose certificate is in secp160r1TestCA.der as a
  trusted CA for issuing web site certificates.

  % ant -DremoteId=0014.4F01.0000.020B addtrustedkey -Dcert=Certs/secp160r1TestCA.der -Dnickname=TestCA -DtrustFlags=w

Example 2:

  List all of the keys trusted by a SPOT:

  % ant -DremoteId=0014.4F01.0000.020B listtrustedkeys

produces an output like:

     [java] -----------------------
     [java] Nickname      Subject                 Issuer             Flags
     [java] *MyCert       CN=0014.4F01.0000.020B  CN=SDK-04ddb829    s
     [java] owner         CN=SDK-04ddb829         CN=SDK-04ddb829    o
     [java] TestCA        C=US;ST=CA;L=Mountain View;O=Sun Microsystems, Inc.;OU=Sun Microsystems Laboratories;CN=Test CA (Elliptic curve secp160r1)C=US;ST=CA;L=Mountain View;O=Sun Microsystems, Inc.;OU=Sun Microsystems Laboratories;CN=Test CA (Elliptic curve secp160r1)w

     [java] -----------------------

     The first two entries correspond to the SPOT's own certificate and 
     that of its 'owner' SDK.

Example 3:

  Get details on a specific key

  % ant -DremoteId=0014.4F01.0000.020B -Dnickname=TestCA listtrustedkey

produces an output like:

Querying spot key store for nickname: TestCA
     [java] -----------------------
     [java] [Type: X.509v1
     [java] Serial number: 30:30:3A:46:30:3A:39:35:3A:42:38:3A:39:31:3A:38:44:3A:43:41:3A:42:45:3A:44:32
     [java] Issuer: C=US;ST=CA;L=Mountain View;O=Sun Microsystems, Inc.;OU=Sun Microsystems Laboratories;CN=Tes
t CA (Elliptic curve secp160r1)
     [java] Subject: C=US;ST=CA;L=Mountain View;O=Sun Microsystems, Inc.;OU=Sun Microsystems Laboratories;CN=Te
st CA (Elliptic curve secp160r1)
     [java] Valid from 6/20/2007 0:28:59 GMT until 7/29/2011 0:28:59 GMT
     [java] ECPublicKey: (CurveId: secp160r1, W:04efa2705a160d024f01b8369801e39452a71578117e1ab87fb3c964d5ebcf6
654567f195382c7f22c)
     [java] Signature Algorithm: None
     [java] ]
     [java] -----------------------

Example 4:

  Delete a key you no longer wish to trust:

  % ant -DremoteId=0014.4F01.0000.020B -Dnickname=TestCA deletetrustedkey

=========================================================================
