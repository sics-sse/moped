//if[ENABLE_CHANNEL_GUI]
/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.vm;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.ChannelConstants;

/**
 * Special channel for graphics.
 *
 */
public class GUIOutputChannel extends Channel implements FocusListener, KeyListener, MouseListener, MouseMotionListener {

    private static final int VERTICAL_FRAME_MARGIN = 28;
    private static final int HORIZONTAL_FRAME_MARGIN = 8;

    private static final boolean isHeadless = System.getProperty("java.awt.headless", "false").equals("true");
    static {
        if (isHeadless) {
            System.out.println("[Running in headless graphics environment]");
        }
    }

    /**
     * JIT-time constant controlling tracing.
     */
    private static final boolean TRACING_ENABLED = ChannelIO.TRACING_ENABLED;

    /**
     * The channel to which events are queued.
     */
    private final GUIInputChannel guiInputChannel;

    /**
     * The table of font metrics created for this channel.
     */
    private final SerializableIntHashtable fonts = new SerializableIntHashtable();

    /**
     * The table of images.
     */
    private final SerializableIntHashtable images = new SerializableIntHashtable();

    /**
     * The name of the applet class.
     */
    private String mainClassName = "?";

    /**
     * Image enumberation.
     */
    private int nextImageNumber = 0;

    /**
     * The frame implementing the display for this graphics instance.
     */
    private transient Frame frame;

    /**
     * The width and height of the frame.
     */
    private Dimension frameSize = new Dimension(300, 300);

    /**
     * The position of the frame.
     */
    private Point frameLocation;

    /**
     * The panel implementing the display for this graphics instance.
     */
    private transient Panel panel;

    /**
     * The object used to render to the on-screen panel.
     */
    private transient Graphics display;

    /**
     * The off-screen buffer.
     */
    private transient Image offScreenBuffer;

    /**
     * The object used to render to the off-screen buffer.
     */
    private transient Graphics offScreenDisplay;

    /**
     * Tracks the images.
     */
    private transient MediaTracker mediaTracker;

    /**
     * Flags whether or not graphics operations are to be applied to the off-screen buffer.
     */
    private boolean offScreen = false;

    /**
     * A hint as to where the next new frame should be placed.
     */
    private static Point nextFrameLocation = new Point(10, 10);

    /**
     * Creates a display for a channel.
     *
     * @param cio ChannelIO
     * @param index int
     * @param guiInputChannel GUIInputChannel
     */
    public GUIOutputChannel(ChannelIO cio, int index, GUIInputChannel guiInputChannel) {
        super(cio, index);
        this.guiInputChannel = guiInputChannel;
    }


    /**
     * Initializes all the transient objects that implement the graphics state of this channel.
     */
    private void initializeGraphics() {
        if (TRACING_ENABLED) ChannelIO.trace("setupGraphics " + frameSize);
        frame = new Frame(mainClassName);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (TRACING_ENABLED)ChannelIO.trace("bye...");
                addEvent(ChannelConstants.GUIIN_EXIT, 0, 0, 0);
            }
        });

        panel = new Panel() {
            public void paint(Graphics g) {
                addEvent(ChannelConstants.GUIIN_REPAINT, 0, 0, 0);
            }
        };

        if (frameLocation == null) {
            frameLocation = new Point(nextFrameLocation);
        }

        panel.addKeyListener(this);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        frame.addFocusListener(this);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                frameLocation = frame.getLocation();
            }
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getSize();
                frameSize = new Dimension(size.width - HORIZONTAL_FRAME_MARGIN, size.height - VERTICAL_FRAME_MARGIN);
            }
        });

        frame.setLocation(frameLocation);
        frame.setSize(frameSize.width + HORIZONTAL_FRAME_MARGIN, frameSize.height + VERTICAL_FRAME_MARGIN);
        frame.add(panel);
        frame.setVisible(true);
        display = panel.getGraphics();
        mediaTracker = new MediaTracker(panel);

        // Set up the next frame location
        int nextFrameX = (frame.getX() + frame.getWidth() + HORIZONTAL_FRAME_MARGIN);
        int nextFrameY = frame.getY();
        if (nextFrameX >= 1024) {
            nextFrameX %= nextFrameX;

            nextFrameY += frame.getHeight() + VERTICAL_FRAME_MARGIN;
            if (nextFrameY >= 768) {
                nextFrameY %= 768;
            }
        }
        nextFrameLocation = new Point(nextFrameX, nextFrameY);
    }


    /**
     * Gets the Graphics object that is used to draw to the screen. This will
     * (re)initialize the transient graphics state for this channel if necessary.
     *
     * @return the Graphics object that is used to draw to the screen
     */
    private Graphics getGraphics() {
        if (isHeadless) {
            throw new RuntimeException("cannot get a Graphics object in a headless graphics environment");
        }
        if (display == null) {
            initializeGraphics();
        }
        // MIDP apps are double buffered, regular kawt apps are not
        if (offScreen && offScreenBuffer == null) {
            panel.setBackground(Color.black);
            offScreenBuffer = panel.createImage(frame.getWidth(), frame.getHeight());
            offScreenDisplay = offScreenBuffer.getGraphics();
            offScreenDisplay.setColor(Color.blue);
            offScreenDisplay.fillRect(0, 0, frame.getWidth(), frame.getHeight());

        }

        if (offScreen) {
            return offScreenDisplay;
        } else {
            return display;
        }
    }

    /**
     * Gets the MediaTracker object holding the media objects used by this channel.
     *
     * @return the MediaTracker object holding the media objects used by this channel
     */
    private MediaTracker getMediaTracker() {
        if (mediaTracker == null) {
            getGraphics();
        }
        return mediaTracker;
    }

    /**
     * Gets the FontMetrics object corresponding to a given ID.
     *
     * @param fontID   the identifier of the font
     * @return the FontMetrics object corresponding to <code>fontID</code>
     * @throws RuntimeException if the specified font was not previously created
     */
    private FontMetrics getFontMetrics(int fontID) {
        try {
            return ((FontMetricsHandle)fonts.get(fontID)).getFontMetrics();
        } catch (NullPointerException e) {
            throw new RuntimeException("attempt to retrieve non-existing font: " + fontID);
        }
    }

    /**
     * Updates the screen.
     */
    void flushScreen() {
        if (offScreen && display != null && offScreenBuffer != null) {
            display.drawImage(offScreenBuffer, 0, 0, panel);
            if (TRACING_ENABLED) ChannelIO.trace("**flushScreen**");
        }
    }


    /**
     * {@inheritDoc}
     */
    public void close() {

        if (frame != null) {
            Frame temp = frame;
            temp.dispose();
            frame = null;
            panel = null;
            display = null;
            offScreenBuffer = null;
            offScreenDisplay = null;
            mediaTracker = null;
        }
    }

   /*
    * execute
    */
    int execute(
                 int op,
                 int i1,
                 int i2,
                 int i3,
                 int i4,
                 int i5,
                 int i6,
                 Object o1,
                 Object o2
              ) {
        try {

            switch (op) {
                case ChannelConstants.SETWINDOWNAME: {
                    String s = (String)o1;
                    mainClassName = s;
                    break;
                }
                case ChannelConstants.SCREENWIDTH: {
                    result = frameSize.width;
                    break;
                }
                case ChannelConstants.SCREENHEIGHT: {
                    result = frameSize.height;
                    break;
                }
                case ChannelConstants.BEEP: {                                 // in awtcore.impl.squawk.ToolkitImpl
                    Toolkit.getDefaultToolkit().beep();
                    break;
                }
                case ChannelConstants.SETOFFSCREENMODE: {                     // in awtcore.impl.squawk.ToolkitImpl
                    if (TRACING_ENABLED) ChannelIO.trace("setOffScreenMode");
                    offScreen = true;
                    break;
                }
                case ChannelConstants.FLUSHSCREEN: {                          // in awtcore.impl.squawk.ToolkitImpl
                    if (TRACING_ENABLED) ChannelIO.trace("setOnScreen");
                    flushScreen();
                    break;
                }
                case ChannelConstants.CREATEIMAGE: {                          // in awtcore.impl.squawk.ImageImpl
                    byte[] buf = (byte[])o1;
                    MemoryImageHandle imageHandle = new MemoryImageHandle(buf);
                    result = nextImageNumber++;
                    images.put((int)result, imageHandle);
                    if (TRACING_ENABLED) ChannelIO.trace("createImage "+result);
                    break;
                }
                case ChannelConstants.CREATEMEMORYIMAGE: {                    // in awtcore.impl.squawk.ImageImpl
                    int hs     =        i1;
                    int vs     =        i2;
                    int rgbLength =     i3;
                    int stride =        i4;
                    RGBImageHandle rgbImageHandle = new RGBImageHandle(hs, vs, rgbLength, stride);
                    result = nextImageNumber++;
                    images.put((int)result, rgbImageHandle);
                    if (TRACING_ENABLED) ChannelIO.trace("createMemoryImage "+result);
                    break;
                }
                case ChannelConstants.GETIMAGE: {                             // in awtcore.impl.squawk.ImageImpl
                    String s = (String)o1;
                    FileImageHandle fileImageHandle = new FileImageHandle(s);
                    result = nextImageNumber++;
                    images.put((int)result, fileImageHandle);
                    if (TRACING_ENABLED) ChannelIO.trace("getImage "+result+" "+s);
                    break;
                }
                case ChannelConstants.IMAGEWIDTH: {                           // in awtcore.impl.squawk.ImageImpl
                    if (TRACING_ENABLED) ChannelIO.trace("imageWidth");
                    int index = i1;
                    ImageHandle imageHandle = (ImageHandle)images.get(index);
                    Image image = imageHandle.getImage(getMediaTracker());
                    result = image.getWidth(null);
                    break;
                }
                case ChannelConstants.IMAGEHEIGHT: {                          // in awtcore.impl.squawk.ImageImpl
                    if (TRACING_ENABLED) ChannelIO.trace("imageHeight");
                    int index = i1;
                    ImageHandle imageHandle = (ImageHandle)images.get(index);
                    Image image = imageHandle.getImage(getMediaTracker());
                    result = image.getHeight(null);
                    break;
                }
                case ChannelConstants.DRAWIMAGE: {                            // in awtcore.impl.squawk.ImageImpl
                    int index = i1;
                    int     x = i2;
                    int     y = i3;
                    if (TRACING_ENABLED) ChannelIO.trace("drawImage0 "+index+" at "+x+":"+y );
                    if (!isHeadless) {
                        ImageHandle imageHandle = (ImageHandle)images.get(index);
                        Image image = imageHandle.getImage(getMediaTracker());
                        getGraphics().drawImage(image, x, y, null);
                    }
                    break;
                }
                case ChannelConstants.FLUSHIMAGE: {                           // in awtcore.impl.squawk.ImageImpl
                    int   index =        i1;
                    int[] rgb   = (int[])o1;
                    if (TRACING_ENABLED) ChannelIO.trace("flush0 "+index+" "+rgb);
                    if (!isHeadless) {
                        RGBImageHandle rgbImageHandle = (RGBImageHandle)images.get(index);
                        rgbImageHandle.getImage(getMediaTracker());
                        rgbImageHandle.flush(rgb);
                    }
                    break;
                }
                case ChannelConstants.CREATEFONTMETRICS: {                    // in awtcore.impl.squawk.FontMetricsImpl
                    int size   = i1;
                    boolean isBold = (i2 != 0);
                    int fontID = (size << 16) + (isBold ? 1 : 0);
                    if (!fonts.containsKey(fontID)) {
                        FontMetricsHandle handle = new FontMetricsHandle(isBold, size);
                        fonts.put(fontID, handle);
                    }
                    if (TRACING_ENABLED) ChannelIO.trace("createFontMetrics "+fontID);
                    result = fontID;
                    break;
                }
                case ChannelConstants.FONTSTRINGWIDTH: {                      // in awtcore.impl.squawk.FontMetricsImpl
                    int fontID = i1;
                    String s     = (String)o1;
                    result = getFontMetrics(fontID).stringWidth(s);
                    if (TRACING_ENABLED) ChannelIO.trace("fontStringWidth "+fontID+ ":"+s+" = "+result);
                    break;
                }
                case ChannelConstants.FONTGETHEIGHT: {                        // in awtcore.impl.squawk.FontMetricsImpl
                    int fontID = i1;
                    result = getFontMetrics(fontID).getHeight();
                    if (TRACING_ENABLED) ChannelIO.trace("fontGetHeight "+fontID+" = "+result);
                    break;
                }
                case ChannelConstants.FONTGETASCENT: {                        // in awtcore.impl.squawk.FontMetricsImpl
                    int fontID = i1;
                    result = getFontMetrics(fontID).getAscent();
                    if (TRACING_ENABLED) ChannelIO.trace("fontGetHeight "+fontID+" = "+result);
                    break;
                }
                case ChannelConstants.FONTGETDESCENT: {                       // in awtcore.impl.squawk.FontMetricsImpl
                    int fontID = i1;
                    result = getFontMetrics(fontID).getDescent();
                    if (TRACING_ENABLED) ChannelIO.trace("fontGetHeight "+fontID+" = "+result);
                    break;
                }

                case ChannelConstants.SETFONT: {                              // awtcore.impl.squawk.GraphicsImpl
                    int fontID = i1;
                    FontMetrics metrics = getFontMetrics(fontID);
                    if (TRACING_ENABLED) ChannelIO.trace("setFont0 "+metrics.getFont());
                    if (!isHeadless) {
                        getGraphics().setFont(metrics.getFont());
                    }
                    break;
                }
                case ChannelConstants.SETCOLOR: {                             // awtcore.impl.squawk.GraphicsImpl
                    int c  = i1;
                    if (TRACING_ENABLED) ChannelIO.trace("setColor0 "+c);
                    if (!isHeadless) {
                        getGraphics().setColor(new Color(c));
                    }
                    break;
                }
                case ChannelConstants.SETCLIP: {                              // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    if (TRACING_ENABLED) ChannelIO.trace("setClip0 "+x+":"+y+":"+w+":"+h);
                    if (!isHeadless) {
                        getGraphics().setClip(x, y, w, h);
                    }
                    break;
                }

                case ChannelConstants.DRAWSTRING: {                            // awtcore.impl.squawk.GraphicsImpl
                    String s = (String)o1;
                    int x    =         i1;
                    int y    =         i2;
                    if (TRACING_ENABLED) ChannelIO.trace("drawString0 \""+s+"\" "+x+":"+y);
                    if (!isHeadless) {
                        getGraphics().drawString(s, x, y);
                    }
                    break;
                }

                case ChannelConstants.DRAWLINE: {                             // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    if (TRACING_ENABLED) ChannelIO.trace("drawLine0 "+x+":"+y+":"+w+":"+h);
                    if (!isHeadless) {
                        getGraphics().drawLine(x, y, w, h);
                    }
                    break;
                }
                case ChannelConstants.DRAWOVAL: {                             // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    if (TRACING_ENABLED) ChannelIO.trace("drawOval0 "+x+":"+y+":"+w+":"+h);
                    if (!isHeadless) {
                        getGraphics().drawOval(x, y, w, h);
                    }
                    break;
                }

                case ChannelConstants.DRAWRECT: {                             // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    if (TRACING_ENABLED) ChannelIO.trace("drawRect0 "+x+":"+y+":"+w+":"+h);
                    if (!isHeadless) {
                        getGraphics().drawRect(x, y, w, h);
                    }
                    break;
                }
                case ChannelConstants.FILLRECT: {                             // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    if (TRACING_ENABLED) ChannelIO.trace("fillRect0 "+x+":"+y+":"+w+":"+h);
                    if (!isHeadless) {
                        getGraphics().fillRect(x, y, w, h);
                    }
                    break;
                }
                case ChannelConstants.DRAWROUNDRECT: {                        // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    int aw = i5;
                    int ah = i6;
                    if (TRACING_ENABLED) ChannelIO.trace("drawRoundRect0 "+x+":"+y+":"+w+":"+h+":"+aw+":"+ah);
                    if (!isHeadless) {
                        getGraphics().drawRoundRect(x, y, w, h, aw, ah);
                    }
                    break;
                }
                case ChannelConstants.FILLROUNDRECT: {                        // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    int aw = i5;
                    int ah = i6;
                    if (TRACING_ENABLED) ChannelIO.trace("fillRoundRect0 "+x+":"+y+":"+w+":"+h+":"+aw+":"+ah);
                    if (!isHeadless) {
                        getGraphics().fillRoundRect(x, y, w, h, aw, ah);
                    }
                    break;
                }
                case ChannelConstants.FILLARC: {                              // awtcore.impl.squawk.GraphicsImpl
                    int x  = i1;
                    int y  = i2;
                    int w  = i3;
                    int h  = i4;
                    int ba = i5;
                    int ea = i6;
                    if (TRACING_ENABLED) ChannelIO.trace("fillArc0 "+x+":"+y+":"+w+":"+h+":"+ba+":"+ea);
                    if (!isHeadless) {
                        getGraphics().fillArc(x, y, w, h, ba, ea);
                    }
                    break;
                }
                case ChannelConstants.FILLPOLYGON: {                          // awtcore.impl.squawk.GraphicsImpl
                    int[] comb  = (int[])o1;
                    int   count =        i1;
                    int[] x     = new int[comb.length/2];
                    int[] y     = new int[comb.length/2];
                    for (int i = 0 ; i < x.length ; i++) {
                        x[i] = comb[i];
                    }
                    for (int i = 0 ; i < y.length ; i++) {
                        y[i] = comb[x.length+i];
                    }
                    if (TRACING_ENABLED) ChannelIO.trace("fillPolygon0 "+count);
                    if (!isHeadless) {
                        getGraphics().fillPolygon(x, y, count);
                    }
                    break;
                }
                case ChannelConstants.REPAINT: {                              // awtcore.impl.squawk.GraphicsImpl
                    if (TRACING_ENABLED) ChannelIO.trace("repaint0");
                    if (!isHeadless) {
                        panel.repaint();
                    }
                    break;
                }

                default: throw new RuntimeException("Illegal channel operation "+op);
            }
        } catch (VirtualMachineError ie) {
            // If this is the message that occurs on unix when the DISPLAY variable is
            // not set correctly, it's useful to see that now
            String message = ie.getMessage();
            if (message.indexOf("X11") != -1 || message.indexOf("DISPLAY") != -1) {
                ie.printStackTrace();
            }
            throw ie;
        }
        return 0;
    }

    /*-----------------------------------------------------------------------*\
     *                             FocusListener                             *
    \*-----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void focusGained(FocusEvent e) {
        panel.requestFocus();
        flushScreen();
    }

    /*
     * focusLost
     */
    public void focusLost(FocusEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    void addEvent(int key1_high, int key1_low, int key2_high, int key2_low) {
        guiInputChannel.addToGUIInputQueue(key1_high, key1_low, key2_high, key2_low);
    }

    /*-----------------------------------------------------------------------*\
     *                              KeyListener                              *
    \*-----------------------------------------------------------------------*/

    private void traceKeyEvent(String action, KeyEvent e) {
        ChannelIO.trace(action + " " + e.getKeyCode() + ":" + e.getKeyChar());
    }

    /**
     * {@inheritDoc}
     */
    public void keyPressed(KeyEvent e) {
        if (TRACING_ENABLED) traceKeyEvent("keyPressed", e);
        if (e.getKeyCode() >= 32 /*|| e.getKeyCode() == 0xA*/) {
            addEvent(ChannelConstants.GUIIN_KEY, e.getID(), e.getKeyCode(), e.getKeyChar());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void keyTyped(KeyEvent e) {
        if (TRACING_ENABLED) traceKeyEvent("keyTyped", e);
        if (e.getKeyChar() >= 32) {
            addEvent(ChannelConstants.GUIIN_KEY, e.getID(), e.getKeyCode(), e.getKeyChar());
        } else {
            addEvent(ChannelConstants.GUIIN_KEY, 401, e.getKeyChar(), e.getKeyChar());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void keyReleased(KeyEvent e) {
        if (TRACING_ENABLED) traceKeyEvent("keyReleased", e);
        addEvent(ChannelConstants.GUIIN_KEY, e.getID(), e.getKeyCode(), e.getKeyChar());
    }

    /*-----------------------------------------------------------------------*\
     *                             MouseListener                             *
    \*-----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void mousePressed (MouseEvent e) {
        if (TRACING_ENABLED) ChannelIO.trace("mousePressed "+e);

        // To support single-button Mac users, the combination of left mouse
        // button + CTRL is regarded mouse button 2
        if (e.getButton() == MouseEvent.BUTTON1 && (e.getModifiers() & InputEvent.CTRL_MASK) == 0) {
            addEvent(ChannelConstants.GUIIN_MOUSE, e.getID(), e.getX(), e.getY());
        } else {
            new HibernateDialog(frame, guiInputChannel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased (MouseEvent e) {
        if (TRACING_ENABLED) ChannelIO.trace("mouseReleased "+e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            addEvent(ChannelConstants.GUIIN_MOUSE, e.getID(), e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseClicked (MouseEvent e) {
        if (TRACING_ENABLED) ChannelIO.trace("mouseClicked "+e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            addEvent(ChannelConstants.GUIIN_MOUSE, e.getID(), e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered (MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited (MouseEvent e) {
    }

    /*-----------------------------------------------------------------------*\
     *                          MouseMotionListener                          *
    \*-----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void mouseMoved (MouseEvent e) {
 //       if (TRACING_ENABLED) ChannelIO.trace("mouseMoved "+e);
 //       addEvent(ChannelConstants.GUIIN_MOUSE, e.getID(), e.getX(), e.getY());
    }

    /**
     * {@inheritDoc}
     */
    public void mouseDragged (MouseEvent e) {
        if (TRACING_ENABLED) ChannelIO.trace("mouseDragged "+e);
        addEvent(ChannelConstants.GUIIN_MOUSE, e.getID(), e.getX(), e.getY());
    }
}


/**
 * An ImageHandle is an indirect and serializable handle to a transient Image object.
 */
abstract class ImageHandle implements java.io.Serializable {

    /**
     * Gets the image represented by this handle.
     *
     * @param tracker  the MediaTracker object which may be used to retrieve the Image object
     * @return the image represented by this handle
     */
    public abstract Image getImage(MediaTracker tracker);
}

/**
 * A RGBImageHandle is an indirect and serializable handle to a transient Image object that
 * is defined by some dimensions and an RGB color value.
 */
final class RGBImageHandle extends ImageHandle {

    /**
     * The Image object that is (re)created and cached by this handle.
     */
    private transient Image image;

    /**
     * The parameters of an RGB image.
     */
    private final int hs;
    private final int vs;
    private final int stride;
    private final int[] rgb;

    /**
     * Creates a MemoryImageHandle.
     *
     * @param hs int
     * @param vs int
     * @param rgbLength int
     * @param stride int
     */
    RGBImageHandle(int hs, int vs, int rgbLength, int stride) {
    this.hs = hs;
        this.vs = vs;
        this.stride = stride;
        rgb = new int[rgbLength];
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(MediaTracker tracker) {
        if (image == null) {
            DirectColorModel colormodel = new DirectColorModel(24, 0x0000ff, 0x00ff00, 0xff0000);
            MemoryImageSource imageSource = new MemoryImageSource(hs, vs, colormodel, rgb, 0, stride );
            image = Toolkit.getDefaultToolkit().createImage(imageSource);
        }
        return image;
    }

    /**
     * Flushes an image's data.
     *
     * @param rgb  ???
     */
    void flush(int[] rgb) {
        if (rgb != null) {
            int[] realrgb = this.rgb;
            if (realrgb.length != rgb.length) {
                System.out.println("Bad flushimage rgb buffer length -- realrgb.length = "+realrgb.length+"rgb.length = "+rgb.length);
                System.exit(1);
            }
            System.arraycopy(rgb, 0, realrgb, 0, realrgb.length);
        }
        image.flush();
    }
}

/**
 * A MemoryImageHandle is an indirect and serializable handle to a transient Image object that
 * is defined by an array of bytes.
 */
class MemoryImageHandle extends ImageHandle {

    /**
     * The Image object that is (re)created and cached by this handle.
     */
    private transient Image image;

    /**
     * The data defining the image.
     */
    private final byte[] buf;

    /**
     * Creates a MemoryImageHandle.
     *
     * @param buf  the data defining the image
     */
    public MemoryImageHandle(byte[] buf) {
        this.buf = buf;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(MediaTracker tracker) {
        if (image == null) {
            image = Toolkit.getDefaultToolkit().createImage(buf);
            tracker.addImage(image, 0);
            try {
                tracker.waitForID(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return image;
    }
}

/**
 * A FileImageHandle is an indirect and serializable handle to a transient Image object that
 * is defined by a file.
 */
class FileImageHandle extends ImageHandle {

    /**
     * The Image object that is (re)created and cached by this handle.
     */
    private transient Image image;

    /**
     * The path to the file containing the image.
     */
    private final String name;

    /**
     * Creates a FileImageHandle.
     *
     * @param name String
     */
    public FileImageHandle(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(MediaTracker tracker) {
        if (image == null) {
            image = Toolkit.getDefaultToolkit().getImage(name.replace('/', File.separatorChar));
            tracker.addImage(image, 0);
            try {
                tracker.waitForID(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return image;
    }
}

/**
 * A FontMetricsHandle is an indirect and serializable handle to a transient FontMetrics object.
 */
class FontMetricsHandle implements java.io.Serializable {

    /**
     * Specifies if the underlying font is bold.
     */
    private final boolean isBold;

    /**
     * The size of the underlying font.
     */
    private final int size;

    /**
     * The FontMetrics object that is (re)created and cached by this handle.
     */
    private transient FontMetrics fontMetrics;

    /**
     * Creates a handle to a FontMetrics object.
     *
     * @param bold   true if the font is bold
     * @param size   the size of the font
     */
    FontMetricsHandle(boolean bold, int size) {
        this.isBold = bold;
        this.size = size;
    }

    /**
     * Gets the FontMetrics object, creating it first if necessary.
     *
     * @return FontMetrics
     */
    public FontMetrics getFontMetrics() {
        if (fontMetrics == null) {
            fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(new Font("TimesRoman", isBold ? Font.BOLD : Font.PLAIN, size));
        }
        return fontMetrics;
    }
}


class HibernateDialog extends Dialog {

    GUIInputChannel guiInputChannel;
    final static int HEIGHT=60, WIDTH=100;

    public HibernateDialog(Frame parent, final GUIInputChannel guiInputChannel) {
        super(parent, true);

        Point parentLocation = parent.getLocation();
        int hwidth  = parent.getWidth()/2;
        int hheight = parent.getHeight()/2;
        setLocation((int)parentLocation.getX() + hwidth -(WIDTH/2), (int)parentLocation.getY() + hheight -(HEIGHT/2));

        this.guiInputChannel = guiInputChannel;
        Button y = new Button("Yes");
        Button n = new Button("No");
        setTitle("Hibernate?");
        setLayout(new GridLayout(1,2));
        add(y);
        add(n);
        setSize(new Dimension(WIDTH, HEIGHT));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we){
                dispose();
            }
        });
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                String cmd = ev.getActionCommand();
                if (cmd.equals("Yes")) {
                    //System.out.println("Hibernating...");
                    guiInputChannel.addToGUIInputQueue(ChannelConstants.GUIIN_HIBERNATE, 0, 0, 0);
                }
                dispose();
            }
        };
        y.addActionListener(action);
        n.addActionListener(action);
        setVisible(true);
    }
}
