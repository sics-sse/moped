package gui;

import java.util.Queue;

// TODO: Auto-generated Javadoc
/**
 * The Class WallModel.
 */
public class WallModel extends SimulatorModel implements PhysicalObjectModel {
	
	// This represents a wall, which is a passive object in the simulation, only there to check against collisions.
	
	/** The y2. */
	public double x1, y1, x2, y2; 

	/* (non-Javadoc)
	 * @see carSimulator.SimulatorModel#update(carSimulator.SimulatorModel, java.util.Queue, double)
	 */
	void update(SimulatorModel oldModel, Queue<SimulatorModel> allOldModels, double timeStep) {}
	
	/**
	 * Instantiates a new wall model.
	 *
	 * @param a the a
	 * @param b the b
	 * @param c the c
	 * @param d the d
	 */
	public WallModel(double a, double b, double c, double d) {
		x1 = a;
		y1 = b;
		x2 = c;
		y2 = d;
	}

	/* (non-Javadoc)
	 * @see carSimulator.PhysicalObjectModel#collidesWith(carSimulator.PhysicalObjectModel)
	 */
	public boolean collidesWith(PhysicalObjectModel other) {
		return false;
	}
	
}
