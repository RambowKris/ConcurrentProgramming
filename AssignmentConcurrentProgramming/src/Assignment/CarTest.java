//Prototype implementation of Car Test class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2014

//Hans Henrik Løvengreen    Oct 6, 2014

package Assignment;

public class CarTest extends Thread {

    CarTestingI cars;
    int testno;

    public CarTest(CarTestingI ct, int no) {
        cars = ct;
        testno = no;
    }

    public void run() {
        try {
            switch (testno) { 
            case 0:
                // Demonstration of startAll/stopAll.
                // Should let the cars go one round (unless very fast)
                cars.startAll();
                cars.stopAll();
                break;

	        case 1: 
	          	// All cars are started. The barrier is turned on, and when all cars are waiting, 
            	// the barrier is turned off, and all cars are released. And then the cars
            	// drives a single round and stop.
            	cars.startAll();
                sleep(1000);
            	cars.barrierOn();
            	sleep(15000);
            	cars.barrierOff();
            	cars.stopAll();
            	break;
            	
	        case 2:
	        	// Test of remove in alley and in queue
	        	cars.startCar(1);
	        	cars.startCar(2);
	        	cars.startCar(5);
	        	cars.startCar(6);
	        	sleep(4000);
	        	cars.removeCar(6);
	        	cars.removeCar(2);
	        	sleep(5000);
	        	cars.restoreCar(2);
	        	cars.restoreCar(6);
	        	sleep(5000);
	        	cars.removeCar(6);
	        	sleep(10000);
	        	cars.stopAll();
	        	sleep(10000);
	        	cars.startAll();
	        	sleep(20000);
	        	cars.stopAll();
	        	break;
	        	
	        case 5:
	        	// Test of remove and restore
	        	cars.startAll();
	        	sleep(1500);
	        	cars.removeCar(2);
	        	cars.removeCar(5);
	        	sleep(3000);
	        	cars.restoreCar(5);
	        	cars.removeCar(4);
	        	sleep(5000);
	        	cars.stopAll();
	        	break;
	        
	        case 19:
                // Demonstration of speed setting.
                // Change speed to double of default values
                cars.println("Doubling speeds");
                for (int i = 1; i < 9; i++) {
                    cars.setSpeed(i,50);
                };
                break;

            default:
                cars.println("Test " + testno + " not available");
            }

            cars.println("Test ended");

        } catch (Exception e) {
            System.err.println("Exception in test: "+e);
        }
    }

}



