package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JPanel;

// TODO: Auto-generated Javadoc
/**
 * The Class DrawingSurface.
 */
@SuppressWarnings("serial")
class DrawingSurface extends JPanel {

	/** The my simulator. */
	private SimulatorControl mySimulator; 
	
	/** The grid_y2. */
	private double grid_x1, grid_y1, grid_x2, grid_y2; // The corner points of the surface, expressed in meters
	
	/**
	 * Instantiates a new drawing surface.
	 *
	 * @param sim the sim
	 * @param x1 the x1
	 * @param y1 the y1
	 * @param x2 the x2
	 * @param y2 the y2
	 */
	public DrawingSurface(SimulatorControl sim, double x1, double y1, double x2, double y2) {
		mySimulator = sim;
		grid_x1 = x1;
		grid_x2 = x2;
		grid_y1 = y1;
		grid_y2 = y2;
	}
	
	/**
	 * Draw line to scale.
	 *
	 * @param g the g
	 * @param relx1 the relx1
	 * @param rely1 the rely1
	 * @param relx2 the relx2
	 * @param rely2 the rely2
	 * @param cx the cx
	 * @param cy the cy
	 * @param rot the rot
	 */
	public void drawLineToScale(Graphics g, double relx1, double rely1, double relx2, double rely2, double cx, double cy, double rot) {
		// Draws a line to scale, with end points expressed in meters instead of pixels
		// The end points are given relative to a centerpoint (cx, cy) of the object the line is part of, and rotatet rod radians.

		// First translate and rotate the end points. 

		double x1 = cx + relx1 * Math.cos(-rot) - rely1 * Math.sin(-rot);
		double y1 = cy + relx1 * Math.sin(-rot) + rely1 * Math.cos(-rot);
		double x2 = cx + relx2 * Math.cos(-rot) - rely2 * Math.sin(-rot);
		double y2 = cy + relx2 * Math.sin(-rot) + rely2 * Math.cos(-rot);
		
		
		// Then convert meters to pixels.
		
        Dimension size = getSize();
        Insets insets = getInsets();

        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        
        int pixel_x1 = (int) ((grid_x2 - x1)/(grid_x2 - grid_x1) * w);
        int pixel_y1 = (int) ((grid_y2 - y1)/(grid_y2 - grid_y1) * h);
        int pixel_x2 = (int) ((grid_x2 - x2)/(grid_x2 - grid_x1) * w);
        int pixel_y2 = (int) ((grid_y2 - y2)/(grid_y2 - grid_y1) * h);
        
        g.drawLine(pixel_x1, pixel_y1, pixel_x2, pixel_y2);
	}
	
	/**
	 * Draw oval to scale.
	 *
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @param oval_w the oval_w
	 * @param oval_h the oval_h
	 */
	public void drawOvalToScale(Graphics g, double x, double y, double oval_w, double oval_h) {
		// Draws an oval to scale, with end points expressed in meters instead of pixels

	    x -= oval_w/2;
	    y += oval_h/2;

        Dimension size = getSize();
        Insets insets = getInsets();

        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        
        int pixel_x = (int) ((grid_x2 - x)/(grid_x2 - grid_x1) * w);
        int pixel_y = (int) ((grid_y2 - y)/(grid_y2 - grid_y1) * h);
        int pixel_w = (int) (oval_w/(grid_x1 - grid_x2) * w);
        int pixel_h = (int) (oval_h/(grid_y2 - grid_y1) * h);
        
        g.drawOval(pixel_x, pixel_y, pixel_w, pixel_h);
	}
	
	/**
	 * Draw car to scale.
	 *
	 * @param g the g
	 * @param car the car
	 */
	private void drawCarToScale(Graphics g, CarModel car) {
        g.setColor(Color.blue);
        drawLineToScale
	    (g, -car.width/2, -car.length/2, -car.width/2, car.length/2,
	     car.position_x, car.position_y, car.direction);
	drawLineToScale
	    (g, car.width/2, -car.length/2, car.width/2, car.length/2,
	     car.position_x, car.position_y, car.direction);
	drawLineToScale
	    (g, -car.width/2, -car.length/2, car.width/2, -car.length/2,
	     car.position_x, car.position_y, car.direction);
	drawLineToScale
	    (g, -car.width/2, car.length/2, car.width/2, car.length/2,
	     car.position_x, car.position_y, car.direction);
	drawLineToScale
	    (g, -car.width/2, car.length/6, 0, car.length/2,
	     car.position_x, car.position_y, car.direction);
	drawLineToScale
	    (g, car.width/2, car.length/6, 0, car.length/2,
	     car.position_x, car.position_y, car.direction);
	}
	
	/**
	 * Draw wall to scale.
	 *
	 * @param g the g
	 * @param wall the wall
	 */
	private void drawWallToScale(Graphics g, WallModel wall) {
        g.setColor(Color.red);
        drawLineToScale(g, wall.x1, wall.y1, wall.x2, wall.y2,0 ,0 , 0);
	}
	
    /**
     * Do drawing.
     *
     * @param g the g
     */
    private void doDrawing(Graphics g) {
        g.setColor(Color.white);
        
        // Draw a grid with 1 m between lines
        double pos = Math.ceil(grid_x2);
        while (pos < grid_x1) {
        	drawLineToScale(g, pos, grid_y1, pos, grid_y2, 0, 0, 0);
        	pos += 1.0;
        }
        pos = Math.ceil(grid_y1);
        while (pos < grid_y2) {
        	drawLineToScale(g, grid_x1, pos, grid_x2, pos, 0, 0, 0);
        	pos += 1.0;
        }
        
        // Draw the models
        for (SimulatorModel mod : mySimulator.models) {
	    if (mod instanceof CarModel) {
		CarModel car = (CarModel) mod;
		drawCarToScale(g, car);
		for (int i = 0; i < car.oldn; i++) {
		    drawOvalToScale(g, car.oldx[i], car.oldy[i], 0.1, 0.1);
		}
	    }
	    if (mod instanceof WallModel)
		drawWallToScale(g, (WallModel) mod);
	}
        
        // Show the current timeStep
        g.setColor(Color.black);
        g.drawString("Time = " + mySimulator.time/1000, 5, 20);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
}

