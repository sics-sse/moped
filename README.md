moped
=====

Mobile Open Platform for Experimental Development of Cyber-Physical Systems

This project contains source code, developed in a research project on software architecture for Cyber-Physical Systems (CPS), https://moped.sics.se/. It is primarily intended for use in research and education environments. 

The code is intended to operate on a hardware platform based on an RC car, equipped with a distributed control system, consisting of a number of Raspberry Pi (RPi) boards. The goal is to simulate typical behavior of an automotive electronic system, while providing the means for them to be easily upgradable with new (plugin) software and allowing them to interact with each other and other embedded systems. 

It consists of the following packages:
* Autosar OS port to Raspberry Pi hardware, incl. necessary drivers for the MOPED platform
* Plugin server code, which in its turn consists of a plugin manager, used for managing plugin configuration and distribution; and a web portal php-code
* External Communication Manager (ECM) code, running on a Linux node on the MOPED platform and communicating with the external world (for example to receive plugin binaries from the central server)
* Squawk java virtual machine port to Autosar OS and Raspberry Pi hardware
* PlugIn RunTime Environment (PIRTE) code, running on Autosar nodes on the MOPED platform, and providing dynamic runtime properties to the Autosar software (such as installation and uninstallation of new software and communication services for dynamically installed plugins)
* Simulator, intended to simplify plugin development off-board 
* WirelessIno, an Android app for RC car steering using a smart phone
* Plugin repository
* API for the plugin developer
* Some useful tools


To get started:
* Read everything in the "doc" directory.
* Building is done by running server/misc/buildall.sh

Forum for discussion of the MOPED platform:
http://mopedforum.prophpbb.com

Repository:
https://github.com/sics-sse/moped
