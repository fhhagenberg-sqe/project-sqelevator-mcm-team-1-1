package at.fhhagenberg.sqelevator.gui;

import java.util.ArrayList;
import java.util.ResourceBundle;

import at.fhhagenberg.sqelevator.viewmodel.BuildingViewModel;
import at.fhhagenberg.sqelevator.viewmodel.ElevatorViewModel;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.converter.NumberStringConverter;

/**
 * The Elevator Panel contains visualizations and controls for all the different elevators and their containing data as well as visualizers for the floor buttons of the building
 */
public class ElevatorPanel extends HBox {

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("elevatorCC");
	private static final int SPACING_5 = 5;
	private static final int SPACING_15 = 15;

	private BuildingViewModel buildingViewModel;


	/**
	 * Constructor needs a view model of the class BuildingViewModel to adapt to changes inside the building / floors / elevators
	 * @param buildingViewModel
	 */
	public ElevatorPanel(BuildingViewModel buildingViewModel) {
		this.buildingViewModel = buildingViewModel;

		buildingViewModel.buildingConfigurationProperty().addListener((observableValue, o, t1) -> buildUI());
	}

	/**
	 * Adds a table containing lights / sliders and labels to visualize all the controls of the elevators.
	 * Furthermore buttons to switch to manual mode to take control of the elevators are added here.
	 */
	private void buildUI() {
		this.getChildren().clear();

		GridPane gridPane = new GridPane();
		gridPane.setId("elevator-grid");

		ColumnConstraints colConstraint = new ColumnConstraints();
		colConstraint.setHalignment(HPos.LEFT);
		colConstraint.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().add(colConstraint);

		int floorNum = buildingViewModel.getFloorViewModels().size();
		int elevatorNum = buildingViewModel.getElevatorViewModels().size();

		if (floorNum == 0 || elevatorNum == 0) {
			this.getChildren().add(
					new Label(String.format(RESOURCE_BUNDLE.getString("no_floor_elevators"), floorNum, elevatorNum)));
			return;
		}

		for (int i = 0; i < floorNum; i++) {
			RowConstraints rowConstraint = new RowConstraints();
			rowConstraint.setValignment(VPos.CENTER);
			rowConstraint.setVgrow(Priority.ALWAYS);
			gridPane.getRowConstraints().add(rowConstraint);
		}

		ArrayList<Slider> liftSliders = new ArrayList<>();

		for (int i = 0; i < elevatorNum; i++) {
			var elevatorViewModel = buildingViewModel.getElevatorViewModels().get(i);

			var sliderCol = new ColumnConstraints();
			sliderCol.setHalignment(HPos.CENTER);
			sliderCol.setHgrow(Priority.ALWAYS);
			gridPane.getColumnConstraints().add(sliderCol);

			var nameAndDirectionIndicator = buildNameAndDirectionIndicators(elevatorViewModel, i);
			gridPane.add(nameAndDirectionIndicator, i + 1, 0);

			var hBox = new HBox();

			var slider = new Slider(0.0, floorNum - 1.0, 0.0);
			slider.setShowTickMarks(true);
			slider.setMax((double) floorNum - 1);
			slider.setMin(0);
			slider.setDisable(true); // slider only use for visualization, not for controlling the elevator
			slider.valueProperty().bind(elevatorViewModel.currentFloorProperty());
			liftSliders.add(slider);

			hBox.getChildren().add(slider);
			hBox.getChildren().add(buildFloorButtons(elevatorViewModel, floorNum, i));
			gridPane.add(hBox, i + 1, 1, 1, floorNum);

			var manualToggle = new ToggleButton("M");
			manualToggle.setId("M" + i);
			manualToggle.disableProperty().bindBidirectional(buildingViewModel.enableEditModeProperty());
			manualToggle.selectedProperty().bindBidirectional(elevatorViewModel.manualModeProperty());

			gridPane.add(manualToggle, i + 1, floorNum + 1);
			addLabels(gridPane, elevatorViewModel, floorNum, i);
		}

		ColumnConstraints lightCol = new ColumnConstraints();
		lightCol.setHalignment(HPos.LEFT);
		gridPane.getColumnConstraints().add(lightCol);

		ColumnConstraints floorNumCol = new ColumnConstraints();
		floorNumCol.setHalignment(HPos.LEFT);
		gridPane.getColumnConstraints().add(floorNumCol);

		var floorsHeading = new Label(RESOURCE_BUNDLE.getString("floors"));
		gridPane.add(floorsHeading, elevatorNum + 2, 0);
		GridPane.setColumnSpan(floorsHeading, 2);

		for (int j = 0; j < floorNum; j++) {
			gridPane.add(buildFloorButtons(j), elevatorNum + 2, j + 1);

			Label floorNumLabel = new Label((floorNum - j) + "");
			floorNumLabel.setOnMouseReleased(new TargetFloorSelectionEventHandler());
			gridPane.add(floorNumLabel, elevatorNum + 3, j + 1);
		}

		addLabelDescriptions(gridPane, floorNum);

		gridPane.prefWidthProperty().bind(this.widthProperty());
		gridPane.prefHeightProperty().bind(this.heightProperty());

		VBox.setVgrow(this, Priority.ALWAYS);

		this.getChildren().add(gridPane);
	}


	/**
	 * Adds labels for the payload-, speed-, target- and door statuses
	 * @param gridPane Grid to add the labels to
	 * @param elevatorViewModel ElevatorViewModel to bind the labels to
	 * @param floorNum Number of floors in the building
	 * @param currentElevator The elevator number of which the labels should be added (Method is called for each elevator)
	 */
	private void addLabels(final GridPane gridPane, final ElevatorViewModel elevatorViewModel, final int floorNum,
			final int currentElevator) {
		Label payload = buildLabel("p" + currentElevator);
		payload.textProperty().bindBidirectional(elevatorViewModel.weightProperty(), new NumberStringConverter());
		gridPane.add(payload, currentElevator + 1, floorNum + 2);

		Label speed = buildLabel("s" + currentElevator);
		speed.textProperty().bindBidirectional(elevatorViewModel.speedProperty(), new NumberStringConverter());
		gridPane.add(speed, currentElevator + 1, floorNum + 3);

		Label target = buildLabel("t" + currentElevator);
		target.textProperty().bindBidirectional(elevatorViewModel.targetFloorTextProperty());
		gridPane.add(target, currentElevator + 1, floorNum + 4);

		Label doors = buildLabel("d" + currentElevator);
		doors.textProperty().bindBidirectional(elevatorViewModel.doorStatusTextProperty());
		gridPane.add(doors, currentElevator + 1, floorNum + 5);
	}


	/**
	 * Creates Gridpane with floor buttons to visualize where the elevator can go / is called to
	 * @param elevatorViewModel ElevatorViewModel to obtain the data of the current elevator
	 * @param floorNum Number of floors to create the correct number of buttons
	 * @param currentElevator Current Elevator to add the buttons to (Method is called for each elevator)
	 * @return Gridpane with the floor buttons.
	 */
	private GridPane buildFloorButtons(final ElevatorViewModel elevatorViewModel, final int floorNum,
			final int currentElevator) {
		GridPane buttons = new GridPane();

		for (int i = 0; i < floorNum; i++) {
			var floorIdReverse = (floorNum - i - 1);
			var elevatorLight = new Group();
			elevatorLight.setId(currentElevator + "," + floorIdReverse);

			var innerCircle = new Circle();
			innerCircle.setRadius(6);
			innerCircle.setFill(Color.YELLOW);
			innerCircle.fillProperty().bind(Bindings.when(elevatorViewModel.floorbuttonActiveProperty(floorIdReverse))
					.then(Color.GREEN).otherwise(Color.YELLOW));
			var outerCircle = new Circle();
			outerCircle.setRadius(8);
			outerCircle.fillProperty().bind(Bindings.when(elevatorViewModel.manualModeProperty()).then(Color.ORANGE)
					.otherwise(Color.LIGHTGRAY));
			elevatorLight.getChildren().addAll(outerCircle, innerCircle);

			elevatorLight.disableProperty().bind(elevatorViewModel.manualModeProperty().not());
			elevatorLight.visibleProperty().bind(elevatorViewModel.servicedfloorActiveProperty(floorIdReverse)
					.and(buildingViewModel.enableEditModeProperty().not())); // hide button if elevator does not service
																				// this floor
			elevatorLight.managedProperty().bind(elevatorLight.visibleProperty());
			elevatorLight.setOnMouseReleased(new TargetFloorSelectionEventHandler());

			var servicesFloorCheckbox = new CheckBox();
			servicesFloorCheckbox.setId("sfc" + currentElevator + "," + floorIdReverse);
			servicesFloorCheckbox.selectedProperty()
					.bindBidirectional(elevatorViewModel.servicedfloorActiveProperty(floorIdReverse));
			servicesFloorCheckbox.visibleProperty().bind(buildingViewModel.enableEditModeProperty());
			servicesFloorCheckbox.managedProperty().bind(servicesFloorCheckbox.visibleProperty());

			var lightCheckboxGroup = new Group();
			lightCheckboxGroup.getChildren().addAll(elevatorLight, servicesFloorCheckbox);
			buttons.add(lightCheckboxGroup, 0, i);

			RowConstraints buttonrow = new RowConstraints();
			buttonrow.setValignment(VPos.CENTER);
			buttonrow.setVgrow(Priority.ALWAYS);
			buttons.getRowConstraints().add(buttonrow);
		}
		return buttons;
	}


	/**
	 * Creates a HorizontalBox with indicators that show where the elevator is currently heading towards and a label for the elevator number.
	 * @param elevatorViewModel ElevatorViewModel to obtain the data of the current elevator.
	 * @param currentElevator Current Elevator to add the buttons to (Method is called for each elevator)
	 * @return The horizontal box with the created indicators and label.
	 */
	private HBox buildNameAndDirectionIndicators(final ElevatorViewModel elevatorViewModel, final int currentElevator) {
		var directionIndicators = new VBox();
		directionIndicators.setSpacing(SPACING_5);

		var directionProperty = elevatorViewModel.currentDirectionProperty();
		var directionTriangleUp = newUpTriangle();
		directionTriangleUp.setId("ebu,"+currentElevator);
		directionTriangleUp.fillProperty()
				.bind(Bindings.when(directionProperty.isEqualTo(ElevatorViewModel.ELEVATOR_DIRECTION_UP))
						.then(Color.GREEN).otherwise(Color.LIGHTGRAY));
		directionIndicators.getChildren().add(directionTriangleUp);

		var directionTriangleDown = newDownTriangle();
		directionTriangleDown.setId("ebd," +currentElevator);
		directionTriangleDown.fillProperty()
				.bind(Bindings.when(directionProperty.isEqualTo(ElevatorViewModel.ELEVATOR_DIRECTION_DOWN))
						.then(Color.GREEN).otherwise(Color.LIGHTGRAY));
		directionIndicators.getChildren().add(directionTriangleDown);

		var elevatorNumLabel = new Label((currentElevator + 1) + "");

		var nameAndDirectionIndicator = new HBox();
		nameAndDirectionIndicator.setSpacing(SPACING_15);
		nameAndDirectionIndicator.getChildren().addAll(elevatorNumLabel, directionIndicators);
		return nameAndDirectionIndicator;
	}


	/**
	 * Create label with provided string as id
	 * @param id
	 * @return Label
	 */
	private Label buildLabel(final String id) {
		Label label = new Label("-");
		label.setId(id);
		label.setMinWidth(70);
		label.setMaxWidth(70);
		return label;
	}

	/**
	 * Creates Vertical Box with buttons that indicate the elevator speed for the current elevator
	 * @param currentElevator number of current elevator (method is called for each elevator)
	 * @return Vertical Box with the created buttons
	 */
	private VBox buildFloorButtons(final int currentFloor) {
		VBox buttons = new VBox();
		buttons.setSpacing(SPACING_5);

		Polygon triangleUp = newUpTriangle();
		triangleUp.setId("fbu," + currentFloor);
		triangleUp.fillProperty()
				.bind(Bindings
						.when(buildingViewModel.getFloorViewModels().get(currentFloor).upButtonActiveProperty())
						.then(Color.DODGERBLUE).otherwise(Color.LIGHTBLUE));
		buttons.getChildren().add(triangleUp);

		Polygon triangleDown = newDownTriangle();
		triangleDown.setId("fbd,"+ currentFloor);
		triangleDown.fillProperty()
				.bind(Bindings
						.when(buildingViewModel.getFloorViewModels().get(currentFloor).downButtonActiveProperty())
						.then(Color.DODGERBLUE).otherwise(Color.LIGHTBLUE));
		buttons.getChildren().add(triangleDown);
		return buttons;
	}


	/**
	 * Adds description labels to the provided gridpane
	 * @param gridPane Grid Pane to add the description to
	 * @param floorNum Number of floors to add the labels at the correct location
	 */
	private void addLabelDescriptions(final GridPane gridPane, final int floorNum) {
		Label manualModeLabel = new Label(RESOURCE_BUNDLE.getString("manualMode"));
		gridPane.add(manualModeLabel, 0, floorNum + 1);

		Label payloadLabel = new Label(RESOURCE_BUNDLE.getString("payload"));
		gridPane.add(payloadLabel, 0, floorNum + 2);

		Label speedLabel = new Label(RESOURCE_BUNDLE.getString("speed"));
		gridPane.add(speedLabel, 0, floorNum + 3);

		Label targetsLabel = new Label(RESOURCE_BUNDLE.getString("target"));
		gridPane.add(targetsLabel, 0, floorNum + 4);

		Label doorsLabel = new Label(RESOURCE_BUNDLE.getString("doors"));
		gridPane.add(doorsLabel, 0, floorNum + 5);
	}


	/**
	 * Class to handle the events of the manual mode.
	 */
	private class TargetFloorSelectionEventHandler implements EventHandler<Event> {

		@Override
		public void handle(Event event) {
			String text = ((Group) event.getSource()).getId();

			int elevatorId = Integer.parseInt(text.substring(0, text.indexOf(',')));
			int floorId = Integer.parseInt(text.substring(text.indexOf(',') + 1));

			var elevator = buildingViewModel.getElevatorViewModels().get(elevatorId);

			if (!elevator.isManualMode()) {
				return;
			}

			elevator.setTargetAndDirection(floorId);

			buildingViewModel
					.setCallInfo(String.format(RESOURCE_BUNDLE.getString("callinfo"), elevatorId + 1, floorId + 1));
		}
	}


	/**
	 * Creates a Triangle Polygon for arrow indicators
	 * @return Polygon arrow
	 */
	private Polygon newUpTriangle() {
		Polygon triangleUp = new Polygon();
		triangleUp.getPoints().addAll(0.0, 0.0, 5.0, -9.0, 10.0, 0.0);
		return triangleUp;
	}

	/**
	 * Creates a Triangle Polygon for arrow indicators
	 * @return Polygon arrow
	 */
	private Polygon newDownTriangle() {
		Polygon triangleDown = new Polygon();
		triangleDown.getPoints().addAll(0.0, 0.0, 5.0, 9.0, 10.0, 0.0);
		return triangleDown;
	}
}
