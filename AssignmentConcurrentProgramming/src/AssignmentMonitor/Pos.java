//Implementation of position class 
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2014

//Hans Henrik Løvengreen    Oct 6, 2014

package AssignmentMonitor;

public class Pos {

    public int row;       // Note: public
    public int col;
    Semaphore sem;
    
    public Pos(int i, int j) { row = i;  col = j; sem=new Semaphore(1); }

    public void free(){
    	sem.V();
    }
    
    public void take(){
    	try{ sem.P(); }catch(InterruptedException e){}
    }
    
    public static boolean equal(Pos p1, Pos p2) {
        if (p1 == null || p2 == null) return false;
        return (p1.row == p2.row) && (p1.col == p2.col);
    }

        
    public Pos copy() {
        return new Pos(row, col);
    }

    public boolean equals(Object p) {
    	return (p instanceof Pos && Pos.equal(this,(Pos) p));
    }

    public int hashCode() { 
    	// Borrowed from java.awt.geom.Point2D
    	long bits = java.lang.Double.doubleToLongBits(row);
    	bits ^= java.lang.Double.doubleToLongBits(col) * 31;
    	return (((int) bits) ^ ((int) (bits >> 32)));
    }
    
   public String toString() {
        return "("+row+","+col+")";
    }

}

