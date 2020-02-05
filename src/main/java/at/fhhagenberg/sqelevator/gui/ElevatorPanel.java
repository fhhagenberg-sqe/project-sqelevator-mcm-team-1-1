package at.fhhagenberg.sqelevator.gui;

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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.converter.NumberStringConverter;

import java.util.ArrayList;
import java.util.List;

public class ElevatorPanel extends HBox {
    private BuildingViewModel buildingViewModel;

    private List<Slider> liftSliders;

    public ElevatorPanel(BuildingViewModel buildingViewModel) {
        this.buildingViewModel = buildingViewModel;

        buildingViewModel.buildingConfigurationProperty().addListener((observableValue, o, t1) ->
                buildUI()
        );
    }

    private void buildUI() {
        this.getChildren().clear();

        GridPane gridPane = new GridPane();
        gridPane.setId("elevator-grid");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.LEFT);
        col1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(col1);

        int floorNum = buildingViewModel.getFloorViewModels().size();
        int elevatorNum = buildingViewModel.getElevatorViewModels().size();

        if (floorNum == 0 || elevatorNum == 0) {
            this.getChildren().add(new Label("no elevators or no floors" + floorNum + "--" + elevatorNum));
            return;
        }

        for (int i = 0; i < floorNum; i++) {
            RowConstraints row = new RowConstraints();
            row.setValignment(VPos.CENTER);
            row.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(row);
        }

        liftSliders = new ArrayList<>();

        for (int i = 0; i < elevatorNum; i++) {
            var elevatorI = buildingViewModel.getElevatorViewModels().get(i);

            var sliderCol = new ColumnConstraints();
            sliderCol.setHalignment(HPos.CENTER);
            sliderCol.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(sliderCol);

            var directionIndicators = new VBox();
            directionIndicators.setSpacing(5);

            var directionProperty = elevatorI.currentDirectionProperty();
            var directionTriangleUp = newUpTriangle();
            directionTriangleUp.fillProperty().bind(Bindings.when(directionProperty.isEqualTo(ElevatorViewModel.ELEVATOR_DIRECTION_UP)).then(Color.GREEN).otherwise(Color.LIGHTGRAY));
            directionIndicators.getChildren().add(directionTriangleUp);

            var directionTriangleDown = newDownTriangle();
            directionTriangleDown.fillProperty().bind(Bindings.when(directionProperty.isEqualTo(ElevatorViewModel.ELEVATOR_DIRECTION_DOWN)).then(Color.GREEN).otherwise(Color.LIGHTGRAY));
            directionIndicators.getChildren().add(directionTriangleDown);

            var elevatorNumLabel = new Label((i + 1) + "");

            var nameAndDirectionIndicator = new HBox();
            nameAndDirectionIndicator.setSpacing(15);
            nameAndDirectionIndicator.getChildren().addAll(elevatorNumLabel, directionIndicators);

            gridPane.add(nameAndDirectionIndicator, i + 1, 0);

            var hBox = new HBox();

            var slider = new Slider(0.0, floorNum - 1.0, 0.0);
            slider.setShowTickMarks(true);
            slider.setMax(floorNum - 1);
            slider.setMin(0);
            slider.setDisable(true);    //slider only use for visualisation, not for controlling the elevator
            slider.valueProperty().bind(elevatorI.currentFloorProperty());
            liftSliders.add(slider);

            GridPane buttons = new GridPane();

            for (int j = 0; j < floorNum; j++) {
                var floorIdReverse = (floorNum - j - 1);
                var elevatorLight = new Group();
                elevatorLight.setId(i + "," + floorIdReverse);

                var innerCircle = new Circle();
                innerCircle.setRadius(6);
                innerCircle.setFill(Color.YELLOW);
                innerCircle.fillProperty().bind(Bindings.when(elevatorI.floorbuttonActiveProperty(floorIdReverse)).then(Color.GREEN).otherwise(Color.YELLOW));
                var outerCircle = new Circle();
                outerCircle.setRadius(8);
                outerCircle.fillProperty().bind(Bindings.when(elevatorI.manualModeProperty()).then(Color.ORANGE).otherwise(Color.LIGHTGRAY));
                elevatorLight.getChildren().addAll(outerCircle, innerCircle);

                elevatorLight.disableProperty().bind(elevatorI.manualModeProperty().not());
                elevatorLight.visibleProperty().bind(elevatorI.servicedfloorActiveProperty(floorIdReverse).and(buildingViewModel.enableEditModeProperty().not())); //hide button if elevator does not service this floor
                elevatorLight.managedProperty().bind(elevatorLight.visibleProperty());
                elevatorLight.setOnMouseReleased(new TargetFloorSelectionEventHandler());

                var servicesFloorCheckbox = new CheckBox();
                servicesFloorCheckbox.setId("sfc" + i + "," + floorIdReverse);
                servicesFloorCheckbox.selectedProperty().bindBidirectional(elevatorI.servicedfloorActiveProperty(floorIdReverse));
                servicesFloorCheckbox.visibleProperty().bind(buildingViewModel.enableEditModeProperty());
                servicesFloorCheckbox.managedProperty().bind(servicesFloorCheckbox.visibleProperty());

                var lightCheckboxGroup = new Group();
                lightCheckboxGroup.getChildren().addAll(elevatorLight, servicesFloorCheckbox);
                buttons.add(lightCheckboxGroup, 0, j);

                RowConstraints buttonrow = new RowConstraints();
                buttonrow.setValignment(VPos.CENTER);
                buttonrow.setVgrow(Priority.ALWAYS);
                buttons.getRowConstraints().add(buttonrow);
            }

            hBox.getChildren().add(slider);
            hBox.getChildren().add(buttons);
            gridPane.add(hBox, i + 1, 1, 1, floorNum);

            var manualToggle = new ToggleButton("M");
            manualToggle.setId("M" + i);
            manualToggle.disableProperty().bindBidirectional(buildingViewModel.enableEditModeProperty());
            manualToggle.selectedProperty().bindBidirectional(elevatorI.manualModeProperty());

            gridPane.add(manualToggle, i + 1, floorNum + 1);

            Label payload = new Label("-");
            payload.textProperty().bindBidirectional(elevatorI.weightProperty(), new NumberStringConverter());
            payload.setId("p" + i);
            payload.setMinWidth(70);
            payload.setMaxWidth(70);
            gridPane.add(payload, i + 1, floorNum + 2);

            Label speed = new Label("-");
            speed.textProperty().bindBidirectional(elevatorI.speedProperty(), new NumberStringConverter());
            speed.setId("s" + i);
            speed.setMinWidth(70);
            speed.setMaxWidth(70);
            gridPane.add(speed, i + 1, floorNum + 3);

            Label target = new Label("-");
            target.textProperty().bindBidirectional(elevatorI.targetFloorTextProperty());
            target.setId("t" + i);
            target.setMinWidth(70);
            target.setMaxWidth(70);
            gridPane.add(target, i + 1, floorNum + 4);

            Label doors = new Label("-");
            doors.textProperty().bindBidirectional(elevatorI.doorStatusTextProperty());
            doors.setId("d" + i);
            doors.setMinWidth(70);
            doors.setMaxWidth(70);
            gridPane.add(doors, i + 1, floorNum + 5);
        }

        ColumnConstraints lightCol = new ColumnConstraints();
        lightCol.setHalignment(HPos.LEFT);
        gridPane.getColumnConstraints().add(lightCol);

        ColumnConstraints floorNumCol = new ColumnConstraints();
        floorNumCol.setHalignment(HPos.LEFT);
        gridPane.getColumnConstraints().add(floorNumCol);

        var floorsHeading = new Label("Floors");
        gridPane.add(floorsHeading, elevatorNum + 2, 0);
        gridPane.setColumnSpan(floorsHeading, 2);

        for (int j = 0; j < floorNum; j++) {
            VBox buttons = new VBox();
            buttons.setSpacing(5);

            Polygon triangleUp = newUpTriangle();
            triangleUp.fillProperty().bind(Bindings.when(buildingViewModel.getFloorViewModels().get(j).upButtonActiveProperty()).then(Color.DODGERBLUE).otherwise(Color.LIGHTBLUE));
            buttons.getChildren().add(triangleUp);

            Polygon triangleDown = newDownTriangle();
            triangleDown.fillProperty().bind(Bindings.when(buildingViewModel.getFloorViewModels().get(j).downButtonActiveProperty()).then(Color.DODGERBLUE).otherwise(Color.LIGHTBLUE));
            buttons.getChildren().add(triangleDown);

            gridPane.add(buttons, elevatorNum + 2, j + 1);

            Label floorNumLabel = new Label((floorNum - j) + "");
            floorNumLabel.setOnMouseReleased(new TargetFloorSelectionEventHandler());
            gridPane.add(floorNumLabel, elevatorNum + 3, j + 1);

        }

        Label manualModeLabel = new Label("Manual");
        gridPane.add(manualModeLabel, 0, floorNum + 1);

        Label payloadLabel = new Label("Payload");
        gridPane.add(payloadLabel, 0, floorNum + 2);

        Label speedLabel = new Label("Speed");
        gridPane.add(speedLabel, 0, floorNum + 3);

        Label targetsLabel = new Label("Target");
        gridPane.add(targetsLabel, 0, floorNum + 4);

        Label doorsLabel = new Label("Doors");
        gridPane.add(doorsLabel, 0, floorNum + 5);

        gridPane.prefWidthProperty().bind(this.widthProperty());
        gridPane.prefHeightProperty().bind(this.heightProperty());

        VBox.setVgrow(this, Priority.ALWAYS);

        this.getChildren().add(gridPane);
    }

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

            buildingViewModel.setCallInfo(String.format("Next target floor for elevator <%s> is %s", elevatorId + 1, floorId + 1));

            // TODO reduce speed for the slider to move slower (as animation for elevator)
        }
    }

    private Polygon newUpTriangle() {
        Polygon triangleUp = new Polygon();
        triangleUp.getPoints().addAll(0.0, 0.0,
                5.0, -9.0,
                10.0, 0.0);

        return triangleUp;
    }

    private Polygon newDownTriangle() {
        Polygon triangleDown = new Polygon();
        triangleDown.getPoints().addAll(0.0, 0.0,
                5.0, 9.0,
                10.0, 0.0);

        return triangleDown;
    }
}
