package gui;

import javax.swing.JFrame;

// TODO: Auto-generated Javadoc
/**
 * The Class CarSimulatorView.
 */
@SuppressWarnings("serial")
public class CarSimulatorView extends JFrame implements SimulatorView {

	/** The my drawing surface. */
	private DrawingSurface myDrawingSurface;
	
	/**
	 * Instantiates a new car simulator view.
	 *
	 * @param sim the sim
	 */
	public CarSimulatorView(SimulatorControl sim) {
        setTitle("FRESTA Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myDrawingSurface = new DrawingSurface(sim, 10.0, -10.0, -10.0, 10.0);
        add(myDrawingSurface);
        setSize(800, 800);
        setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see carSimulator.SimulatorView#update(carSimulator.SimulatorControl)
	 */
	public void update(SimulatorControl sim) {
		myDrawingSurface.repaint();
	}
}
