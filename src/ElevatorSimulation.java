

import java.util.ArrayList;

import building.Elevator;
import building.Passengers;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ElevatorSimulation extends Application {
	/** Instantiate the GUI fields */
	private ElevatorSimController controller;
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int currFloor = 1;
	private int passengers = 0;
	
	private int previousLogCall;
	
	private Timeline timeline;
	private Text t;
	private Text displayPassengers = new Text("");

	/** Local copies of the states for tracking purposes */
	private final int STOP = Elevator.STOP;
	private final int MVTOFLR = Elevator.MVTOFLR;
	private final int OPENDR = Elevator.OPENDR;
	private final int OFFLD = Elevator.OFFLD;
	private final int BOARD = Elevator.BOARD;
	private final int CLOSEDR = Elevator.CLOSEDR;
	private final int MV1FLR = Elevator.MV1FLR;

	private Pane pane = new Pane();
	
	private Rectangle elevatorDisplay = new Rectangle(25, 700, 170, 100);
	private Button log;
	private ArrayList<Passengers> passengerList = new ArrayList<>();
	private ArrayList<Ellipse> passDisplayList = new ArrayList<>();
	private TextField nTimes;
	
	/**
	 * Instantiates a new elevator simulation.
	 */
	public ElevatorSimulation() {
		controller = new ElevatorSimController(this);	
		NUM_FLOORS = controller.getNumFloors();
		NUM_ELEVATORS = controller.getNumElevators();
		currFloor = controller.getCurrentFloor();
		
		initTimeline();
		
		
	}
	
	/**
	 * Makes the timeline object and starts it
	 */
	private void initTimeline() {
		timeline = new Timeline(new KeyFrame(Duration.millis(100), ae -> controller.stepSim()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}	

	/**
	 * Start.
	 *
	 * @param primaryStage the primary stage
	 * @throws Exception the exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		Scene scene = new Scene(pane, 800, 1000);
		Rectangle r = new Rectangle(0, 0, 800, 1000);
		r.setFill(Color.DIMGRAY);
		pane.getChildren().add(r);

		makeButtonSection();		
		addFloorsDisplay();
		builidngTop();
		
		elevatorDisplay.setFill(Color.CADETBLUE);
		pane.getChildren().add(elevatorDisplay);

		primaryStage.setTitle("Elevator Simulation - "+ controller.getTestName());
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show();
		
	}
	
	/**
	 * When function is called, the timeline is paused and the information panel shows up
	 */
	private void giveInfoAboutGUI() {
		timeline.pause();
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText("Information");
		alert.setContentText("Green Nodes: UP\nBlue Nodes: DOWN\n\nEach node represents a group\n\nClick RUN to resume");
		alert.showAndWait();
	}
	
	/**
	 * Codes the roof of the building with the building name
	 */
	private void builidngTop() {
		pane.getChildren().add(new Rectangle(0, 190, 800, 10));
		pane.getChildren().add(new Rectangle(0, 800, 800, 10));
		Text buildingName = new Text(220, 150, "AAA Tower");
		buildingName.setFont(Font.font("Times New Roman", FontWeight.BOLD, 64));
		buildingName.setFill(Color.GOLDENROD);
		pane.getChildren().add(buildingName);
	}
	
	/**
	 * Make all the buttons and text fields/labels and add them to pane using hbox
	 */
	private void makeButtonSection() {
		HBox hbox = new HBox();
		hbox.setLayoutX(100);
		hbox.setLayoutY(950);
		
		Button run = new Button("Run");
		Button pause = new Button("Pause");
		log = new Button("Logging");
		Button step = new Button("Step");
		Button info = new Button("Click Me!");
		info.setStyle("-fx-background-color: Orange");
		
		run.setOnAction(e -> timeline.play());
		pause.setOnAction(e -> timeline.pause());
		log.setOnAction(e -> loggingLogicForButton());
		step.setOnAction(e -> controller.stepSim());
		info.setOnAction(e -> giveInfoAboutGUI());
		
		Label stepNLabel = new Label("Step N Times: ");
		stepNLabel.setTextFill(Color.WHITE);
		nTimes = new TextField();
		
		hbox.getChildren().addAll(run, pause, log, step, stepNLabel, nTimes, info);
		hbox.setSpacing(10);
		
		pane.getChildren().add(hbox);
	}
	
	/**
	 * The logic for the logging button (enables/disables logging)
	 * Changes color based on state
	 */
	private void loggingLogicForButton() {
		if (previousLogCall == 0) {
			previousLogCall = 1;
			log.setStyle("-fx-background-color: Cyan");
			controller.enableLogging();
		}
		else {
			previousLogCall = 0;
			log.setStyle("");
			controller.disableLogging();
		}
	}
	
	/**
	 * checks to see if string is convertable to an integer (regex)
	 * @return integer value if convertable, 0 otherwise
	 */
	private int changeStringToInt(String str) {
		if (str.matches("\\d+")) {
			return Integer.parseInt(str);
		}
		return 0;
	}
	
	/**
	 * adds floors to the GUI pane
	 */
	private void addFloorsDisplay() {
		int yVal = 300;
		for (int i = NUM_FLOORS-1; i > 0; i--) {
			pane.getChildren().add(new Rectangle(250, yVal, 600, 10));
			yVal += 100;
		}
	}
	
	/**
	 * ends simulation by pausing timeline and disable logging
	 */
	public void endSimulation() {
		timeline.pause();
		controller.disableLogging();
	}
	
	/**
	 * changes step count when called upon, then plays the timeline (basically restarting it)
	 * @param int newTime (the cycle count replacement)
	 */
	private void changeStepCnt(int newTime) {
		timeline.setCycleCount(newTime);
		timeline.play();
	}
	
	/**
	 * updates the passenger number display by updating the count and
	 * position according to elevator positioning
	 */
	private void updateNumPassengerDisplay() {
		passengers = controller.getNumPassengersOnElevator();
		pane.getChildren().remove(displayPassengers);
		
		displayPassengers.setText(""+passengers);
		displayPassengers.setFont(new Font(32));
		
		displayPassengers.setX(elevatorDisplay.getX()+75);
		displayPassengers.setY(elevatorDisplay.getY()+60);
		pane.getChildren().add(displayPassengers);
	}
	
	/**
	 * uses a simple formula to calculate the new position of elevator based on current floor
	 */
	private void moveElevator() {
		elevatorDisplay.setY(800-((controller.getCurrentFloor()+1)*100));
	}
	
	/**
	 * using the string os passengers for up and down, updating the display of passengers on each floor
	 */
	private void updatePassDisplay() {
		int xCoor = 300;
		int yCoor = 750;
		removePassNodes();
		for (int i = 1; i <= NUM_FLOORS; i++) {
			String up = controller.getFloorNum(i-1).queueString(1);
			for (int j = 0; j < up.length(); j++) {
				Ellipse passDisplay = new Ellipse(xCoor, yCoor, 20, 20);
				passDisplay.setFill(Color.LIMEGREEN);
				pane.getChildren().add(passDisplay);
				passDisplayList.add(passDisplay);
				xCoor+=50;
			}
			String down = controller.getFloorNum(i-1).queueString(-1);
			for (int j = 0; j < down.length(); j++) {
				Ellipse passDisplay = new Ellipse(xCoor, yCoor, 20, 20);
				passDisplay.setFill(Color.LIGHTBLUE);
				pane.getChildren().add(passDisplay);
				passDisplayList.add(passDisplay);
				xCoor+=50;
			}
			xCoor = 300;
			yCoor = yCoor-100;
		}
	}
	
	/**
	 * removes passenger nodes from the GUI so it can reset for next tick
	 */
	private void removePassNodes() {
		for (int i = 0; i < passDisplayList.size(); i++) {
			pane.getChildren().remove(passDisplayList.get(i));
		}
	}
	
	/**
	 * updates the GUI by updating tick count and calling methods to recalculate for current tick
	 * @param int time (stepCnt from building class)
	 */
	public void updateGui(int time) {
		pane.getChildren().remove(t);
		t = new Text(100, 900, "Tick Count: " + time);
		t.setFill(Color.WHITE);
		t.setFont(new Font(18));
		pane.getChildren().add(t);
		
		moveElevator();
		updateNumPassengerDisplay();
		updatePassDisplay();
		changeStepCnt(changeStringToInt(nTimes.getText()));
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main (String[] args) {
		Application.launch(args);
	}

}
