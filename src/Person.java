public class Person {
    private final int personId;
    private final int priority;
    private final Floor toFloor;
    private Floor fromFloor;

    public Person(int id, int priority, Floor fromFloor, Floor toFloor) {
        this.personId = id;
        this.priority = priority;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;


    }

    public Floor getFromFloor() {
        return this.fromFloor;
    }

    public void setFromFloor(Floor fromFloor) {
        this.fromFloor = fromFloor;
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

    public boolean needIn(Floor curFloor, boolean dir, Floor maxFloor, Floor minFloor) {
        int curFloorNum = curFloor.ordinal();
        int toFloorNum = this.toFloor.ordinal();
        if (curFloor.equals(maxFloor) && !maxFloor.equals(Floor.F7)) {
            if (toFloorNum > maxFloor.ordinal()) {
                return false;
            } else {
                return true;
            }
        }
        if (curFloor.equals(minFloor) && !minFloor.equals(Floor.B4)) {
            if (toFloorNum < maxFloor.ordinal()) {
                return false;
            }else {
                return true;
            }
        }
        int subNum = toFloorNum - curFloorNum;
        boolean sign = (dir && (subNum > 0)) || (!dir && (subNum < 0));
        return curFloor.equals(this.fromFloor) && sign;
    }

    public boolean willIn(Floor curFloor, boolean dir, Floor maxFloor, Floor minFloor) {
        int curFloorNum = curFloor.ordinal();
        int fromFloorNum = this.fromFloor.ordinal();
        int toFloorNum = this.toFloor.ordinal();
        int maxFloorNum = maxFloor.ordinal();
        int minFloorNum = minFloor.ordinal();
        if (fromFloorNum > maxFloorNum || fromFloorNum < minFloorNum) {
            return false;
        }
        if (fromFloorNum==maxFloorNum && toFloorNum>maxFloorNum) {
            return false;
        }
        if (fromFloorNum==minFloorNum && toFloorNum<minFloorNum) {
            return false;
        }
        if (dir) {
            return curFloorNum < fromFloorNum;
        } else {
            return curFloorNum > fromFloorNum;
        }
    }
}
