public class Person {
    private final int personId;
    private final int priority;
    private  Floor fromFloor;
    private final Floor toFloor;

    public Person(int id, int priority, Floor fromFloor, Floor toFloor) {
        this.personId = id;
        this.priority = priority;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;


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

    public boolean needIn(Floor curFloor, boolean dir) {
        int curFloorNum = curFloor.ordinal();
        int toFloorNum = this.toFloor.ordinal();
        int subNum = toFloorNum - curFloorNum;
        boolean sign = (dir && (subNum > 0)) || (!dir && (subNum < 0));
        return curFloor.equals(this.fromFloor) && sign;
    }

    public boolean willIn(Floor curFloor, boolean dir) {
        int curFloorNum = curFloor.ordinal();
        int fromFloorNum = this.fromFloor.ordinal();
        if (dir) {
            return curFloorNum < fromFloorNum;
        } else {
            return curFloorNum > fromFloorNum;
        }
    }

    public void setFromFloor(Floor fromFloor) {
        this.fromFloor = fromFloor;
    }
}
