import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Thread.sleep;

public class ElevatorOperation implements Runnable {
    private final RequestTable requestTable;
    private int id;
    private int maxNum = 6;
    private int curNum = 0; // 当前人数
    private boolean couldReceive;
    private Floor curFloor = Floor.F1; //当前楼层
    private boolean dir = true; // true表示往上
    private Strategy strategy;
    private HashSet<Person> personsInElevator;

    public ElevatorOperation(int id, RequestTable eleRequestTable) {
            this.id = id;
        this.requestTable = eleRequestTable;
        this.strategy = new Strategy(eleRequestTable);
        this.personsInElevator = new HashSet<>();
        this.couldReceive = true;
    }

    @Override
    public void run() {
        while (true) {
       //     TimableOutput.println("nowtime22222222222222222222222222222222222222");
            Advice.Type advice = strategy.getAdvice(curNum, maxNum, curFloor, dir,
                    personsInElevator);
            if (advice.equals(Advice.Type.SCHE)) {
                couldReceive = false;
                sche();
                couldReceive = true;
            } else if (advice.equals(Advice.Type.MOVE)) {
                move(dir,400.0);
            } else if (advice.equals(Advice.Type.WAIT)) {
                requestTable.waitForPerson();
            } else if (advice.equals(Advice.Type.END)) {
  //              TimableOutput.println("nowtime"+id);
                break;
            } else if (advice.equals(Advice.Type.TURN)) {
                this.dir = !this.dir;
            } else if (advice.equals(Advice.Type.OPEN)) {
                exchangePerson();
            }
    //        TimableOutput.println("nowtime11111111111111111111111111111111111111");
        }


    }

    private void move(boolean dir,double speed) {
        int floorIndex = curFloor.ordinal();

        Floor[] floors = Floor.values();
        if (dir) {
            if (floorIndex < floors.length - 1) {
                curFloor = floors[floorIndex + 1];
            } else {
                curFloor = floors[floorIndex - 1];
                this.dir = false;
            }

        } else {
            if (floorIndex > 0) {
                curFloor = floors[floorIndex - 1];
            } else {
                curFloor = floors[floorIndex + 1];
                this.dir = true;
            }
        }
        try {
            sleep((long)speed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("ARRIVE-%s-%d", curFloor.name(), id));
    }

    public void exchangePerson() {
        out();
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        in();
    }

    public boolean couldReceiveRequest() {
        return couldReceive;
    }
    private void out() {
        TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
        ArrayList<Person> outPersons = new ArrayList<>();
        for (Person person : personsInElevator) {
            if (person.getToFloor().equals(curFloor)) {
                outPersons.add(person);

            }
        }
        for (Person person : outPersons) {
            TimableOutput.println(String.format("OUT-S-%d-%s-%d", person.getPersonId(),
                    curFloor.name(), id));
            curNum--;
            personsInElevator.remove(person);
        }
    }

    private void in() {
        synchronized (requestTable) {
            if (requestTable.getRequestMap().containsKey(curFloor)) {
                ArrayList<Person> inPersons = new ArrayList<>();
                if (curNum < maxNum) {
                    for (Person person : requestTable.getRequestMap().get(curFloor)) {
                        if (person.needIn(curFloor, dir)) {
                            curNum++;
                            personsInElevator.add(person);
                            inPersons.add(person);
                            TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId()
                                    , curFloor.name(), id));
                            if (curNum == maxNum) {
                                break;
                            }
                        }

                    }
                }
                for (Person person : inPersons) {
                    requestTable.removeEleRequest(person);
                }
            }

            TimableOutput.println(String.format("CLOSE-%s-%d", curFloor.name(), id));
        }
    }

    private void sche(){
     ScheRequest scheRequest= requestTable.getScheRequests().get(0);
     String toFloor = scheRequest.getToFloor();
     Floor floor = Floor.valueOf(toFloor);
     double speed =scheRequest.getSpeed()*1000;
        int curFloorNum = curFloor.ordinal();
        int toFloorNum = floor.ordinal();
        dir = curFloorNum < toFloorNum;
        TimableOutput.println(String.format("SCHE-BEGIN-%d",id));
        while (!curFloor.equals(floor)) {
            move(dir,speed);
        }

        flush();//将电梯候乘表清空
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloor.name(), id));
        TimableOutput.println(String.format("SCHE-END-%d",id));
        requestTable.getScheRequests().remove(scheRequest);
    }
    private void flush() {
        TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
        ArrayList<Person> notArrivePersons = new ArrayList<>();
        ArrayList<Person> arrivePersons = new ArrayList<>();
        for (Person person : personsInElevator) {
            if (!person.getToFloor().equals(curFloor)) {
                notArrivePersons.add(person);
            }else {
                arrivePersons.add(person);
            }
            curNum--;
        }
        for (Person person :requestTable.getPersonRequests()){
            notArrivePersons.add(person);

        }
        for (Person person : arrivePersons) {
            TimableOutput.println(String.format("OUT-S-%d-%s-%d", person.getPersonId(),
                             curFloor.name(), id));
            personsInElevator.remove(person);
        }

        for (Person person : notArrivePersons) {

            TimableOutput.println(String.format("OUT-F-%d-%s-%d", person.getPersonId(),
                    curFloor.name(), id));
            person.setFromFloor(curFloor);
            personsInElevator.remove(person);
        }
        Scheduler.getMasterRequestTable().returnPerson(notArrivePersons);
        requestTable.getPersonRequests().clear();
        requestTable.getScheRequests().clear();
        requestTable.getRequestMap().clear();
    }

    public int judgeValues(Person person) {
        Floor floor = person.getFromFloor();
        int curFloorNum = curFloor.ordinal();
        int fromFloorNum = floor.ordinal();
        if (dir&&(curFloorNum - fromFloorNum<0)) {
            return requestTable.getPersonRequests().size()+curNum +fromFloorNum - curFloorNum;
        } else if((!dir)&&(curFloorNum - fromFloorNum>0)) {
            return requestTable.getPersonRequests().size()+curNum +curFloorNum - fromFloorNum;
        }else{
            return -requestTable.getPersonRequests().size()-curNum;
        }
    }

    public boolean isEnd() {
        return requestTable.isEmpty()&&personsInElevator.isEmpty();
    }
}
