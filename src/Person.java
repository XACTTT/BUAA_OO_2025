public class Person {
    private final int personId;
    private final int priority;
    private final Floor fromFloor;
    private final Floor toFloor;
    private final int elevatorId;

    public Person(int id, int priority, Floor fromFloor, Floor toFloor, int elevatorId) {
        this.personId = id;
        this.priority = priority;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.elevatorId = elevatorId;

    }

    public Floor getFromFloor() {
        return this.fromFloor;
    }

    public Floor getToFloor() {
        return this.toFloor;
    }

    public int getPersonId() {
        return this.personId;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public boolean needIn(Floor curFloor, boolean dir) {
        int curFloorNum = curFloor.ordinal();
        int toFloorNum = this.toFloor.ordinal();
        int subNum = toFloorNum - curFloorNum;
        boolean sign = (dir && (subNum > 0)) || (!dir && (subNum < 0));
        return curFloor.equals(this.fromFloor) && sign;
    }

    public boolean willIn(Floor curFloor, boolean dir) {
        Floor[] floors = Floor.values();
        int curFloorNum = curFloor.ordinal();
        int fromFloorNum = this.fromFloor.ordinal();
        if (dir) {
            return curFloorNum < fromFloorNum;
        } else {
            return curFloorNum > fromFloorNum;
        }
    }
}
