package building;

/**
 * @author Andrew Jacobsen and Adi Narasimha
 * The Class CallManager. This class models all of the calls on each floor,
 * and then provides methods that allow the building to determine what needs
 * to happen (ie, state transitions).
 */
public class CallManager {
	
	/** The floors. */
	private Floor[] floors;
	
	/** The num floors. */
	private final int NUM_FLOORS;
	
	/** The Constant UP. */
	private final static int UP = 1;
	
	/** The Constant DOWN. */
	private final static int DOWN = -1;
	
	/** The up calls array indicates whether or not there is a up call on each floor. */
	private boolean[] upCalls;
	
	/** The down calls array indicates whether or not there is a down call on each floor. */
	private boolean[] downCalls;
	
	/** The up call pending - true if any up calls exist */
	private boolean upCallPending;
	
	/** The down call pending - true if any down calls exit */
	private boolean downCallPending;
	
	
	//TODO: Add any additional fields here..
	
	/**
	 * Instantiates a new call manager.
	 *
	 * @param floors the floors
	 * @param numFloors the num floors
	 */
	// peer-reviewed: Aman
	public CallManager(Floor[] floors, int numFloors) {
		this.floors = floors;
		NUM_FLOORS = numFloors;
		upCalls = new boolean[NUM_FLOORS];
		downCalls = new boolean[NUM_FLOORS];
		upCallPending = false;
		downCallPending = false;
		
		//TODO: Initialize any added fields here
	}
	
	/**
	 * Update call status. This is an optional method that could be used to compute
	 * the values of all up and down call fields statically once per tick (to be
	 * more efficient, could only update when there has been a change to the floor queues -
	 * either passengers being added or being removed. The alternative is to dynamically
	 * recalculate the values of specific fields when needed.
	 */
	// peer-reviewed: Aman
	protected void updateCallStatus() {
		//TODO: Write this method if you choose to implement it...
		upCallPending = false;
		downCallPending = false;
		for(int i = 0; i < NUM_FLOORS; i++) {
			if(floors[i].empty(UP)) {
				upCalls[i] = false;
			}
			else {
				upCalls[i] = true;
				upCallPending = true;
			}
			if(floors[i].empty(DOWN)) {
				downCalls[i] = false;
			}
			else {
				downCalls[i] = true;
				downCallPending = true;
			}
		}
	}
	
	/**
	 * Prioritize other floors.
	 *
	 * @param floor the floor
	 * @return the passengers
	 */
	// peer-reviewed: Aman
	protected Passengers prioritizeOtherFloors(int floor) {
		// added by Adi
		int lowestUp = 0;
		int highestDown = 0;
		for(int i = 0; i < NUM_FLOORS; i++) {
			if (!floors[i].empty(UP)) {
				lowestUp = i;
				break;
			}
		}

		for(int i = NUM_FLOORS-1; i >= 0; i--) {
			if (!floors[i].empty(DOWN)) {
				highestDown = i;
				break;
			}
		}
		if(getNumUp() > getNumDown()) {
			return floors[lowestUp].peek(UP);
		} else if(getNumUp() < getNumDown()){
			return floors[highestDown].peek(DOWN);
		} else {
			if (Math.abs(floor-lowestUp) <= Math.abs(highestDown-floor)) {
				return floors[lowestUp].peek(UP);
			} else  {
				return floors[highestDown].peek(DOWN);
			}
		}
	}

	/**
	 * Prioritize passenger calls from STOP STATE
	 *
	 * @param floor the floor
	 * @return the passengers
	 */
	// peer-reviewed: Aman
	protected Passengers prioritizePassengerCalls(int floor) {
		//TODO: Write this method based upon prioritization from STOP...
		// modified by Adi
		if(upCalls[floor] && !downCalls[floor]) {
			return floors[floor].peek(UP);
		}
		if(!upCalls[floor] && downCalls[floor]) {
			return floors[floor].peek(DOWN);
		}
		if(upCalls[floor] && downCalls[floor]) {
			if(getNumUp(floor) >= getNumDown(floor)) {
				return floors[floor].peek(UP);
			} else if(getNumUp(floor) < getNumDown(floor)) {
				return floors[floor].peek(DOWN);
			}
				
		}
		
		return prioritizeOtherFloors(floor);
	}
	
	/**
	 * Prioritize passenger calls.
	 *
	 * @param floor the floor
	 * @param dir the dir
	 * @return the passengers
	 */
	//added by adi
	// peer-reviewed: Aman
	protected Passengers getPassenger(int floor, int dir) {
		return floors[floor].peek(dir);
	}

	//TODO: Write any additional methods here. Things that you might consider:
	//      1. pending calls - are there any? only up? only down?
	//      2. is there a call on the current floor in the current direction
	//      3. How many up calls are pending? how many down calls are pending? 
	//      4. How many calls are pending in the direction that the elevator is going
	//      5. Should the elevator change direction?
	//
	//      These are an example - you may find you don't need some of these, or you may need more...

	/**
	 * Call pending.
	 *
	 * @return true, if successful
	 */
	// peer-reviewed: Aman
	protected boolean callPending() {
		if(upCallPending || downCallPending) {
			return true;
		}
		return false;
	}
	// peer-reviewed: Aman
	protected boolean callsPendingOnFloor(int currFloor, int direction) {
		if(floors[currFloor].empty(direction)) {
			return false;
		}
		return true;
	}

	//added by adi
	//after you offload passengers, there are no calls in the same direction on currFloor
	/**
	 * Should change dir.
	 *
	 * @param currFloor the curr floor
	 * @param direction the direction
	 * @return true, if successful
	 */
	//if there are no calls in the same dir 
	// peer-reviewed: Aman
	protected boolean shouldChangeDir(int currFloor, int direction) {
		if (callsPendingInDir(currFloor, direction)) {
			return false;
		} else if (callsPendingOnFloor(currFloor, direction*-1)) {
			return true;
		}
		return false;
	}

	
	/**
	 * Calls pending in dir.
	 *
	 * @param currFloor the curr floor
	 * @param direction the direction
	 * @return true, if successful
	 */
	//added by adi
	// peer-reviewed: Aman
	protected boolean callsPendingInDir(int currFloor, int direction) {
		if(direction == UP) {
			for(int i = currFloor+1; i < NUM_FLOORS; i++) {
				if (!floors[i].empty(UP) || !floors[i].empty(DOWN)) {
					return true;
				}
			}
		} else {
			for(int i = currFloor-1; i >= 0; i--) {
				if (!floors[i].empty(UP) || !floors[i].empty(DOWN)) {
					return true;
				}
			}
		}
		return false;	
	}
	
	/**
	 * Num call pending.
	 *
	 * @return the int
	 */
	// peer-reviewed: Aman
	protected int numCallPending() {
		int calls = 0;
		for(int i = 0; i < NUM_FLOORS; i++) {
			calls += floors[i].size(UP);
			calls += floors[i].size(DOWN);
		}
		return calls;
	}
	
	/**
	 * Num call pending.
	 *
	 * @param floor the floor
	 * @return the int
	 */
	// peer-reviewed: Aman
	protected int numCallPending(int floor) {
		int callsOnFloor = 0;
		callsOnFloor += floors[floor].size(UP);
		callsOnFloor += floors[floor].size(DOWN);
		return callsOnFloor;
	}
	
	
	
	/**
	 * Gets the num up.
	 *
	 * @return the num up
	 */
	// peer-reviewed: Aman
	private int getNumUp() {
		int numUp = 0;
		for(int i = 0; i < NUM_FLOORS; i++) {
			numUp += floors[i].size(UP);
		}
		return numUp;
	}
	
	/**
	 * Gets the num down.
	 *
	 * @return the num down
	 */
	// peer-reviewed: Aman
	private int getNumDown() {
		int numDown = 0;
		for(int i = NUM_FLOORS-1; i >= 0; i--) {
			numDown += floors[i].size(DOWN);
		}
		return numDown;
	}
	
	/**
	 * Gets the num up.
	 *
	 * @param floor the floor
	 * @return the num up
	 */
	// peer-reviewed: Aman
	private int getNumUp(int floor) {
		int numUp = 0;
		for(int i = floor+1; i < NUM_FLOORS; i++) {
			numUp += floors[i].size(UP);
		}
		return numUp;
	}
	
	/**
	 * Gets the num down.
	 *
	 * @param floor the floor
	 * @return the num down
	 */
	// peer-reviewed: Aman
	private int getNumDown(int floor) {
		int numDown = 0;
		for(int i = floor-1; i >= 0; i--) {
			numDown += floors[i].size(DOWN);
		}
		return numDown;
	}
	

}
