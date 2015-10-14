package plugins;

import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

import com.sun.squawk.VM;

public class SemiAutomaticReverseParking extends PlugInComponent {
	
	// SemiAutomaticReverseParking is a PlugInComponent which takes control of steering during a reverse parking operation.
	// The driver still controls the acceleration. As a side effect, the current state of the function is published to MQTT.

	// TODO: It would be good to be able to read driver set speed; this could be used for limiting the speed to a low value.
	// It could also be used to determine when to finish, i.e. when the driver starts moving forward.
	// TODO: Turn on the hazard lights instead of just a diode. Would be nice to use another component for that!

	
	// Component interface
	public PluginRPort wheelSpeed;     // Input: Integer encoding cm/s
	public PluginPPort steeringAngle;  // Output: Steering angle between -100..100% of maximum
	public PluginPPort parkingState;   // Output: String showing the state
	public PluginPPort light;          // Output: String indicating state of the light
		
	// These are the possible states of the parking maneuver:
/*
	public enum State {
	    STARTING,       // Initial state, assumes car is put in correct starting position by the driver 
	    TURNING_RIGHT,  // Turn wheels right (+100) and wait until the distance TURN_RIGHT_DIST has been moved
	    GOING_STRAIGHT, // Turn wheels straight (0) and wait until the distance STRAIGHT_DIST has been moved
	    TURNING_LEFT,   // Turn the wheels left (-100) and wait until the distance TURN_LEFT_DIST has been moved
	    ALIGNING,       // Turn the wheels straight (0) 
	    FINISHED;       // Done 
	}
*/

    static final int STARTING = 0;       // Initial state, assumes car is put in correct starting position by the driver 
    static final int TURNING_RIGHT = 1;  // Turn wheels right (+100) and wait until the distance TURN_RIGHT_DIST has been moved
    static final int GOING_STRAIGHT = 2; // Turn wheels straight (0) and wait until the distance STRAIGHT_DIST has been moved
    static final int TURNING_LEFT = 3;   // Turn the wheels left (-100) and wait until the distance TURN_LEFT_DIST has been moved
    static final int ALIGNING = 4;       // Turn the wheels straight (0) 
    static final int FINISHED = 5;       // Done 

	// State variables
//	private State state;
	private int state;
	private int distance;  // Distance travelled, in cm

	// Constant parameters
	private static final int TURN_RIGHT_DIST = 20;    // cm
	private static final int STRAIGHT_DIST   = 50;    // cm
	private static final int TURN_LEFT_DIST  = 20;    // cm
	private static final int TIME_STEP       = 100;   // ms
	
	public void init() {
		// Initialize interface ports
		wheelSpeed = new PluginRPort(this, "wheelSpeed"); //, 0);
		steeringAngle = new PluginPPort(this, "steeringAngle");
		parkingState = new PluginPPort(this, "parkingState");
		light = new PluginPPort(this, "light");
		
		// Initialize state variables
//		state = State.STARTING;
		state = STARTING;
		distance = 0;
	}
	
//	private void gotoState(State s) {
	private void gotoState(int s) {
		// Moves to a new state, resetting the distance and writing the new state to the parkingState port
		state = s;
		distance = 0;
		parkingState.write("parkingState|" + state);
	}
	
	public void run() {
		init();
		
		//		VM.println("SemiAutomaticReverseParking is running");
		System.out.println("SemiAutomaticReverseParking is running");
		
		distance = 0;
//		while (state != State.FINISHED) {
		while (state != FINISHED) {
//			double speed = (double)((Integer) wheelSpeed.read()); 									//read() is not implemented in Squawk.PIRTE
			double speed = (double)((Integer) wheelSpeed.readInt());
//			distance += Math.round((speed * TIME_STEP) / 1000.0);
			distance += round(speed * TIME_STEP / 1000.0);											//Math.round() is not included in basic Squawk (however MathUtils.round exists)

			String sss;
			double xx = 8.0;
			sss = "SARP: state = "
			    + Double.toString(xx)
			    ;

//			VM.print("SARP: state = " + state + ", distance = " + distance + " at speed = ");		// VM doesn't seem to be able to cast from double to String, instead casting error: "Internal error: stack sim error on"
//			VM.print(speed); 
//			VM.println();
			
			switch (state) {
			case STARTING:
			    // Initial state, assumes car is put in correct starting position by the driver 
				light.write("1|1");  // TODO: Check that this turns the diode on!
//				gotoState(State.TURNING_RIGHT);
				gotoState(TURNING_RIGHT);
				break;
			case TURNING_RIGHT:
			    // Turn wheels right (+100) and wait until the distance TURN_RIGHT_DIST has been moved
				steeringAngle.write(100);
				if (distance > TURN_RIGHT_DIST)
//					gotoState(State.GOING_STRAIGHT);
					gotoState(GOING_STRAIGHT);
				break;
			case GOING_STRAIGHT:
			    // Turn wheels straight (0) and wait until the distance STRAIGHT_DIST has been moved
				steeringAngle.write(0);
				if (distance > STRAIGHT_DIST)
//					gotoState(State.TURNING_LEFT);
					gotoState(TURNING_LEFT);
				break;
			case TURNING_LEFT:
			    // Turn the wheels left (-100) and wait until the distance TURN_LEFT_DIST has been moved
				steeringAngle.write(-100);
				if (distance > TURN_LEFT_DIST)
//					gotoState(State.ALIGNING);
					gotoState(ALIGNING);
				break;
			case ALIGNING:
			    // Turn the wheels straight (0) 
				steeringAngle.write(-100);
				light.write("1|0");  // TODO: Check that this turns the diode off!
//				gotoState(State.FINISHED);
				gotoState(FINISHED);
			case FINISHED:
			    // Done 
				break;
			}
			try {
	   			 Thread.sleep(TIME_STEP);
			} catch (InterruptedException e) {
				;
			}
		}
	}
	
	private int round(double val) {
		return (int)Math.floor(val + 0.5);
	}
	
	// Constructors etc.
	public SemiAutomaticReverseParking() {}

	public SemiAutomaticReverseParking(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		SemiAutomaticReverseParking instance = new SemiAutomaticReverseParking(args);
		instance.run();
	}
}
