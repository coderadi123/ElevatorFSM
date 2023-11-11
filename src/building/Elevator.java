package building;
import java.util.ArrayList;


/**
 * @author Andrew Jacobsen and Adi Narasimha
 * This class will represent an elevator, and will contain
 * configuration information (capacity, speed, etc) as well
 * as state information - such as stopped, direction, and count
 * of passengers targeting each floor...
 */
public class Elevator {
	/** Elevator State Variables - These are visible publicly */
	public final static int STOP = 0;
	public final static int MVTOFLR = 1;
	public final static int OPENDR = 2;
	public final static int OFFLD = 3;
	public final static int BOARD = 4;
	public final static int CLOSEDR = 5;
	public final static int MV1FLR = 6;

	/** Default configuration parameters for the elevator. These should be
	 *  updated in the constructor.
	 */
	private int capacity = 15;				// The number of PEOPLE the elevator can hold
	private int ticksPerFloor = 5;			// The time it takes the elevator to move between floors
	private int ticksDoorOpenClose = 2;  	// The time it takes for doors to go from OPEN <=> CLOSED
	private int passPerTick = 3;            // The number of PEOPLE that can enter/exit the elevator per tick
	
	/** Finite State Machine State Variables */
	private int currState;		// current state
	private int prevState;      // prior state
	private int prevFloor;      // prior floor
	private int currFloor;      // current floor
	private int direction;      // direction the Elevator is traveling in.

	private int timeInState;    // represents the time in a given state
	                            // reset on state entry, used to determine if
	                            // state has completed or if floor has changed
	                            // *not* used in all states 

	private int doorState;      // used to model the state of the doors - OPEN, CLOSED
	                            // or moving

	
	private int passengers;  	// the number of people in the elevator
	
	private ArrayList<Passengers>[] passByFloor;  // Passengers to exit on the corresponding floor

	private int moveToFloor;	// When exiting the STOP state, this is the floor to move to without
	                            // stopping.
	
	private int postMoveToFloorDir; // This is the direction that the elevator will travel AFTER reaching
	                                // the moveToFloor in MVTOFLR state.
	private boolean isBoarding;
	private boolean isBoardingDone;
	private boolean isOffloading;
	private boolean isOffloadingDone;
	private int timeNeeded;
	private int totalBoarding;
	private boolean canBoard;
	private boolean forcedOpen;
	
	public static int CLOSED = 0;
	
	public static int MOVING = 1;
	
	public static int OPEN = 2;
	

	/**
	 * Instantiates a new elevator.
	 *
	 * @param numFloors the num floors
	 * @param capacity the capacity
	 * @param floorTicks the floor ticks
	 * @param doorTicks the door ticks
	 * @param passPerTick the pass per tick
	 */
	// peer-reviewed: Aman
	@SuppressWarnings("unchecked")
	public Elevator(int numFloors,int capacity, int floorTicks, int doorTicks, int passPerTick) {		
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
		this.currFloor = 0;
		passByFloor = new ArrayList[numFloors];
		
		for (int i = 0; i < numFloors; i++) 
			passByFloor[i] = new ArrayList<Passengers>(); 

		//TODO: Finish this constructor, adding configuration initialiation and
		//      initialization of any other private fields, etc.
		this.capacity = capacity;
		ticksPerFloor = floorTicks;
		ticksDoorOpenClose = doorTicks;
		this.passPerTick = passPerTick;
		doorState = CLOSED;
		OPEN = doorTicks;
		passengers = 0;
		isBoarding = false;
		timeNeeded = 0;
		totalBoarding = 0;
		canBoard = true;
		forcedOpen = false;
		isBoardingDone = true;
	}
	
	
	//TODO: Add Getter/Setters and any methods that you deem are required. Examples 
		//      include:
		//      1) moving the elevator
		//      2) closing the doors
		//      3) opening the doors
		//      and so on...
	
	
	// peer-reviewed: Aman
	public boolean isForcedOpen() {
		return forcedOpen;
	}

	// peer-reviewed: Aman
	public void setForcedOpen(boolean forcedOpen) {
		this.forcedOpen = forcedOpen;
	}


	/**
	 * @param currState
	 * This method updates the current state of the elevator
	 */
	
	// peer-reviewed: Aman
	protected void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		if(this.prevState != currState) {
			timeInState = 0;
			canBoard = true;
		}
	}
	/**
	 * Moves the elevator in the current direction
	 */
	// peer-reviewed: Aman
	protected void moveElevator() {
		timeInState++;
		prevFloor = currFloor;
		if((timeInState % ticksPerFloor) == 0) {
			currFloor = currFloor + direction;
		}
	}

	/**
	 * Opens the door to the elevator
	 */
	// peer-reviewed: Aman
	protected void openDoor() {
		//increment doorState 0 is fully closed numTicks it takes to open door is fully open
		prevFloor = currFloor;
		doorState++;
	}
	/**
	 * Closes the door to the elevator
	 */
	// peer-reviewed: Aman
	protected void closeDoor() {
		canBoard = true;
		prevFloor = currFloor;
		doorState--;
	}
	
	//added by adi
	/**
	 * 
	 * @param Passengers p, the passengers needing to board
	 * This method takes passengers from the floor and puts them on the elevator
	 * @return boolean depending on if the passengers were able to board
	 */
	// peer-reviewed: Aman
	protected boolean board(Passengers p) {
		
		int passBoarding = p.getNumPass();
		if(passBoarding + passengers > capacity) {
			canBoard = false;
			return false;
		}
		totalBoarding += passBoarding;
		passByFloor[p.getDestFloor()].add(p);
		passengers += passBoarding;
		
		timeNeeded = (int)Math.ceil((totalBoarding*1.0)/passPerTick);
		
		return true;
	}
	
	//added by adi
	/**
	 * This method updates the boarding state
	 * @return always returns true
	 */
	// peer-reviewed: Aman
	protected boolean keepBoarding() {	
		timeInState++;
		if (timeInState == timeNeeded) {
			isBoardingDone = true;
			isBoarding = false;		
			totalBoarding = 0;
			canBoard = true;
		} else {
			isBoardingDone = false;
			isBoarding = true;
		}
		return true;
	}
	
	//added by adi
	/**
	 * @param floor, the current floor that passengers are offloading to
	 * This takes passengers off the elevator and puts them on the floor
	 * @return an array list of passengers that were offloaded
	 */
	// peer-reviewed: Aman
	protected ArrayList<Passengers> offload(int floor) {
		isOffloadingDone = false;
		isOffloading = true;
		int passOffloading = 0;
		ArrayList<Passengers> groups = passByFloor[floor];
		ArrayList<Passengers> offGroup = new ArrayList<Passengers>(); 
		for (Passengers p: groups) {
			offGroup.add(p);
			passOffloading += p.getNumPass();
			
		}
		passByFloor[floor] = new ArrayList<Passengers>();
		passengers -= passOffloading;
		timeNeeded = (int)Math.ceil((passOffloading*1.0)/passPerTick);
		
		keepOffloading();
		return offGroup;
	}
	
	//added by adi
	/**
	 * This method updates the offloading state
	 * @return true
	 */
	// peer-reviewed: Aman
	protected boolean keepOffloading() {	
		timeInState++;
		
		if (timeInState == timeNeeded) {
			isOffloadingDone = true;
			isOffloading = false;	
		} else {
			isOffloadingDone = false;
			isOffloading = true;
		}
		return true;
	}
	/**
	 * This method changes the direction of the elevator
	 */
	// peer-reviewed: Aman
	protected void changeDirection() {
		direction = direction * -1;
	}
	
	/**
	 * 
	 * @param currentFloor, The floor that the elevator is on
	 * @return boolean, true if there are passengers to offload on this floor, 
	 * @return false if there are no passengers getting off
	 */
	// peer-reviewed: Aman
	protected boolean passengersToOffload(int currentFloor) {
		return !passByFloor[currentFloor].isEmpty();
	}
	/**
	 * @return whether the elevator is currently offloading
	 */
	protected boolean isOffloading() {
		return isOffloading;
	}
	/**
	 * @return whether the elevator is done offloading
	 */
	protected boolean isOffloadDone() {
		return isOffloadingDone;
	}
	/**
	 * @return boolean, true if elevator is full, false if there is room for more passengers
	 */
	protected boolean isFull() {
		return passengers >= capacity;
	}
	/**
	 * @return whether the elevator is currently boarding
	 */
	protected boolean isBoarding() {
		return isBoarding;
	}
	/**
	 * @return whether the elevator is finished boarding
	 */
	protected boolean isBoardingDone() {
		return isBoardingDone;
	}
	/**
	 * 
	 * @return boolean, true if the elevator is empty, false if there are passengers on the elevator
	 */
	protected boolean isEmpty() {
		return passengers == 0;
	}
	
	/**
	 * 
	 * @return the capacity of the elevator
	 */
	public int getCapacity() {
		return capacity;
	}
	/**
	 * This method sets the capacity
	 * @param capacity, the total capacity of the elevator
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	/**
	 * 
	 * @return the amount of ticks it takes to change floors
	 */
	public int getTicksPerFloor() {
		return ticksPerFloor;
	}
	/**
	 * This method sets the amount of ticks it takes to change floors
	 * @param ticksPerFloor
	 */
	public void setTicksPerFloor(int ticksPerFloor) {
		this.ticksPerFloor = ticksPerFloor;
	}
	/**
	 * 
	 * @return the amount of ticks it takes to open or close the door
	 */
	public int getTicksDoorOpenClose() {
		return ticksDoorOpenClose;
	}
	/**
	 * This method sets the amount of ticks it takes to open or close the door
	 * @param ticksDoorOpenClose
	 */
	public void setTicksDoorOpenClose(int ticksDoorOpenClose) {
		this.ticksDoorOpenClose = ticksDoorOpenClose;
	}
	/**
	 * 
	 * @return the amount of passengers can board or offload in one tick
	 */
	public int getPassPerTick() {
		return passPerTick;
	}
	/**
	 * This method sets the amount of passengers can move on or off the elevator in one tick
	 * @param passPerTick
	 */
	public void setPassPerTick(int passPerTick) {
		this.passPerTick = passPerTick;
	}
	/**
	 * 
	 * @return the current state of the elevator
	 */
	public int getCurrState() {
		return currState;
	}
	/**
	 * This method sets the current state of the elevator
	 * @param currState
	 */
	public void setCurrState(int currState) {
		this.currState = currState;
	}
	/**
	 * 
	 * @return The previous state of the elevator
	 */
	public int getPrevState() {
		return prevState;
	}
	/**
	 * Sets the previous state of the elevator
	 * @param prevState
	 */
	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}
	/**
	 * 
	 * @return the previous floor of the elevator
	 */
	public int getPrevFloor() {
		return prevFloor;
	}
	/**
	 * Sets the previous floor of the elevator
	 * @param prevFloor
	 */
	public void setPrevFloor(int prevFloor) {
		this.prevFloor = prevFloor;
	}
	/**
	 * 
	 * @return the current floor of the elevator
	 */
	public int getCurrFloor() {
		return currFloor;
	}
	/**
	 * Sets the current floor of the elevator
	 * @param currFloor
	 */
	public void setCurrFloor(int currFloor) {
		this.currFloor = currFloor;
	}
	/**
	 * 
	 * @return the direction of the elevator
	 */
	public int getDirection() {
		return direction;
	}
	/**
	 * Sets the direction of the elevator
	 * @param direction
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}
	/**
	 * 
	 * @return The time in the current state
	 */
	public int getTimeInState() {
		return timeInState;
	}
	/**
	 * Sets the time in current state
	 * @param timeInState
	 */
	public void setTimeInState(int timeInState) {
		this.timeInState = timeInState;
	}
	/**
	 * 
	 * @return the Door state either open or closed
	 */
	public int getDoorState() {
		return doorState;
	}
	/**
	 * Opens or closes the door based on what the current door state is
	 * @param doorState
	 */
	public void changeDoorState(int doorState) {
		if(doorState == OPEN) {
			openDoor();
		} else {
			closeDoor();
		}
	}
	/**
	 * 
	 * @return the number of people in the elevator
	 */
	public int getPassengers() {
		return passengers;
	}
	/**
	 * Sets the amount of passengers in the elevator
	 * @param passengers
	 */
	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}
	/**
	 * 
	 * @return The array of array lists of passengers sorted by what floor they get off on.
	 */
	public ArrayList<Passengers>[] getPassByFloor() {
		return passByFloor;
	}
	/**
	 * Sets teh array of array lists of passengers sorted by what floor they get off on
	 * @param passByFloor
	 */
	public void setPassByFloor(ArrayList<Passengers>[] passByFloor) {
		this.passByFloor = passByFloor;
	}
	/**
	 * 
	 * @return The floor to go to from stop state
	 */
	public int getMoveToFloor() {
		return moveToFloor;
	}
	/**
	 * Sets the floor to go to from stop state
	 * @param moveToFloor
	 */
	public void setMoveToFloor(int moveToFloor) {
		this.moveToFloor = moveToFloor;
	}
	/**
	 * 
	 * @return The direction after moving to floor
	 */
	public int getPostMoveToFloorDir() {
		return postMoveToFloorDir;
	}
	/**
	 * Sets the direction after moving to floor
	 * @param postMoveToFloorDir
	 */
	public void setPostMoveToFloorDir(int postMoveToFloorDir) {
		this.postMoveToFloorDir = postMoveToFloorDir;
	}
	
	
	public boolean getCanBoard() {
		return canBoard;
	}








	
}
