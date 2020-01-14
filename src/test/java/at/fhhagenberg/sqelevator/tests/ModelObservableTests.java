package at.fhhagenberg.sqelevator.tests;

import at.fhhagenberg.sqelevator.model.Elevator;
import at.fhhagenberg.sqelevator.model.ElevatorController;
import at.fhhagenberg.sqelevator.model.Floor;
import at.fhhagenberg.sqelevator.model.observers.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ModelObservableTests {
    private static final Integer ELEVATOR_CAPACITY = 10;
    private static final Integer NUM_ELEVATORS = 3;
    private static final Integer NUM_FLOORS = 4;
    private static final Integer FLOOR_HEIGHT = 5;

    private static final int ELEVATOR_0 = 0;
    private static final int ELEVATOR_1 = 1;
    private static final int ELEVATOR_2 = 2;

    public static final int FLOOR_0 = 0;
    public static final int FLOOR_1 = 1;
    public static final int FLOOR_2 = 2;
    public static final int FLOOR_3 = 3;

    private MockElevator elevatorService;
    private ElevatorController elevatorController;

    @BeforeEach
    public void setup() {
        elevatorService = new MockElevator(NUM_ELEVATORS, NUM_FLOORS, FLOOR_HEIGHT, ELEVATOR_CAPACITY);
        elevatorController = new ElevatorController(elevatorService);
        elevatorController.initialize();
        //elevatorController.setUpdateInterval(1000);   // periodic update not used in tests
        //elevatorController.startPeriodicUpdates();    // -> call updateFromService() manually
    }

    @Test
    void testElevatorObservableUpdate() throws Exception {
        var observer = (Observer<Elevator>) mock(Observer.class);

        var building = elevatorController.getCurrentState();
        var elevator0 = building.getElevator(ELEVATOR_0);
        elevator0.addObserver(observer);

        elevatorService.getElevators().get(ELEVATOR_0).setAcceleration(20);
        elevator0.updateFromService();

        assertEquals(20, elevator0.getAcceleration());
        verify(observer, times(1)).update(elevator0);
    }

    @Test
    void testFloorObservableUpdate() throws Exception {
        var observer = (Observer<Floor>) mock(Observer.class);

        var building = elevatorController.getCurrentState();
        var floor0 = building.getFloor(FLOOR_0);
        floor0.addObserver(observer);

        elevatorService.getFloors().get(FLOOR_0).setDownButtonActive(true);
        floor0.updateFromService();

        assertEquals(true, floor0.isDownButtonActive());
        verify(observer, times(1)).update(floor0);
    }
}
