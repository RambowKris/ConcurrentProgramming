package AssignmentMonitor;
//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2014

//Hans Henrik Løvengreen    Oct 6, 2014

import java.awt.Color;

class Gate {

	Semaphore g = new Semaphore(0);
	Semaphore e = new Semaphore(1);
	boolean isopen = false;

	public void pass() throws InterruptedException {
		g.P();
		g.V();
	}

	public void open() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (!isopen) {
			g.V();
			isopen = true;
		}
		e.V();
	}

	public void close() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (isopen) {
			try {
				g.P();
			} catch (InterruptedException e) {
			}
			isopen = false;
		}
		e.V();
	}

}

class Alley {

	int cars = 0;
	boolean trafficUp;

	public void print(int no, String msg) {
		// System.out.println("Car " + no + msg);
		// System.out.println("u: " + u.toString());
		// System.out.println("d: " + d.toString());
		// System.out.println("b: " + b.toString());
	}

	public synchronized void enter(int no) throws InterruptedException {

		print(no, " starts entering");

		if (no < 5) {
			if (trafficUp) {
				cars++;

			} else {
				if (cars == 0) {
					trafficUp = true;
					cars++;
				} else {
					while (!trafficUp && cars > 0) {
							wait();
					}
					trafficUp = true;
					cars++;
				}
			}
		} else {
			if (trafficUp) {
				if (cars == 0) {
					trafficUp = false;
					cars++;
				} else {
					while (trafficUp && cars > 0) {
						wait();
					}
					trafficUp = false;
					cars++;
				}
			} else {
				cars++;
			}
		}
		print(no, " ends entering");
	}

	public synchronized void leave(int no) {

		print(no, " starts leaving");

		cars--;

		if (cars == 0) {
			notifyAll();
		}

		print(no, " ends leaving");

	}

}

class Barrier {

	int cars = 0;
	Semaphore b = new Semaphore(1);
	Semaphore a = new Semaphore(1);
	int threshold = 9;

	boolean barrierOn;
	boolean stop;

	public void print(String msg) {
		// System.out.println(msg);
		// System.out.println("Barrier number1: " + b.toString());
		// System.out.println("Barrier number2: " + c.toString());
	}
	
	// Wait for others to arrive (if barrier active)
	public synchronized void sync() throws InterruptedException {

		cars++;

		print("At barrier");

		if (barrierOn) {
			if (cars < threshold) {
				while (stop||!barrierOn) {
					wait();	
				}
			}else{
				stop=false;
				notifyAll();
			}
		} 

		cars--;
		if(cars==0){
			stop=true;
		}
		print("leaving");
	}

	// Activate barrier
	public synchronized void on() {
		if (!barrierOn) {
			barrierOn = true;
			stop=true;
			print("Barrier on");
		}
	}

	// Deactivate barrier
	public synchronized void off() {
		if (barrierOn) {
			barrierOn = false;
			notifyAll();
			print("Barrier off");
		}
	}

}

class Car extends Thread {

	int basespeed = 100; // Rather: degree of slowness
	int variation = 50; // Percentage of base speed

	CarDisplayI cd; // GUI part

	int no; // Car number
	Pos startpos; // Startpositon (provided by GUI)
	Pos barpos; // Barrierpositon (provided by GUI)
	Color col; // Car color
	Gate mygate; // Gate at startposition
	Alley alley;
	boolean inAlley;
	boolean atBarrier;
	boolean remove;
	Pos AlleyEnter1 = new Pos(0, 0);
	Pos AlleyEnter2 = new Pos(8, 1);
	Pos AlleyEnter3 = new Pos(9, 3);
	Pos AlleyEnd1 = new Pos(1, 1);
	Pos AlleyEnd2 = new Pos(10, 2);

	int speed; // Current car speed
	Pos curpos; // Current position
	Pos newpos; // New position to go to
	CarControl carcon;

	public Car(int no, CarDisplayI cd, Gate g, CarControl carcon) {

		this.no = no;
		this.cd = cd;
		this.carcon = carcon;
		mygate = g;
		startpos = cd.getStartPos(no);
		inAlley = false;
		atBarrier = false;
		barpos = cd.getBarrierPos(no); // For later use

		col = chooseColor();

		// do not change the special settings for car no. 0
		if (no == 0) {
			basespeed = 0;
			variation = 0;
			setPriority(Thread.MAX_PRIORITY);
		}
	}

	public synchronized void setSpeed(int speed) {
		if (no != 0 && speed >= 0) {
			basespeed = speed;
		} else
			cd.println("Illegal speed settings");
	}

	public synchronized void setVariation(int var) {
		if (no != 0 && 0 <= var && var <= 100) {
			variation = var;
		} else
			cd.println("Illegal variation settings");
	}

	synchronized int chooseSpeed() {
		double factor = (1.0D + (Math.random() - 0.5D) * 2 * variation / 100);
		return (int) Math.round(factor * basespeed);
	}

	private int speed() {
		// Slow down if requested
		final int slowfactor = 3;
		return speed * (cd.isSlow(curpos) ? slowfactor : 1);
	}

	Color chooseColor() {
		return Color.blue; // You can get any color, as longs as it's blue
	}

	Pos nextPos(Pos pos) {
		// Get my track from display
		return cd.nextPos(no, pos);
	}

	boolean atGate(Pos pos) {
		return pos.equals(startpos);
	}

	boolean atAlleyEntre(Pos pos) {
		if (pos.equals(AlleyEnter1) || pos.equals(AlleyEnter2)
				|| pos.equals(AlleyEnter3)) {
			return true;
		} else {
			return false;
		}
	}

	boolean inAlley() {
		if (inAlley) {
			return true;
		} else {
			return false;
		}
	}

	boolean atBarrier() {
		if (atBarrier) {
			return true;
		} else {
			return false;
		}
	}

	boolean atAlleyEnd(Pos pos) {
		if (pos.equals(AlleyEnd1) || pos.equals(AlleyEnd2)) {
			return true;
		} else {
			return false;
		}
	}

	public void run() {

		speed = chooseSpeed();
		curpos = startpos;
		cd.mark(curpos, col, no);
		carcon.setPosition(no, curpos);
		alley = carcon.getAlley();

		while (true) {
			try {
				sleep(speed());

				if (atGate(curpos)) {
					mygate.pass();
					speed = chooseSpeed();
				}

				if (barpos.equals(curpos)) {
					atBarrier = true;
					carcon.getBarrier().sync();
					atBarrier = false;
				}
			} catch (InterruptedException e) {
				cd.clear(curpos);
				if(atBarrier){
					carcon.getBarrier().cars--;
				}
				break;
			}

			if (atAlleyEntre(curpos) && !inAlley) {
				try {
					alley.enter(no);
				} catch (InterruptedException e) {
					alley.cars--;
					cd.clear(curpos);
					break;
					// cd.println("Exception in Car no. " + no);
					// System.err.println("Exception in Car no. " + no + ":" +
					// e);
					// e.printStackTrace();
				}

				inAlley = true;
			}

			if (atAlleyEnd(curpos) && inAlley) {
				inAlley = false;
				alley.leave(no);

			}

			// newpos = nextPos(curpos);
			//
			// newpos.take();
			// cd.clear(curpos);
			// cd.mark(curpos, newpos, col, no);
			// try {
			// sleep(speed());
			// } catch (InterruptedException e) {
			// cd.clear(curpos, newpos);
			// break;
			// }
			// cd.clear(curpos, newpos);
			// cd.mark(newpos, col, no);
			//
			// newpos.free();
			//
			// curpos = newpos;

			carcon.seeSem();
			newpos = nextPos(curpos);
			Pos[] position = carcon.getPositions();
			boolean free = true;
			for (int i = 0; i < 9; i++) {
				if (i != no) {
					// Checking the position of the other cars with the new
					// position
					if (position[i].equals(newpos)) {
						free = false;
					}
					// checking the next position of the other cars with the
					// cars next position
					if (newpos.equals(cd.nextPos(i, position[i]))) {
						if (inAlley) {
							if (no < 5) {
								// Make sure the cars in the alley goes first
								if (no > i) {
									free = false;
								}
							} else {
								if (no < i) {
									free = false;
								}
							}
						} else {
							// If the cars are outside the alley the car with
							// highest number goes first
							if (no < i) {
								free = false;
							}
						}
					}
				}
			}
			carcon.freeSem();


			if (free) {
				// Move to new position
				cd.clear(curpos);
				cd.mark(curpos, newpos, col, no);
				try {
					sleep(speed());
				} catch (InterruptedException e) {
					cd.clear(curpos, newpos);
					break;
				}
				cd.clear(curpos, newpos);
				cd.mark(newpos, col, no);
				curpos = newpos;
				carcon.seeSem();
				carcon.setPosition(no, curpos);
				carcon.freeSem();
			} else {
				carcon.freeSem();
				try {
					sleep(speed());
				} catch (InterruptedException e) {
					cd.clear(curpos);
					break;
				}
			}
		}

	}
}

public class CarControl implements CarControlI {

	CarDisplayI cd; // Reference to GUI
	Car[] car; // Cars
	Gate[] gate; // Gates
	Pos[] position; // Car positions
	Alley alley; // Alley
	Barrier bar; // Barrier
	int threshold;
	Semaphore sem = new Semaphore(1);

	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		car = new Car[9];
		gate = new Gate[9];
		position = new Pos[9];
		alley = new Alley();
		bar = new Barrier();
		threshold = 9;

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], this);
			car[no].start();
		}
	}

	public void startCar(int no) {
		gate[no].open();
	}

	public Alley getAlley() {
		return alley;
	}

	public Barrier getBarrier() {
		return bar;
	}

	public void seeSem() {
		try {
			sem.P();
		} catch (InterruptedException e) {
		}
	}

	public void freeSem() {
		sem.V();
	}

	public Pos[] getPositions() {
		return position;
	}

	public void setPosition(int no, Pos pos) {
		position[no] = pos;
	}

	public void stopCar(int no) {
		gate[no].close();
	}

	public void barrierOn() {
		bar.on();
		cd.println("Barrier is on");
	}

	public void barrierOff() {
		bar.off();
		cd.println("Barrier is off");
	}

	public void barrierSet(int k) {
		if (!bar.barrierOn) {
			bar.threshold = k;
		} else {
			if (k > bar.threshold) {
				bar.threshold = k;
			} else {
				bar.threshold = k;
				if (bar.cars >= k) {
					bar.stop=false;
					try{
					bar.notifyAll();
					}catch(Exception e){
						
					}
				}
			}
		}
		cd.println("Barrier threshold is set to " + k);

		// This sleep is for illustrating how blocking affects the GUI
		// Remove when feature is properly implemented.
		// try {
		// Thread.sleep(3000);
		// } catch (InterruptedException e) {
		// }
	}

	public void removeCar(int no) {
		if (car[no].isAlive()) {
			try {

				if (car[no].inAlley()) {
					car[no].interrupt();
					position[no] = cd.getStartPos(no);
					alley.leave(no);
				} else if (car[no].atBarrier()) {
					car[no].interrupt();
				} else {
					car[no].interrupt();
				}

				position[no] = cd.getStartPos(no);
			} catch (Exception e) {
			}
			// cd.println("Remove Car not implemented in this version");
			cd.println("Car " + no + " has been removed.");
		}

	}

	public void restoreCar(int no) {
		if (!car[no].isAlive()) {
			car[no] = new Car(no, cd, gate[no], this);
			car[no].start();
			cd.println("Car " + no + " has been restored.");
		}
		// cd.println("Restore Car not implemented in this version");
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, int speed) {
		car[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) {
		car[no].setVariation(var);
	}

}
