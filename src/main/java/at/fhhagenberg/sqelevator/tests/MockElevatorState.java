package at.fhhagenberg.sqelevator.tests;

import sqelevator.IElevator;

import java.util.ArrayList;
import java.util.List;

class MockElevatorState {
    private int direction = IElevator.ELEVATOR_DIRECTION_UNCOMMITTED;
    private int acceleration = 0;
    private int doorStatus = IElevator.ELEVATOR_DOORS_CLOSED;
    private int currentFloor = 0;
    private int targetFloor = 0;
    private int speed = 0;
    private int weight = 0;
    private int capacity;

    private List<Boolean> servicedFloors;
    private List<Boolean> floorButtons;

    public MockElevatorState(int numFloors) {
        servicedFloors = new ArrayList<>(numFloors);
        floorButtons = new ArrayList<>(numFloors);

        for (int i = 0; i < numFloors; i++) {
            servicedFloors.add(i, true);
            floorButtons.add(i, false);
        }
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public void setTargetFloor(int targetFloor) {
        this.targetFloor = targetFloor;
    }

    public boolean isFloorButtonActive(int floor) {
        return floorButtons.get(floor);
    }

    public void setFloorButtonActive(int floor, boolean active) {
        floorButtons.set(floor, active);
    }

    public boolean getServicesFloors(int floor) {
        return servicedFloors.get(floor);
    }

    public void setServicesFloors(int floor, boolean service) {
        servicedFloors.set(floor, service);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    public int getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(int doorStatus) {
        this.doorStatus = doorStatus;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}