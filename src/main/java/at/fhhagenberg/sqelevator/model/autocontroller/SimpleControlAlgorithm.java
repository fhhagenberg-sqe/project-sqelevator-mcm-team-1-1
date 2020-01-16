package at.fhhagenberg.sqelevator.model.autocontroller;

import at.fhhagenberg.sqelevator.model.ControlMode;
import at.fhhagenberg.sqelevator.model.Elevator;
import at.fhhagenberg.sqelevator.model.Floor;
import at.fhhagenberg.sqelevator.model.IElevatorController;
import at.fhhagenberg.sqelevator.model.observers.IBuildingInitializedObserver;
import at.fhhagenberg.sqelevator.model.observers.Observable;
import at.fhhagenberg.sqelevator.model.observers.Observer;
import sqelevator.IElevator;

public class SimpleControlAlgorithm implements IControlAlgorithm, IBuildingInitializedObserver {

    private class ElevatorObserver implements Observer<Elevator> {

        private SimpleControlAlgorithm sca;

        public ElevatorObserver(SimpleControlAlgorithm sca) {
            this.sca = sca;
        }

        @Override
        public void update(Observable<Elevator> observable) {

            var elevator = observable.getValue();

            sca.updateElevator(elevator);
        }
    }

    private class FloorObserver implements Observer<Floor> {

        private SimpleControlAlgorithm sca;

        public FloorObserver(SimpleControlAlgorithm sca) {
            this.sca = sca;
        }

        @Override
        public void update(Observable<Floor> observable) {

            var floor = observable.getValue();

            sca.updateFloor(floor);

        }
    }

    private ElevatorObserver elevatorObserver;
    private FloorObserver floorObserver;

    private IElevatorController elevatorController;

    @Override
    public void setElevatorController(IElevatorController elevatorController) {
        this.elevatorController = elevatorController;
    }

    @Override
    public void start() {
        this.elevatorController.addInitializedObserver(this);
    }

    @Override
    public void stop() {
        var building = elevatorController.getCurrentState();

        for (Floor floor : building.getFloors()) {
            floor.removeObserver(floorObserver);
        }
        for (Elevator elevator : building.getElevators()) {
            elevator.removeObserver(elevatorObserver);
        }
    }


    @Override
    public void initializationDone() {
        var building = elevatorController.getCurrentState();

        elevatorObserver = new ElevatorObserver(this);
        floorObserver = new FloorObserver(this);

        for (Floor floor : building.getFloors()) {
            floor.addObserver(floorObserver);
        }

        for (Elevator elevator : building.getElevators()) {
            elevator.addObserver(elevatorObserver);
        }


    }

    public void updateElevator(Elevator elevator) {
        var building = elevatorController.getCurrentState();

        int targetfloor = -1;

        boolean isFloorButtonActive = false;
        for(int i = 0; i < building.getNumFloors(); i++) {
            if(elevator.isFloorButtonActive(i)) {
                isFloorButtonActive = true;
            }
        }

        if(isFloorButtonActive && (elevator.getDoorStatus() == IElevator.ELEVATOR_DOORS_OPEN) && (elevator.getControlMode() == ControlMode.AUTOMATIC) ) {
            for(int i = 0; i < building.getNumFloors(); i++) {
                if(elevator.isFloorButtonActive(i)) {
                    targetfloor = i;
                    elevator.gotoTarget(targetfloor);
                    break;
                }
            }
        }

        if(targetfloor == -1 && isFloorButtonActive) {
            updateFloor(building.getFloor(0)); // Time for a floor button
        }
    }

    public void updateFloor(Floor floor) {
        var building = elevatorController.getCurrentState();
        Elevator targetElevator = null;

        // Iterate through all elevators
        for(Elevator e : building.getElevators()) {
            System.out.println(e.getServicesFloors(floor.getId()));

            // only make use of this elevator when this is a floor that that elevator is servicing and the mode is automatic and no other elevator is sent to this floor
            if(e.getServicesFloors(floor.getId()) && e.getControlMode() == ControlMode.AUTOMATIC && targetElevator == null && e.getDoorStatus() == IElevator.ELEVATOR_DOORS_OPEN) {

                // send this elevator when floor downbutton is active and this elevator is above this floor and going down or the elevator has no direction / not going anywhere
                if (floor.isDownButtonActive() && ((e.getCurrentFloor() > floor.getId() && e.getDirection() == IElevator.ELEVATOR_DIRECTION_DOWN) || (e.getDirection() == IElevator.ELEVATOR_DIRECTION_UNCOMMITTED))) {

                    targetElevator = e;

                    // send this elevator when floor upbutton is active and this elevator is below this floor and going up or the elevator has no direction / not going anywhere
                } else if (floor.isUpButtonActive() && ((e.getCurrentFloor() < floor.getId() && e.getDirection() == IElevator.ELEVATOR_DIRECTION_UP) || (e.getDirection() == IElevator.ELEVATOR_DIRECTION_UNCOMMITTED))) {

                    targetElevator = e;

                }
            }
        }

        // If still no elevator handles this floor then use first available one
        if(targetElevator == null) {

            for(Elevator e : building.getElevators()) {

                if(e.getServicesFloors(floor.getId()) && e.getControlMode() == ControlMode.AUTOMATIC && targetElevator == null && e.getDoorStatus() == IElevator.ELEVATOR_DOORS_OPEN) {

                    if (floor.isDownButtonActive() || floor.isUpButtonActive()) {

                        targetElevator = e;

                    }
                }
            }

        }

        if(targetElevator != null) {
            targetElevator.gotoTarget(floor.getId());
            System.out.println("Sending elevator " + targetElevator.getId() + " to floor " + floor.getId());
        }
        else {
            // floor ignored - is handled by a next elevator event that is free
        }
    }
}
