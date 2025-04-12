import java.util.ArrayList;
import java.util.HashSet;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice.Type getAdvice(Floor maxFloor,Floor minFloor,int curNum, int maxNum, Floor curFloor, boolean dir,
        HashSet<Person> personsInElevator) {

        if (!requestTable.getUpdateRequests().isEmpty()) {
         return Advice.Type.UPDATE;
        }

        if (!requestTable.getScheRequests().isEmpty()) {
            return Advice.Type.SCHE;
        }

        if (ableToIn(maxFloor,minFloor, curNum, maxNum, curFloor, dir) ||
                ableToOut(curFloor, personsInElevator,maxFloor,minFloor)) {
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
                if (hasSameDir(curFloor, dir, maxFloor, minFloor)) {
                    return Advice.Type.MOVE;
                } else {
                    return Advice.Type.TURN;
                }
            }
        }
    }
    public boolean ableToIn(Floor maxFloor,Floor minFloor, int curNum, int maxNum
            , Floor curFloor, boolean dir) {
        synchronized (requestTable) {
            if (curNum < maxNum && requestTable.getRequestMap().containsKey(curFloor)) {
                for (Person person : requestTable.getRequestMap().get(curFloor)) {
                    if (person.needIn(curFloor, dir,maxFloor,minFloor)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    public boolean ableToOut(Floor curFloor, HashSet<Person> personsInElevator
            ,Floor maxFloor, Floor minFloor) {
        for (Person person : personsInElevator) {
            if (person.getToFloor().equals(curFloor)) {
                return true;
            }
            if(curFloor.equals(maxFloor)&&
            person.getToFloor().ordinal()>curFloor.ordinal()) {
                return true;
            }
            if (curFloor.equals(minFloor)&&
            person.getToFloor().ordinal()<curFloor.ordinal()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSameDir(Floor curFloor, boolean dir, Floor maxFloor, Floor minFloor) {
        synchronized (requestTable) {
            ArrayList<Person> persons = requestTable.getPersonRequests();
            for (Person person : persons) {
                if (person.willIn(curFloor, dir, maxFloor,minFloor)) {
                    return true;
                }

            }
            return false;
        }

    }
}
