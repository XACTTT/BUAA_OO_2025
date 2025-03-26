import java.util.ArrayList;
import java.util.HashSet;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice.Type getAdvice(int curNum, int maxNum, Floor curFloor, boolean dir, HashSet<Person> personsInElevator) {
        if (ableToIn(curNum, maxNum, curFloor, dir) || ableToOut(curFloor, personsInElevator)) {
            return Advice.Type.OPEN;
        }
        if (curNum != 0) {
            return Advice.Type.MOVE;
        } else {
            if (requestTable.isEmpty()) {
                if (requestTable.isEnd()) {
                    return Advice.Type.END;
                } else {
                    return Advice.Type.WAIT;
                }
            } else {
                if (hasSameDir(curFloor, dir)) {
                    return Advice.Type.MOVE;
                } else {
                    return Advice.Type.TURN;
                }
            }
        }
    }

    public boolean ableToIn(int curNum, int maxNum, Floor curFloor, boolean dir) {
        synchronized (requestTable) {
            if (curNum < maxNum && requestTable.getRequestMap().containsKey(curFloor)) {
                for (Person person : requestTable.getRequestMap().get(curFloor)) {
                    if (person.needIn(curFloor, dir)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    public boolean ableToOut(Floor curFloor, HashSet<Person> personsInElevator) {
        for (Person person : personsInElevator) {
            if (person.getToFloor().equals(curFloor)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSameDir(Floor curFloor, boolean dir) {
        synchronized (requestTable) {
            Floor[] floors = Floor.values();
            int curFloorNum = curFloor.ordinal();

                    ArrayList<Person> persons = requestTable.getPersonRequests();
                    for (Person person : persons) {
                        if (person.willIn(curFloor, dir)) {
                            return true;
                }

            }
            return false;
        }

    }
}
