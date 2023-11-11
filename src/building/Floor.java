package building;
// ListIterater can be used to look at the contents of the floor queues for 
// debug/display purposes...
import java.util.ListIterator;
import genericqueue.GenericQueue;

// TODO: Auto-generated Javadoc
/**
 * @author Adi Narasimha
 * The Class Floor. This class provides the up/down queues to hold
 * Passengers as they wait for the Elevator.
 */
public class Floor {
	/**  Constant for representing direction. */
	private static final int UP = 1;
	
	/** The Constant DOWN. */
	private static final int DOWN = -1;
	// private Passengers p;

	/**  The queues to represent Passengers going UP or DOWN. */	
	private GenericQueue<Passengers> down;
	
	/** The up. */
	private GenericQueue<Passengers> up;


	/**
	 * Instantiates a new floor.
	 *
	 * @param qSize the q size
	 */
	public Floor(int qSize) {
		down = new GenericQueue<Passengers>(qSize);
		up = new GenericQueue<Passengers>(qSize);
	}
	
	/**
	 * Instantiates a new floor.
	 */
	public Floor() {
		down = new GenericQueue<Passengers>();
		up = new GenericQueue<Passengers>();
	}
	
	// TODO: Write the helper methods needed for this class. 
	// You probably will only be accessing one queue at any
	// given time based upon direction - you could choose to 
	// account for this in your methods.
	
	/**
	 * Adds the.
	 *
	 * @param dir the dir
	 * @param p the passenger that needs to be added to the floor queue
	 */
	// peer-reviewed: Aman
	public void add(int dir, Passengers p) {
		if (dir == UP) {
			up.add(p);
		} else {
			down.add(p);
		}
	}
	
	/**
	 * Poll.
	 *
	 * @param dir the dir
	 * @return the passengers
	 */
	// peer-reviewed: Aman
	//same as removes but returns null if queue is empty
	public Passengers poll(int dir) {
		if(dir == UP) {
			return up.poll();
		} else {
			return down.poll();
		}
	}
	
	/**
	 * Empty.
	 *
	 * @param dir the dir
	 * @return true, if successful
	 */
	// peer-reviewed: Aman
	public boolean empty(int dir) {
		if(dir == UP) {
			return up.isEmpty();
		}
		return down.isEmpty();
	}
	
	/**
	 * Peek.
	 *
	 * @param dir the dir
	 * @return the passengers
	 */
	// peer-reviewed: Aman
	public Passengers peek(int dir) {
		if(dir == UP) {
			return up.peek();
		}
		return down.peek();
	}
	
	/**
	 * Size.
	 *
	 * @param dir the dir
	 * @return the size of the queue in the direction mentioned
	 */
	// peer-reviewed: Aman
	public int size(int dir) {
		if(dir == UP) {
			return up.size();
		}
		return down.size();
	}
	 
	
	/**
	 * Queue string. This method provides visibility into the queue
	 * contents as a string. What exactly you would want to visualize 
	 * is up to you
	 *
	 * @param dir determines which queue to look at
	 * @return the string of queue contents
	 */
	
	
	public String queueString(int dir) {
		String str = "";
		ListIterator<Passengers> list;
		list = (dir == UP) ?up.getListIterator() : down.getListIterator();
		if (list != null) {
			while (list.hasNext()) {
				// choose what you to add to the str here.
				str += list.next().getNumPass();
				if (list.hasNext()) str += ",";
			}
		}
		return str;	
	}
	
	
}
