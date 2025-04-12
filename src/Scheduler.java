import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static RequestTable masterRequestTable;
    private final ArrayList<RequestTable> eleRequestTables;
    private ArrayList<ElevatorOperation> elevators;

    public Scheduler(RequestTable masterRequestTable, ArrayList<RequestTable> eleRequestTables,
        ArrayList<ElevatorOperation> elevators) {
        Scheduler.masterRequestTable = masterRequestTable;
        this.eleRequestTables = eleRequestTables;
        this.elevators = elevators;
    }

    public static RequestTable getMasterRequestTable() {
        return masterRequestTable;
    }

    public void run() {
        while (true) {
            if (masterRequestTable.isEmpty() && masterRequestTable.isEnd()) {
                if (eleAllEnd()) {
                    //TimableOutput.println("nowtime4444444444444444444444444444444444444");
                    for (RequestTable eleRequestTable : eleRequestTables) {
                        eleRequestTable.setEnd();
                    }
                    return;
                } else {
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            UpdateRequest updateRequest = masterRequestTable.getUpdateFromMasterTable();
            if (updateRequest != null) {
                updateEle(updateRequest);
            }

            ScheRequest scheRequest = masterRequestTable.getScheRequestFromMasterTable();

            if (scheRequest != null) {
                //  TimableOutput.println(scheRequest.toString()+"333333333333333");
                scheEle(scheRequest);

            }
            Person person = masterRequestTable.getRequestFromMasterTable();
            ArrayList<Person> persons = new ArrayList<>();
            if (person == null) {
                continue;
            }

            int eleId = schedulerAnPerson(person);
            if (eleId != -1) {
                TimableOutput.println(String.format("RECEIVE-%d-%d",
                        person.getPersonId(), eleId));
                eleRequestTables.get(eleId - 1).addPersonRequest(person);

            } else {
                persons.add(person);
                masterRequestTable.returnPerson(persons);
                try {
                    sleep(150);
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
            if (elevators.get(i).couldReceiveRequest() && !
                    elevators.get(i).inSche() && !
                    elevators.get(i).inUpdate() &&
                    elevators.get(i).waittingSize()<=10
            ) {
                if (elevators.get(i).canReceivePerson(person)) {
                    availableNum++;
                    availableEleId.add(i + 1);
                }
            }
        }
        if (availableNum <= 0) {
            return -1;
        }

        int bestElevatorId = -1;
        int minScore = 7141027;

        for (Integer eleId : availableEleId) {
            ElevatorOperation elevator = elevators.get(eleId - 1);
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
        int load = elevator.getCurNum() + elevator.waittingSize();
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

        return distance * 2 + directionScore + load * 3;
    }

    private void scheEle(ScheRequest scheRequest) {
        int id = scheRequest.getElevatorId();
        synchronized (eleRequestTables) {
            eleRequestTables.get(id - 1).addScheRequest(scheRequest);
            //TimableOutput.println(id + "deliver11111111111111ok");
        }
    }

    private void updateEle(UpdateRequest updateRequest) {
        int id1 = updateRequest.getElevatorAId();
        int id2 = updateRequest.getElevatorBId();
        ShareData shareData = new ShareData(updateRequest);
        synchronized (eleRequestTables) {
            elevators.get(id1-1).addShareData(shareData);
            elevators.get(id2-1).addShareData(shareData);
            eleRequestTables.get(id1-1).addUpdateRequest(updateRequest);
            eleRequestTables.get(id2-1).addUpdateRequest(updateRequest);
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
}
