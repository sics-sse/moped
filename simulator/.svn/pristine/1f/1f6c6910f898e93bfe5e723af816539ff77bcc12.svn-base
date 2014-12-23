package gui;

import java.util.Queue;
import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulatorControl.
 */
public class SimulatorControl extends Thread {

	// The Simulator class implements a generic discrete simulator framework.
	// Concrete instances are subclasses of Simulator,
	// where the detailed logic can be described in the update() method.

	/** The views. */
	private Queue<SimulatorView> views;

	/** The models. */
	public Queue<SimulatorModel> models;

	/** The autosar model **/
//	public Queue<EmbeddedSystemModel> autosarModels;

	/** The time step. */
	public long timeStep; // The time step of the simulator, in milliseconds

	/** The time. */
	public long time; // The current time since the start of the simulation, in
						// milliseconds

	/**
	 * Instantiates a new simulator control.
	 * 
	 * @param ts
	 *            the ts
	 */
	public SimulatorControl(long ts) {
		views = new LinkedList<SimulatorView>();
		models = new LinkedList<SimulatorModel>();
//		autosarModels = new LinkedList<EmbeddedSystemModel>();
		timeStep = ts;
	}

	/**
	 * Adds the view.
	 * 
	 * @param ui
	 *            the ui
	 */
	public void addView(SimulatorView ui) {
		views.add(ui);
	}

	/**
	 * Adds the model.
	 * 
	 * @param model
	 *            the model
	 */
	public void addModel(SimulatorModel model) {
		models.add(model);
	}

//	public void addModel(EmbeddedSystemModel model) {
//		autosarModels.add(model);
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		// The method run() starts the simulator as a separate process, running
		// a periodic update of the models and views.
		try {
			while (true) {
				long now = System.currentTimeMillis();
				time += timeStep;

				// Update the models, first creating a copy of the old values
				Queue<SimulatorModel> oldModels = new LinkedList<SimulatorModel>();
				for (SimulatorModel mod : models)
					oldModels.add((SimulatorModel) mod.clone());
				for (SimulatorModel mod : models)
					mod.update(mod, oldModels, timeStep / 1000.0);
				// Update the views
				for (SimulatorView ui : views) {
					ui.update(this);
				}
				sleep(Math
						.max(0, timeStep - (System.currentTimeMillis() - now)));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
//	public void init() {
//		for (EmbeddedSystemModel mod : autosarModels){
//			mod.init();
//		}
//	}
}
