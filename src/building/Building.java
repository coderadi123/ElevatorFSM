package building;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import myfileio.MyFileIO;
import genericqueue.GenericQueue;

/**
 * @author Adi Narasimha 
 * The Building class models the building and contains
 *         floors and elevators. Building also maintains passenger queue and
 *         adds passengers to appropriate floor queues Building class implements
 *         the functionality of the FSM states and logs relevant events
 */

//find what the 
public class Building {
	
	/**  Constants for direction. */
	private final static int UP = 1;
	
	/** The Constant DOWN. */
	private final static int DOWN = -1;
	
	/** The Constant LOGGER. */
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());
	
	/**  The fh - used by LOGGER to write the log messages to a file. */
	private FileHandler fh;
	
	/**  The fio for writing necessary files for data analysis. */
	private MyFileIO fio;
	
	/**  File that will receive the information for data analysis. */
	private File passDataFile;

	/**  passSuccess holds all Passengers who arrived at their destination floor. */
	private ArrayList<Passengers> passSuccess;
	

	/**  gaveUp holds all Passengers who gave up and did not use the elevator. */
	private ArrayList<Passengers> gaveUp;
	
	/**  The number of floors - must be initialized in constructor. */
	private final int NUM_FLOORS;
	
	/**  The size of the up/down queues on each floor. */
	private final int FLOOR_QSIZE = 10;	
	
	/** passQ holds the time-ordered queue of Passengers, initialized at the start 
	 *  of the simulation. At the end of the simulation, the queue will be empty.
	 */
	private GenericQueue<Passengers> passQ;

	/**  The size of the queue to store Passengers at the start of the simulation. */
	private final int PASSENGERS_QSIZE = 1000;	
	
	/**  The number of elevators - must be initialized in constructor. */
	private final int NUM_ELEVATORS;
	
	/** The floors. */
	public Floor[] floors;
	
	/** The elevators. */
	private Elevator[] elevators;
	
	/**  The Call Manager - it tracks calls for the elevator, analyzes them to answer questions and prioritize calls. */
	private CallManager callMgr;
	
	// Add any fields that you think you might need here...

	
	//building checks to see if the floorQ has changed as well as the num of people in elevator
	//relays that info to the controller which then updates the gui
	
	
	/**
	 * Instantiates a new building.
	 *
	 * @param numFloors    the number of floors in the building
	 * @param numElevators the num elevators
	 * @param logfile      the logfile
	 */
	public Building(int numFloors, int numElevators, String logfile) {
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		passQ = new GenericQueue<Passengers>(PASSENGERS_QSIZE);
		passSuccess = new ArrayList<Passengers>();
		gaveUp = new ArrayList<Passengers>();
		Passengers.resetStaticID();		
		initializeBuildingLogger(logfile);
		// passDataFile is where you will write all the results for those passengers who successfully
		// arrived at their destination and those who gave up...
		fio = new MyFileIO();
		passDataFile = fio.getFileHandle(logfile.replaceAll(".log","PassData.csv"));
		
		// create the floors, call manager and the elevator arrays
		// note that YOU will need to create and config each specific elevator...
		floors = new Floor[NUM_FLOORS];
		for (int i = 0; i < NUM_FLOORS; i++) {
			floors[i]= new Floor(FLOOR_QSIZE); 
		}
		callMgr = new CallManager(floors,NUM_FLOORS);
		elevators = new Elevator[NUM_ELEVATORS];
		
		
	}
	
	// TODO: Place all of your code HERE - state methods and helpers...
	
	/**
	 * Config elevators.
	 *
	 * @param capacity       the elevator passenger capacity
	 * @param floorTicks     the ticks needed to transition from one floor to next
	 * @param doorTicks      the ticks needed to open or close the doors
	 * @param tickPassengers the number of passengers that can board or offload in a
	 *                       tick of time configs a new elevator
	 * 
	 */
	// peer-reviewed: Aman
	public void configElevators(int capacity, int floorTicks, int doorTicks, int tickPassengers) {
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i] = new Elevator(NUM_FLOORS, capacity, floorTicks, doorTicks, tickPassengers);
			logElevatorConfig(capacity, floorTicks, doorTicks, tickPassengers, elevators[i].getCurrState(), elevators[i].getCurrFloor());
		}
	}

	/**
	 * Adds the passengers to queue.
	 *
	 * @param time      the current time
	 * @param numPass   the number of passengers in the current group
	 * @param fromFloor the from floor where passengers board
	 * @param toFloor   the to floor where passengers are traveling to
	 * @param polite    the polite flag for whether or not passengers will try to
	 *                  open a closing door
	 * @param wait      the time passengers are willing to wait before giving up
	 * @return a boolean indicating whether or not a passenger has been added to the
	 *         passQ
	 */
	// peer-reviewed: Aman
	public boolean addPassengersToQueue(int time, int numPass, int fromFloor, int toFloor, boolean polite, int wait) {
		Passengers p = new Passengers(time, numPass, fromFloor, toFloor, polite, wait);
		return passQ.add(p);
	}
	
	/**
	 * endSim returns a boolean indicating whether the simulation has ended if the
	 * elevators' current and previous states are both stop, passQ as well as
	 * floorQs in both directions are all empty, then endSim will return true and
	 * the simulation will end.
	 *
	 * @param time is the current time in ticks since the simulation started
	 * @return true, if successful
	 */
	// peer-reviewed: Aman
	public boolean endSim(int time) {
		// checks if passQ is empty, if elevator is stopped and if 
		// elevator door is closed: return true, else false
		if (passQ.isEmpty()) {
			for (int i = 0; i < NUM_ELEVATORS; i++) {
				if (!elevators[i].isEmpty() || elevators[i].getCurrState() != Elevator.STOP || elevators[i].getPrevState() != Elevator.STOP) {
					return false;
				}
			}
			for(int i = 0; i < NUM_FLOORS; i++) {
				if(!floors[i].empty(UP) || !floors[i].empty(DOWN)) {
					return false;
				}
			}
			logEndSimulation(time);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Elevator state changed.
	 *
	 * @param lift the lift
	 * @return true, if successful
	 * @returns a boolean if the elevatorState has changed basically if the
	 *          prevState is diff from the currState or if the floor has changed
	 */
	// peer-reviewed: Aman
	private boolean elevatorStateChanged(Elevator lift) {
		if (lift.getPrevState()!=lift.getCurrState() || lift.getPrevFloor()!=lift.getCurrFloor()) {
			return true;
		}
		return false;	
	}
	
	/**
	 * Curr state stop.
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the int
	 * @returns an int indicating the next state of the elevator after the current
	 *          state is stop. In stop state, if no pending calls, remain in stop
	 *          state else, invoke callManager's prioritizePassengerCalls to decide
	 *          to floor to move to. Based on passengers call, decide the
	 *          postmovetofloordir
	 */
	// peer-reviewed: Aman
	private int currStateStop(int time, Elevator lift) {
		// action
		// Adi: do we really need to increment timeInState for stop state?
		// lift.setTimeInState(lift.getTimeInState()+1);
		
		// next state
		if(!callMgr.callPending()) {
			return Elevator.STOP;
		} else {
			int liftFlr = lift.getCurrFloor();
			Passengers p = callMgr.prioritizePassengerCalls(liftFlr);
			int passengerFlr = p.getOnFloor();
			
			int dir = p.getDestFloor() < passengerFlr? DOWN: UP;
			if(passengerFlr == liftFlr) {
				lift.setDirection(dir);
				return Elevator.OPENDR;
			} else {
				lift.setDirection(passengerFlr < liftFlr? DOWN: UP);
				lift.setMoveToFloor(passengerFlr);
				lift.setPostMoveToFloorDir(dir);
				return Elevator.MVTOFLR;
			}
		}
	}
	
	/**
	 * Curr state mv to flr.
	 *
	 * @param time the current time
	 * @param lift the lift
	 * @return the next state the elevator should be in if the current state is move
	 *         to floor. if the currentFlr is != getMoveToFloor then remain in
	 *         MVTOFLR else the elevator has moved to the right floor, so next state
	 *         is open door
	 */
	// peer-reviewed: Aman
	private int currStateMvToFlr(int time, Elevator lift) {
		// action
		lift.moveElevator();
		
		// next state
		if(lift.getCurrFloor() != lift.getMoveToFloor()) {
			return Elevator.MVTOFLR;
		} else {
			logElevatorStateChanged(time, lift.getPrevState(), lift.getCurrState(), lift.getPrevFloor(), lift.getCurrFloor());
			lift.setDirection(lift.getPostMoveToFloorDir());
			return Elevator.OPENDR;
		}
	}
	
	/**
	 * Curr state open dr.
	 *
	 * @param time the current time
	 * @param lift the lift
	 * @return the next state the elevator should be in if the current state is open
	 *         door if the doorState is closed, first openDoor else if the door is
	 *         already open begin offLoading (if any) and then board passengers (if
	 *         any)
	 */
	// peer-reviewed: Aman
	private int currStateOpenDr(int time, Elevator lift) {
		// action
		lift.changeDoorState(Elevator.OPEN);

		// next state
		if (lift.getDoorState() != Elevator.OPEN) {
			return Elevator.OPENDR;
		} else {
			if (lift.passengersToOffload(lift.getCurrFloor())) {
				return Elevator.OFFLD;
			} else {
				return Elevator.BOARD;
			}
		}
	}
	
	// peer-reviewed: Aman
	private void offLdAction(int time, Elevator lift) {
		int floor = lift.getCurrFloor();
		if(!lift.isOffloading()) {
			// mark arrival time for all arriving passengers
			ArrayList<Passengers> offGroup = lift.offload(floor); 
			for (Passengers p: offGroup) {
				p.setTimeArrived(time);
				passSuccess.add(p);
				logArrival(time, p.getNumPass(), floor, p.getId());
			}
		} else {
			lift.keepOffloading();
		}
		return;
	}
	
	/**
	 * Curr state offload.
	 *
	 * @param time the current time
	 * @param lift the lift
	 * @returns the next state if the currentState is offload. First, offload all
	 *          passengers who need to get off on this floor Next, if there are
	 *          passengers on this floor who are going in the same direction as the
	 *          elevator, board them. Else, if lift is empty, then check with
	 *          callMgr if the elevator direction needs to be changed IF pass on
	 *          current floor, board else closedoor
	 */
	// peer-reviewed: Aman
	private int currStateOffLd(int time, Elevator lift) {
		// action
		offLdAction(time, lift);
		
		// next state
		if (!lift.isOffloadDone()) {
			return Elevator.OFFLD;
		} else {
			if(callMgr.callsPendingOnFloor(lift.getCurrFloor(), lift.getDirection())) {
				return Elevator.BOARD;
			} else if (lift.isEmpty() && callMgr.shouldChangeDir(lift.getCurrFloor(), lift.getDirection())) {
				lift.changeDirection();
				if(callMgr.callsPendingOnFloor(lift.getCurrFloor(), lift.getDirection())) {
					return Elevator.BOARD;
				}
				return Elevator.CLOSEDR;
			} else {
				return Elevator.CLOSEDR;
			}
		}
	}
	
	// this function checks if passengers waiting at this floor need to be skipped. 
	// passengers will be skipped if the lift is already full upon arrival
	// peer-reviewed: Aman
	private void handleInitialSkips(int time, Elevator lift) {
		int floor = lift.getCurrFloor();
		if (lift.isFull()) {
			
			Passengers p = null;
			if(callMgr.callsPendingOnFloor(floor, lift.getDirection())) {
				p = callMgr.getPassenger(floor, lift.getDirection());
			}
			if(p != null) {
				int dir = p.getDestFloor() > p.getOnFloor()? UP: DOWN;
				logSkip(time, p.getNumPass(), floor, dir, p.getId());
			}
		}
	}
	
	// this function takes care of the action needed in board state
	// while lift is not full and the next group to board can fit, attempt to board the passengers
	// if wait time has exceeded, mark the pass as gaveup
	// if pass group is too large to fit in the remaining capacity, skip
	// peer-reviewed: Aman
	private boolean boardAction(int time, Elevator lift) {
		int floor = lift.getCurrFloor();
		boolean boarded = false;
		
		while(!lift.isFull() && lift.getCanBoard()) {
			Passengers p = null;
			if(callMgr.callsPendingOnFloor(floor, lift.getDirection())) {
				p = callMgr.getPassenger(floor, lift.getDirection());
			}
			if(p == null) {
				break;
			}				
			int dir = p.getDestFloor() > p.getOnFloor()? UP: DOWN;		
			if(time > (p.getTime()+p.getWaitTime())) { // pass gaveup
				floors[p.getOnFloor()].poll(dir);
				gaveUp.add(p);
				logGiveUp(time, p.getNumPass(), floor, dir, p.getId());
				continue;
			}			
			if(lift.board(p)) { //pass boarding so remove from floor queues
				lift.setForcedOpen(false);
				floors[p.getOnFloor()].poll(dir);
				p.setBoardTime(time);
				logBoard(time, p.getNumPass(), floor, dir, p.getId());
				boarded = true;
			} else {
				logSkip(time, p.getNumPass(), floor, dir, p.getId());
				break;
			}
		}
		return boarded;
	}
	
	/**
	 * Curr state board.
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the the next state if the curr state is board. Remain in board state
	 *         until all passengers who can be boarded are boarded. Then transition
	 *         to closedoor state
	 */
	// peer-reviewed: Aman
	private int currStateBoard(int time, Elevator lift) {
		// action
		handleInitialSkips(time, lift);
		boolean boarded = boardAction(time, lift);
		if(boarded) {
			lift.keepBoarding();
		}
		
		// next state
		if(!lift.isBoardingDone()) {			
			if (!boarded && lift.isBoarding()) {
				lift.keepBoarding();
			}
			if(!lift.isBoardingDone()) {
				return Elevator.BOARD;
			} else {
				return Elevator.CLOSEDR;
			}
		} else {
			return Elevator.CLOSEDR;
		}
	}
	
	// If lift is empty when doors closed, use the following logic to decide what to
	// do next:
	// If no calls penging, transition to stop state
	// if calls pending on any floor in the current dir, move 1 floor
	// else, if calls penging on same floor, open door
	// else, change direction and if passengers on the same floor open door or move
	// 1 floor
	// peer-reviewed: Aman
	private int closeDrEmptyLift(int time, Elevator lift) {
		int floor = lift.getCurrFloor();
		if (!callMgr.callPending()) {
			return Elevator.STOP;
		}
		if (callMgr.callsPendingInDir(floor, lift.getDirection())) {
			return Elevator.MV1FLR;
		} else {
			if (callMgr.callsPendingOnFloor(floor, lift.getDirection())) {
				return Elevator.OPENDR;
			}
			// no calls pending for the current dir but there ARE pending calls so change
			// direction
			lift.changeDirection();
			if (callMgr.callsPendingOnFloor(floor, lift.getDirection())) {
				return Elevator.OPENDR;
			} else {
				return Elevator.MV1FLR;
			}
		}
	}

	/**
	 * Curr state close dr.
	 *
	 * @param time the time
	 * @param lift the lift
	 * @return the next state if the current state is close door check if there are
	 *         any impolite passengers on the floor, if yes, open the door (unless
	 *         it was already opened for them once) once the doors are fully closed,
	 *         transition to move one floor.
	 */
	// peer-reviewed: Aman
	private int currStateCloseDr(int time, Elevator lift) {
		// action
		lift.changeDoorState(Elevator.CLOSED);

		// next state
		int floor = lift.getCurrFloor();
		if(!lift.isForcedOpen()) {
			Passengers p = callMgr.getPassenger(floor, lift.getDirection());
			if(p != null && !p.isPolite() && p.getTime() == time) {
				lift.setForcedOpen(true);
				return Elevator.OPENDR;
			}
		}
		if(lift.getDoorState() == Elevator.CLOSED) {
			lift.setForcedOpen(false);
			if (lift.isEmpty()) {
				return closeDrEmptyLift(time, lift);
			} else {
				return Elevator.MV1FLR;
			}
		} else {
			return Elevator.CLOSEDR;
		}
	}
	
	
	/**
	 * Curr state mv 1 flr.
	 *
	 * @param time the current time
	 * @param lift the lift
	 * @return the next state if the current state is move1floor. Keep moving the
	 *         elevator till elevator has transitioned to the next floor once
	 *         arrived, if there are passengers to offload, open door. Else, if lift
	 *         is empty and no calls pending, go to stop Else, if callmgr says
	 *         change direction, do so and open door
	 */
	// peer-reviewed: Aman
	private int currStateMv1Flr(int time, Elevator lift) {
		// action
		lift.moveElevator();
		
		// next state
		if(lift.getCurrFloor() != lift.getPrevFloor() + lift.getDirection()) {
			return Elevator.MV1FLR;
		} else {
			logElevatorStateChanged(time, lift.getPrevState(), lift.getCurrState(), lift.getPrevFloor(), lift.getCurrFloor());
			if(lift.passengersToOffload(lift.getCurrFloor()) || callMgr.callsPendingOnFloor(lift.getCurrFloor(), lift.getDirection())) {
				return Elevator.OPENDR;
			}  else {
				if (lift.isEmpty()) {
					if (!callMgr.callPending()) {
						return Elevator.STOP;
					} 
					if (callMgr.shouldChangeDir(lift.getCurrFloor(), lift.getDirection())){
						lift.changeDirection();
						return Elevator.OPENDR;
					}
				}
			}
			return Elevator.MV1FLR;
		}
	}
	
	/**
	 * Check passenger queue.
	 *
	 * @param time the current time If head of passQ has passengers that match the
	 *             current time, remove the passengers from passQ and move them to
	 *             floor queues. If pass needs to travel up, move them to up queue
	 *             of the floor else down queue of the correct floor.
	 */
	// peer-reviewed: Aman
	public void checkPassengerQueue(int time) {
		while (passQ.peek() != null) {
			if (passQ.peek().getTime() == time) {
				Passengers p = passQ.remove();
				// ternary logic same as if else to decide dir
				int dir = p.getDestFloor() < p.getOnFloor() ? DOWN : UP;
				floors[p.getOnFloor()].add(dir, p);
				logCalls(time, p.getNumPass(), p.getOnFloor(), dir, p.getId());
			} else {
				break;
			}
		}
		callMgr.updateCallStatus();
	}
	// DO NOT CHANGE ANYTHING BELOW THIS LINE:
	/**
	 * Initialize building logger. Sets formating, file to log to, and
	 * turns the logger OFF by default
	 *
	 * @param logfile the file to log information to
	 */
	void initializeBuildingLogger(String logfile) {
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$-7s %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Update elevator - this is called AFTER time has been incremented.
	 * -  Logs any state changes, if the have occurred,
	 * -  Calls appropriate method based upon currState to perform
	 *    any actions and calculate next state...
	 *
	 * @param time the time
	 */
	public void updateElevator(int time) {
		for (Elevator lift: elevators) {
			if (elevatorStateChanged(lift))
				logElevatorStateChanged(time,lift.getPrevState(),lift.getCurrState(),lift.getPrevFloor(),lift.getCurrFloor());

			switch (lift.getCurrState()) {
				case Elevator.STOP: lift.updateCurrState(currStateStop(time,lift)); break;
				case Elevator.MVTOFLR: lift.updateCurrState(currStateMvToFlr(time,lift)); break;
				case Elevator.OPENDR: lift.updateCurrState(currStateOpenDr(time,lift)); break;
				case Elevator.OFFLD: lift.updateCurrState(currStateOffLd(time,lift)); break;
				case Elevator.BOARD: lift.updateCurrState(currStateBoard(time,lift)); break;
				case Elevator.CLOSEDR: lift.updateCurrState(currStateCloseDr(time,lift)); break;
				case Elevator.MV1FLR: lift.updateCurrState(currStateMv1Flr(time,lift)); break;
			}
		}
	}

	
	
	/**
	 * Process passenger data. Do NOT change this - it simply dumps the 
	 * collected passenger data for successful arrivals and give ups. These are
	 * assumed to be ArrayLists...
	 */
	public void processPassengerData() {
		
		try {
			BufferedWriter out = fio.openBufferedWriter(passDataFile);
			out.write("ID,Number,From,To,WaitToBoard,TotalTime\n");
			for (Passengers p : passSuccess) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             (p.getBoardTime() - p.getTime())+","+(p.getTimeArrived() - p.getTime())+"\n";
				out.write(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             p.getWaitTime()+",-1\n";
				out.write(str);
			}
			fio.closeFile(out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enable logging. Prints the initial configuration message.
	 * For testing, logging must be enabled BEFORE the run starts.
	 */
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
		for (Elevator el:elevators)
			logElevatorConfig(el.getCapacity(),el.getTicksPerFloor(), el.getTicksDoorOpenClose(), el.getPassPerTick(), el.getCurrState(), el.getCurrFloor());
	}
	
	/**
	 * Close logs, and pause the timeline in the GUI.
	 *
	 * @param time the time
	 */
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			logEndSimulation(time);
			fh.flush();
			fh.close();
		}
	}
	
	/**
	 * Prints the state.
	 *
	 * @param state the state
	 * @return the string
	 */
	private String printState(int state) {
		String str = "";
		
		switch (state) {
			case Elevator.STOP: 		str =  "STOP   "; break;
			case Elevator.MVTOFLR: 		str =  "MVTOFLR"; break;
			case Elevator.OPENDR:   	str =  "OPENDR "; break;
			case Elevator.CLOSEDR:		str =  "CLOSEDR"; break;
			case Elevator.BOARD:		str =  "BOARD  "; break;
			case Elevator.OFFLD:		str =  "OFFLD  "; break;
			case Elevator.MV1FLR:		str =  "MV1FLR "; break;
			default:					str =  "UNDEF  "; break;
		}
		return(str);
	}
	
	/**
	 * Dump passQ contents. Debug hook to view the contents of the passenger queue...
	 */
	public void dumpPassQ() {
		ListIterator<Passengers> passengers = passQ.getListIterator();
		if (passengers != null) {
			System.out.println("Passengers Queue:");
			while (passengers.hasNext()) {
				Passengers p = passengers.next();
				System.out.println(p);
			}
		}
	}

	/**
	 * Log elevator config.
	 *
	 * @param capacity the capacity
	 * @param ticksPerFloor the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick the pass per tick
	 * @param state the state
	 * @param floor the floor
	 */
	private void logElevatorConfig(int capacity, int ticksPerFloor, int ticksDoorOpenClose, int passPerTick, int state, int floor) {
		LOGGER.info("CONFIG:   Capacity="+capacity+"   Ticks-Floor="+ticksPerFloor+"   Ticks-Door="+ticksDoorOpenClose+
				    "   Ticks-Passengers="+passPerTick+"   CurrState=" + (printState(state))+"   CurrFloor="+(floor+1));
	}
		
	/**
	 * Log elevator state changed.
	 *
	 * @param time the time
	 * @param prevState the prev state
	 * @param currState the curr state
	 * @param prevFloor the prev floor
	 * @param currFloor the curr floor
	 */
	private void logElevatorStateChanged(int time, int prevState, int currState, int prevFloor, int currFloor) {
		LOGGER.info("Time="+time+"   Prev State: " + printState(prevState) + "   Curr State: "+printState(currState)
		+"   PrevFloor: "+(prevFloor+1) + "   CurrFloor: " + (currFloor+1));
	}
	
	/**
	 * Log arrival.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param id the id
	 */
	private void logArrival(int time, int numPass, int floor,int id) {
		LOGGER.info("Time="+time+"   Arrived="+numPass+" Floor="+ (floor+1)
		+" passID=" + id);						
	}
	
	/**
	 * Log calls.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logCalls(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Called="+numPass+" Floor="+ (floor +1)
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);
	}
	
	/**
	 * Log give up.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   GaveUp="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log skip.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Skip="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log board.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Board="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log end simulation.
	 *
	 * @param time the time
	 */
	private void logEndSimulation(int time) {
		LOGGER.info("Time="+time+"   Detected End of Simulation");
	}
	
	/**
	 * Gets the elevators first index.
	 *
	 * @return the elevators first index
	 */
	public Elevator getElevatorsFirstIndex() {
		return elevators[0];
	}

	/**
	 * Sets the elevators.
	 *
	 * @param elevators the new elevators
	 */
	public void setElevators(Elevator[] elevators) {
		this.elevators = elevators;
	}

	/**
	 * Gets the floors.
	 *
	 * @return the floors
	 */
	public Floor[] getFloors() {
		return floors;
	}


	
	
	
}
