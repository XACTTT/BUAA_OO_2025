import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static RequestTable masterRequestTable;
    private final ArrayList<RequestTable> eleRequestTables;
    private ArrayList<ElevatorOperation> elevators;

    public Scheduler(RequestTable masterRequestTable, ArrayList<RequestTable> eleRequestTables, ArrayList<ElevatorOperation> elevators) {
        Scheduler.masterRequestTable = masterRequestTable;
        this.eleRequestTables = eleRequestTables;
        this.elevators = elevators;
    }

    public void run() {
        while (true) {
            if (masterRequestTable.isEmpty() && masterRequestTable.isEnd() ) {
               if(eleAllEnd()) {
     //              TimableOutput.println("nowtime4444444444444444444444444444444444444");
                   for (RequestTable eleRequestTable : eleRequestTables) {
                   eleRequestTable.setEnd();
               }
                   return;
               }
               else {
                 try {
                       sleep(300);
                   }catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
            }

            ScheRequest scheRequest = masterRequestTable.getScheRequestFromMasterTable();

            if (scheRequest != null) {
                scheEle(scheRequest);

            }
            Person person = masterRequestTable.getRequestFromMasterTable();
            if (person == null) {
                continue;
            }
            while (true) {
                int eleId = schedulerAnPerson(person);
                if (eleId != -1) {
                    TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), eleId));
                    eleRequestTables.get(eleId-1).addPersonRequest(person);
                    break;
                }
                try {
                    sleep(1050);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private int schedulerAnPerson(Person person) {
        int availableNum = 0;
        ArrayList<Integer> availableEleId = new ArrayList<>();
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).couldReceiveRequest()) {
                availableNum++;
                availableEleId.add(i+1);
            }
        }
        if (availableNum <= 3) {
            return -1;
        }

        int bestElevatorId = -1;
        int minScore = 7141027;

        for (Integer eleId : availableEleId) {
            ElevatorOperation elevator = elevators.get(eleId-1);
            int score = calculateMatchScore(elevator, person);
            if (score < minScore) {
                minScore = score;
                bestElevatorId = eleId;
            }
        }

        return bestElevatorId;
    }

    private int calculateMatchScore(ElevatorOperation elevator, Person person) {
        Floor fromFloor = person.getFromFloor();
        Floor toFloor = person.getToFloor();
        boolean passengerDirection = fromFloor.ordinal() < toFloor.ordinal();

        Floor curFloor = elevator.getCurFloor();
        boolean elevatorDirection = elevator.getDir();
        int load = elevator.getCurNum();
        int distance = Math.abs(curFloor.ordinal() - fromFloor.ordinal());

        int directionScore = 0;
        if (elevatorDirection == passengerDirection) {
            if ((elevatorDirection && fromFloor.ordinal() >= curFloor.ordinal()) ||
                    (!elevatorDirection && fromFloor.ordinal() <= curFloor.ordinal())) {
                directionScore = 0;
            } else {
                directionScore = 5;
            }
        } else {
            directionScore = 10;
        }

        return distance * 2 + directionScore  + load*3;
    }

    private void scheEle(ScheRequest scheRequest) {
    int id = scheRequest.getElevatorId();
    synchronized (eleRequestTables) {
        ArrayList<Person> people = new ArrayList<>();
        eleRequestTables.get(id-1).addScheRequest(scheRequest);
    }
    }

    private boolean eleAllEnd() {
        for (ElevatorOperation elevatorOperation : elevators) {
            if (!elevatorOperation.isEnd()) {
                return false;
            }
        }
        return true;
    }

    public static RequestTable getMasterRequestTable() {
        return masterRequestTable;
    }
}
