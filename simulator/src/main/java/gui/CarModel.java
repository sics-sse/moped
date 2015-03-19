package gui;

import java.util.Queue;

// TODO: Auto-generated Javadoc
/**
 * The Class CarModel.
 */
public class CarModel extends SimulatorModel implements PhysicalObjectModel {
	public double lastMotorPower = 0.0;   
	boolean isCarAHead = true;
	// Constant parameters of the simulation.
	/** The length. */
	final double length = 0.53; // Total length of the car, in m

	/** The width. */
	final double width = 0.29; // Total width of the car, in m

	/** The wheel base. */
	final double wheelBase = 0.33; // Wheel base of the car, in m

	/** The wheel radius. */
	final double wheelRadius = 0.053; // Wheel radius, in m

	/** The max steering angle. */
	final double maxSteeringAngle = 0.43; // Maximum steering angle, in rad

	/** The weight. */
	final double weight = 3.440; // Weight of the car, in kg

	/** The max motor power. */
	final double maxMotorPower = 50; // Maximum motor power, in W

	/** The drag coefficient. */
	final double dragCoefficient = 0.15; // Multiply by vehicleSpeed^2 to get
											// deceleration from drag, in 1/m
	/** The max acceleration. */
	final double maxAcceleration = 10; // Maximum acceleration, in m/s^2

	// Variables under control of the "driver"
	/** The motor power. */
	public static double motorPower; // Power of the motor, as -1 .. 1 of maxMotorPower

	/** The steering angle. */
	public static double steeringAngle; // Current steering angle, as -1 .. 1 of
	// maxSteeringAngle
	
	/** Lamps */
	public static boolean lamp[]; // True when lamp is on, False otherwise

	// Variables calculated by the simulation.
	/** The vehicle speed. */
	public static double vehicleSpeed; // Speed of the car, in m/s

	/** The acceleration_x. */
	double acceleration_x; // Acceleration in x dimension, in m/s^2

	/** The rear wheel speed. */
	public static double rearWheelSpeed; // Wheel speed of the rear wheel, in 1/s

	/** The drag. */
	double drag; // Drag, in m/s^2

	/** The direction. */
	public static double direction; // Direction of travel, where 0 is straight North, in rad

	/** The position_y. */
	public static double position_x, position_y; // Position on the grid of car centerpoint,
									// in m from centerpoint of grid

	// total running time
	static double runningTime;
	
	// outdoor temperature
	static double outdoorTemperature;

	// allowed speed
	static double allowedSpeed;

	// hazardLightOn
	public static boolean hazardLightOn;
	
	// battery voltage
	public static double voltage;
	
	/** The limited speed. */
	static double limitedSpeed;

    public static double [] oldx;
    public static double [] oldy;
    public static int oldn;

    private static int iteration = 0;

	public CarModel() {
		oldx = new double [100];
		oldy = new double [100];
		oldn = 0;

		lamp = new boolean[3];
		lamp[0] = false;
		lamp[1] = false;
		lamp[2] = false;
		steeringAngle = -50.0;
		motorPower = 0.001;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see carSimulator.PhysicalObjectModel#collidesWith(carSimulator.
	 * PhysicalObjectModel)
	 */
	public boolean collidesWith(PhysicalObjectModel other) {
		/*
		 * // Some bug in this ... Fix after vacation!! // Simply use a circle
		 * around the car. (Maybe later replace with the
		 * "separating axis test"?) if (other instanceof CarModel) { CarModel
		 * car = (CarModel) other; double r1 = Math.sqrt(length*length +
		 * width*width)/2; double r2 = Math.sqrt(car.length*car.length +
		 * car.width*car.width)/2; return (r1 + r2 >
		 * Math.sqrt(Math.pow(position_x - car.position_x, 2) +
		 * Math.pow(position_y - car.position_y, 2))); } else if (other
		 * instanceof WallModel) { WallModel wall = (WallModel) other; return
		 * false; // Change this to check if line intersects with circle.... }
		 * else return false;
		 */
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see carSimulator.SimulatorModel#update(carSimulator.SimulatorModel,
	 * java.util.Queue, double)
	 */
	protected void update(SimulatorModel oldModel,
		Queue<SimulatorModel> allOldModels, double timeStep) {
		CarModel old = (CarModel) oldModel;

		iteration += 1;

		/*
		 * TODO list: Calculate acceleration, gyro in all directions Calculate
		 * collision with walls and cars
		 */

		// Physical equations describing the motion of the car

		//old.vehicleSpeed = 0.1;
		//old.rearWheelSpeed = 0.5;

		drag = dragCoefficient * old.vehicleSpeed * old.vehicleSpeed;

		// Depending on the values of vehicleSpeed, motorPower, we get the
		// following states with different equations for acceleration_x
		// 1. vehicleSpeed = 0, motorPower = 0: Stationary, acceleration_x = 0
		// 2. vehicleSpeed = 0, motorPower > 0: Starting from standstill,
		// acceleration_x = maxAcceleration
		// 3. vehicleSpeed = 0, motorPower < 0: Start reversing from standstill,
		// acceleration_x = - maxAcceleration
		// 4. vehicleSpeed > 0, motorPower = 0: Free rolling, acceleration_x = -
		// drag
		// 5. vehicleSpeed > 0, motorPower > 0: Accelerating forward
		// 6. vehicleSpeed > 0, motorPower < 0: Braking
		// 7. vehicleSpeed < 0, motorPower = 0: Free rolling, acceleration_x = -
		// drag
		// 8. vehicleSpeed < 0, motorPower > 0: Braking
		// 9. vehicleSpeed < 0, motorPower < 0: Accelerating reverse
		// This is captured in the following equations:

		if (old.vehicleSpeed == 0) // Case 1-3
			acceleration_x = Math.signum(motorPower) * maxAcceleration;
		else
			// Case 4-9
			acceleration_x = Math.signum(motorPower)
					* Math.min(maxAcceleration, Math.abs(motorPower
							* maxMotorPower
							/ (old.rearWheelSpeed * wheelRadius * weight)))
					- Math.signum(old.vehicleSpeed) * drag;

		vehicleSpeed = old.vehicleSpeed + acceleration_x * timeStep;
		if (Math.abs(vehicleSpeed) < maxAcceleration * timeStep)
			vehicleSpeed = 0;

		rearWheelSpeed = vehicleSpeed / (2 * Math.PI * wheelRadius);
		
		if(CarModel.position_x<7.5 && CarModel.position_x > -7.5 && CarModel.position_y <7.5 && CarModel.position_y > -7.5) {
			position_y = old.position_y + vehicleSpeed * timeStep
					* Math.cos(direction);
			position_x = old.position_x + vehicleSpeed * timeStep
					* Math.sin(direction);
			
			direction = old.direction + vehicleSpeed * timeStep / wheelBase
					* Math.sin(steeringAngle * maxSteeringAngle);
			lastMotorPower = motorPower;
		} else{
			if(CarModel.motorPower < 0 && this.lastMotorPower > 0) {
				position_y = old.position_y + vehicleSpeed * timeStep
						* Math.cos(direction);
				position_x = old.position_x + vehicleSpeed * timeStep
						* Math.sin(direction);
			} else if(motorPower > 0 && lastMotorPower < 0) {
				position_y = old.position_y + vehicleSpeed * timeStep
						* Math.cos(direction);
				position_x = old.position_x + vehicleSpeed * timeStep
						* Math.sin(direction);
			}
		}
		
		// Check for collisions with other cars, or with walls, based on the
		// allOldModels list and a method "collidesWith" in each model.
		// If collision is detected, stop this car.

		for (SimulatorModel mod : allOldModels) {
			if (mod instanceof PhysicalObjectModel) {
				if (collidesWith((PhysicalObjectModel) mod)) {
					vehicleSpeed = 0;
					acceleration_x = old.vehicleSpeed / timeStep;
				}
			}
		}

		// Calculate new temperature. Temperature oscillates between 1.5 and 2.5 degrees every second
		runningTime += timeStep;
		if (Math.round(runningTime) % 2 == 0)
			outdoorTemperature = 2.5;
		else
			outdoorTemperature = 1.5;

		// Calculate new allowed speed. Limit is 8 m/s for first 5 s, then 5 m/s
		if (runningTime < 5)
			allowedSpeed = 8.0;
		else 
			allowedSpeed = 5.0;
		
		if (iteration % 10 == 0) {
		    if (oldn < 100) {
			oldx[oldn] = position_x;
			oldy[oldn] = position_y;
			oldn++;
		    } else {
			for (int i = 0; i < 100-1; i++) {
			    oldx[i] = oldx[i+1];
			    oldy[i] = oldy[i+1];
			}
			oldx[100-1] = position_x;
			oldy[100-1] = position_y;
		    }
		}

//		System.out.println("speed = " + vehicleSpeed + ", acc = "
//				+ acceleration_x + ", allowedSpeed = " + allowedSpeed + ", temp = " + outdoorTemperature + ", hazard = " + hazardLightOn);
	}

}
