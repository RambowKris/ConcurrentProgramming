//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2014

//Hans Henrik Løvengreen    Oct 6, 2014

package Assignment;

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
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Alley {

    Semaphore u = new Semaphore(4);
    Semaphore d = new Semaphore(2);
    boolean trafficUp;

    public void enter(int no) throws InterruptedException {
    	System.out.println(no+" is entering");
    	System.out.println("u holds: "+u.toString());
    	System.out.println("d holds: "+d.toString());
    	System.out.println("Direction up: "+trafficUp);

    	if(no<5){
    		if(trafficUp){
    			if(Integer.parseInt(u.toString())==4){
    				d.P();
    			}
                try { u.P(); } catch (InterruptedException e) {}    		    			    			    			    			
    		}else{
                try { d.P(); } catch (InterruptedException e) {}    		    			    			    			    			
                try { u.P(); } catch (InterruptedException e) {}    		    			    			    			    			
        		trafficUp=true;    				
    		}
    	}else{
    		if(trafficUp){
                try { d.P(); } catch (InterruptedException e) {}    		    			    			    			    			
                try { d.P(); } catch (InterruptedException e) {}    		    			    			    			    			
                try { u.P(); } catch (InterruptedException e) {}    		    			    			    			    			
        		trafficUp=false;    				
    		}else{
    			if(Integer.parseInt(u.toString())==4){
    				d.P();
    				d.P();
    			}
                try { u.P(); } catch (InterruptedException e) {}    		    			    			
    		}
    	}
    }

    public void leave(int no) throws InterruptedException {
    	System.out.println(no+" is leaving");
    	System.out.println("u holds: "+u.toString());
    	System.out.println("d holds: "+d.toString());
    	System.out.println("Direction up: "+trafficUp);

    	u.V();    		
        
    	if(Integer.parseInt(u.toString())==4){
        	if(no<5){
           		d.V();        			        			
        	}else{
             	d.V();        			        			
               	d.V();        			        			        			
        	}
        }
    }

}

class Barrier {
	
    Semaphore c = new Semaphore(9);
    Semaphore b = new Semaphore(1);
    
    boolean barrelOn;

    // Wait for others to arrive (if barrier active)
	public void sync() { 
        try { c.P(); } catch (InterruptedException e) {}
		if(barrelOn){
	        System.out.println("Barrel semaphore: "+c.toString());
	        if(Integer.parseInt(c.toString())!=0){
	            try { b.P(); } catch (InterruptedException e) {}	     
	        	b.V();
	        }else{
	        	b.V();
	        }
            
		}
    	c.V();
	}  

	// Activate barrier
	public void on() {
        if (!barrelOn) { 
            try { b.P(); } catch (InterruptedException e) {}
            barrelOn = true; 
        }
	}    

	// Deactivate barrier
	public void off() {
        if (barrelOn) { 
        	b.V();
            barrelOn = false; 
        }		
	}    

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition
    Alley alley;
    Pos AlleyEnter1 = new Pos(0,0);
    Pos AlleyEnter2 = new Pos(8,1);
    Pos AlleyEnter3 = new Pos(9,3);
    Pos AlleyEnd1 = new Pos(1,1);
    Pos AlleyEnd2 = new Pos(10,2);

    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to
    CarControl carcon;
    
    public Car(int no, CarDisplayI cd, Gate g, CarControl carcon) {

        this.no = no;
        this.cd = cd;
        this.carcon=carcon;
        mygate = g;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use

        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int)Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }

    boolean atAlleyEntre(Pos pos) {
    	if(pos.equals(AlleyEnter1) || pos.equals(AlleyEnter2) || pos.equals(AlleyEnter3)){
            return true;
    	}else{
    		return false;
    	}
    }

    boolean atAlleyEnd(Pos pos) {
    	if(pos.equals(AlleyEnd1) || pos.equals(AlleyEnd2)){
            return true;
    	}else{
    		return false;
    	}
    }

   public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);
            carcon.setPosition(no,curpos);
            alley=carcon.getAlley();

            while (true) { 
                sleep(speed());
                

                if (atGate(curpos)) { 
                    mygate.pass(); 
                    speed = chooseSpeed();
                }

                if (barpos.equals(curpos)) { 
                	carcon.getBarrier().sync();
                }

                if (atAlleyEntre(curpos)) { 
                    alley.enter(no); 
                }

                if (atAlleyEnd(curpos)) { 
                    alley.leave(no); 
                }

                newpos = nextPos(curpos);
                Pos[] position=carcon.getPositions();
                boolean free=true;
                for(int i=0;i<9;i++){
                	if(i!=no){
                		if(position[i].equals(newpos)){
                			free=false;
                		}else if(nextPos(position[i]).equals(newpos)){
                			if(no<i){
                				free=false;
                			}
                		}
                	}
                }
                
                if(free){
                //  Move to new position 
                cd.clear(curpos);
                cd.mark(curpos,newpos,col,no);
                sleep(speed());
                cd.clear(curpos,newpos);
                cd.mark(newpos,col,no);

                curpos = newpos;
                carcon.setPosition(no, curpos);
                }else{
                    sleep(speed());
                }                	
            }

        } catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

}

public class CarControl implements CarControlI{

    CarDisplayI cd;           // Reference to GUI
    Car[]  car;               // Cars
    Gate[] gate;              // Gates
    Pos[] position;			  // Car positions
    Alley alley;			  // Alley
    Barrier bar;			  // Barrier
    
    
    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car  = new  Car[9];
        gate = new Gate[9];
        position = new Pos[9];
        alley = new Alley();
        bar = new Barrier();

        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            car[no] = new Car(no,cd,gate[no],this);
            car[no].start();
        } 
    }

   public void startCar(int no) {
        gate[no].open();
    }

   public Alley getAlley(){
	   return alley;
   }

   public Barrier getBarrier(){
	   return bar;
   }

   public Pos[] getPositions(){
	   return position;
   }

   public void setPosition(int no, Pos pos){
	   position[no]=pos;
   }

   public void stopCar(int no) {
        gate[no].close();
    }

    public void barrierOn() { 
    	bar.on();
//        cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() { 
    	bar.off();
//        cd.println("Barrier Off not implemented in this version");
    }

    public void barrierSet(int k) { 
        cd.println("Barrier threshold setting not implemented in this version");
         // This sleep is for illustrating how blocking affects the GUI
        // Remove when feature is properly implemented.
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
     }

    public void removeCar(int no) { 
        cd.println("Remove Car not implemented in this version");
    }

    public void restoreCar(int no) { 
        cd.println("Restore Car not implemented in this version");
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}






