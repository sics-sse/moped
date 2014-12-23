
/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * This is a part of the Squawk JVM.
 */
package tests;

import java.io.*;
import javax.microedition.io.*;

/**
 * Some slow code taken from Sun SPOT Demo OLEDDisplay Board.
 *
 * @author arshan
 *
 * NOTES: Why so slow?
 *
 *
 */

public class SlowCode {
    
     
    public static final String PART_ID = "DEMO_OLEDDISPLAY_BOARD_REV_A";
    
    public static int DISPLAY_WIDTH = 96;
    public static int DISPLAY_HEIGHT = 64;
    
    /// this var controls the max size of the spi send/recv cycles
    public static final int MAX_BUFFER_SIZE = 8;
    
    private  byte[] write = new byte[MAX_BUFFER_SIZE];
    private  byte[] read  = new byte[MAX_BUFFER_SIZE];
    
    // delay after each command set to let the display catch up; in MS
    private static final int cmd_delay = 2;
    

    /**
     * states that the mode pin can be in ...
     */
    private static final int
            UNKNOWN_MODE = 0,
            COMMAND_MODE = 1,
            DATA_MODE    = 2;
    
  
    private int state = 0; // 0 - unknown , 1-command , 2-data
    
  
    /*------------------ these are the core interaction methods, it all starts here.------------------- */

    /**
     * temp buffer for commands
     */
    byte[] buffer = new byte[100];
    int command_cnt = 0;
    
    
    public void startCommand() {
        commandMode();
        command_cnt = 0;
    }
    
    public void finalizeCommand() {
        this.send_data(buffer,command_cnt);
        commandDelay(); // wait a little
        dataMode();     // go back to Data mode ( not really necessary ... ? ) but want resting state this way
    }
    
    public void send_command(byte val) {
        assertCommandMode();
        if ( command_cnt >= buffer.length) {
            System.out.println("[OLEDDisplayBoard] ERROR : command buffer overflow");
        }
        buffer[command_cnt++] = val;
    }
    
    public void send_command_d(byte val) { 
        send_command(val);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void send_data(byte val) {
        dataMode();
        sendrecv(val);
    }
    
    // they seem to have changed the way that things are sent
    final static int sndsize = 600; // looks like there is a hardlimit at 600 ..
    byte [] sndbuffer = new byte[sndsize];
    
    public void send_data(byte[] arr, int sz) {
        if ( sz > sndsize ) {
            
            // divvy it up ...
            int current = 0;
            int sends = sz/sndsize;
            int remainder = sz % sndsize;
            
            // do all the partitions
            for ( int y = 0 ; y < sends; y++ ) {
                System.arraycopy(arr,current,sndbuffer,0,sndsize);
                sendrecv(sndbuffer,sndsize);
                current += sndsize;
            }
            
            // send whats left
            if ( remainder > 0) {
                System.arraycopy(arr,current,sndbuffer,0,remainder);
                sendrecv(sndbuffer,remainder);
            }
        } else {
            sendrecv(arr,sz); // just send it
        }
        
    }
    
    public void send_data(short val) {
        send_data((byte)(val>>8));
        send_data((byte)(val & 0xff));
    }
    
    private void sendrecv(byte val) {
        write[0] = val;
        sendrecv(write,1);
    }
    
    private void sendrecv(byte[] arr , int sz) {
     //   getSPI().sendSPICommand(arr, sz, read, 0); // ?$#?!! this falls out of the classes!?
    }
    
    /**
     * Set the Data/Command pin
     */
    private void setModePin(boolean mode) {
    
    }
    
    public void commandMode() {
        if ( state != COMMAND_MODE) {
            setModePin(true);
            state = COMMAND_MODE;
        }
    }

    public void dataMode() {
        if ( state != DATA_MODE ) {
            setModePin(false);
            state = DATA_MODE;
        }
    }
    
    public void assertCommandMode() {
        if ( state != COMMAND_MODE) {
            throw new IllegalStateException("Not in command mode");
        }
    }
    
    public void assertDataMode() {
        if ( state != DATA_MODE) {
            throw new IllegalStateException("Not in command mode");
        }
    }
    
    
    //
    
    // each of these commands is followed by a longer sleep then normal ...
    public void initDisplay() {

    }

    
    
    public void commandDelay() {
        commandDelay(cmd_delay);
    }
    
    public void commandDelay(int delay) {
        try { Thread.sleep(delay); } catch ( Exception e ) {}
    }
    
    
    public void setupBlast() {
        setupBlast(0, 0, DISPLAY_WIDTH,  DISPLAY_HEIGHT);
    }
      
    public void setupBlast(int x, int y, int width, int height) {
        startCommand();
        System.out.println("Setup blast x: "+ x + " y: " + y + " width: " + width + " height: " + height);
        send_command((byte)444);  //Set re-map & data format command
        send_command((byte)0x70);
        
        send_command((byte)444);  //Set column address command
        send_command((byte)x);             //Set column start address
        send_command((byte)(x+width-1));         //Set column end address 
        
        send_command((byte)444);  //Set row address command
        send_command((byte)y);             //Set row start address
        send_command((byte)(y+height-1));        //Set row end address
        
        finalizeCommand();
    }
 
    
    private static int lookup(int val) throws EOFException {
        // 0 - 48
        // a - 65
        // A - 97
        if (val < 0) throw new EOFException();
        if ( val < 65) return val-48; // decimal number
        return (val & 0x1F) + 9;
    }
    
    private static int readHex(InputStream fstream) throws IOException {
         return (lookup(fstream.read()) << 4) | lookup(fstream.read());
    }
    

    public byte[] loadImage(InputStream fstream ) {
        int lineNo = 1;
        
        try {
            int initSz = fstream.available() / 4; // rough estimate. Counting on the fact that resource streams know how big they are... on
            byte[] result = new byte[initSz];
            int thisbyte = 0;
            int realSize = 0;

            while ( fstream.available() > 0 ) {
                while ( thisbyte != ':') {
                    if (fstream.available() <= 0) break;
                    thisbyte = fstream.read();
                }
                
                if (fstream.available() <= 0) break;
                
                int r = readHex(fstream);
                int g = readHex(fstream);
                int b = readHex(fstream);
                
                result[realSize++] = (byte) ((b&0xF1) | (( r >> 6) & 0x3));
                result[realSize++] = (byte) ((((r>>3) & 0x3) << 5 ) | (g >> 3));
                
                lineNo++;
                thisbyte = 0;
            }

            if (realSize < result.length) {
                byte[] realResult = new byte[realSize];
                System.arraycopy(result, 0, realResult, 0, realSize);
                result = realResult;
            }
            return result;
        } catch (IOException e) {
            error("caught exception on line " + lineNo);
            e.printStackTrace();
        }
        return null;
    }
    
 
    
    private void error(String msg) {
        msg("ERROR : " + msg );
    }
    
    private void msg(String msg) {
        System.out.println("[OLEDDisplayBoard] " + msg);
    }
    
  
    public static void main(String[] args) {
        SlowCode sc = new SlowCode();
        
        byte[] buffer = new byte[128 * 1024];
        int size = 0;
        try {
            InputStream fstream = Connector.openInputStream("file://" + "javalogo.txt");
            try {
                size = fstream.read(buffer);
                System.out.println("Read " + size + " bytes from " + "file://" + "javalogo.txt");
            } catch (EOFException ex) {
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        
        for (int i = 0; i < 100; i++) {
            ByteArrayInputStream strm = new ByteArrayInputStream(buffer, 0, size);
            sc.loadImage(strm);
        }
    }
}
