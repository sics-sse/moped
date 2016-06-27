package main;

import car.Car;
import car.CarFactory;
import gui.CarModel;
import gui.CarSimulatorView;
import gui.SimulatorControl;
import gui.WallModel;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimulatorControl sim = new SimulatorControl((long) 10.0);

		// Add walls
		WallModel wall1 = new WallModel(-7.5, 7.5, 7.5, 7.5);
		WallModel wall2 = new WallModel(7.5, 7.5, 7.5, -7.5);
		WallModel wall3 = new WallModel(7.5, -7.5, -7.5, -7.5);
		WallModel wall4 = new WallModel(-7.5, -7.5, -7.5, 7.5);
		sim.addModel(wall1);
		sim.addModel(wall2);
		sim.addModel(wall3);
		sim.addModel(wall4);

		CarModel car1 = new CarModel();
		sim.addModel(car1);
		
		for (int i = 0; i < args.length; i++) {
		    System.out.println("arg " + i + ": " + args[i]);
		}

		boolean show_window = true;
		if (args.length > 0 && args[0].equals("--nowindow"))
		    show_window = false;

		if (show_window)
		    sim.addView(new CarSimulatorView(sim));
 		sim.start();
 		
 		Car simulatorCar = CarFactory.getCarFactory().generateCar("configs/system1.xml");
 		simulatorCar.init(args);

 		//simulatorCar = CarFactory.getCarFactory().generateCar("configs/system2.xml");
 		//simulatorCar.init();
	}

}
