package gui;

import java.util.Queue;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulatorModel.
 */
public abstract class SimulatorModel implements Cloneable {
	
	/**
	 * Update.
	 *
	 * @param oldModel the old model
	 * @param allOldModels the all old models
	 * @param timeStep the time step
	 */
	abstract void update(SimulatorModel oldModel, Queue<SimulatorModel> allOldModels, double timeStep);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
