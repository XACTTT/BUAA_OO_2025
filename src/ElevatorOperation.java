import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Thread.sleep;

public class ElevatorOperation implements Runnable {
    private final RequestTable requestTable;
    private int id;
    private int maxNum = 6;
    private int curNum = 0; // 当前人数
    private double speed = 400.0;
    private volatile boolean couldReceive;
    private boolean isInSche = false;
    private Floor curFloor = Floor.F1; //当前楼层
    private Floor maxFloor = Floor.F7;
    private Floor minFloor = Floor.B4;
    private boolean isUpdated = false;
    private boolean inUpdate = false;
    private boolean dir = true; // true表示往上
    private Strategy strategy;
    private HashSet<Person> personsInElevator;
    private ShareData shareData;
    private Floor sharedFloor;
    private boolean isUp;

    public ElevatorOperation(int id, RequestTable eleRequestTable) {
        this.id = id;
        this.requestTable = eleRequestTable;
        this.strategy = new Strategy(eleRequestTable);
        this.personsInElevator = new HashSet<>();
        this.couldReceive = true;
        this.shareData = null;
    }

    @Override
    public void run() {
        while (true) {
            //     TimableOutput.println("nowtime22222222222222222222222222222222222222");
            Advice.Type advice = strategy.getAdvice(maxFloor, minFloor, curNum,
                    maxNum, curFloor, dir, personsInElevator);
            if (advice.equals(Advice.Type.SCHE)) {
                sche();
                //TimableOutput.println(id+"receive22222222222222222222ok");
            } else if (advice.equals(Advice.Type.MOVE)) {
                move(dir, speed);
            } else if (advice.equals(Advice.Type.WAIT)) {
                requestTable.waitForPerson();
            } else if (advice.equals(Advice.Type.END)) {
                //              TimableOutput.println("nowtime"+id);
                break;
            } else if (advice.equals(Advice.Type.TURN)) {
                this.dir = !this.dir;
            } else if (advice.equals(Advice.Type.OPEN)) {
                exchangePerson();
            } else if (advice.equals(Advice.Type.UPDATE)) {
                this.update();
            }
            //        TimableOutput.println("nowtime11111111111111111111111111111111111111");
        }


    }

    private void move(boolean dir, double speed) {
        int floorIndex = curFloor.ordinal();
        int maxFloorIndex = maxFloor.ordinal();
        int minFloorIndex = minFloor.ordinal();
        Floor[] floors = Floor.values();
        Floor targetFloor;
        if (dir) {
            if (floorIndex < maxFloorIndex) {
                targetFloor = floors[floorIndex + 1];
            } else {
                targetFloor = floors[floorIndex - 1];
                this.dir = false;
            }
        } else {
            if (floorIndex > minFloorIndex) {
                targetFloor = floors[floorIndex - 1];
            } else {
                targetFloor = floors[floorIndex + 1];
                this.dir = true;
            }
        }
        if (this.isUpdated) {
            //TimableOutput.println(this.dir);
            //TimableOutput.println(personsInElevator.size());
            if (shareData.isShareFloor(targetFloor)) {
                try {
                    shareData.enterShareFloor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        try {
            sleep((long) speed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        curFloor = targetFloor;
        TimableOutput.println(String.format("ARRIVE-%s-%d", curFloor.name(), id));

        if (this.isUpdated && shareData.isShareFloor(curFloor)) {
            exchangePerson();
            curFloor = floors[floorIndex];
            this.dir = !this.dir;
            try {
                sleep((long) speed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println(String.format("ARRIVE-%s-%d", curFloor.name(), id));
            shareData.exitShareFloor();
        }


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
        synchronized (this) {
            return couldReceive;
        }
    }

    public boolean inSche() {
        synchronized (this) {
            return isInSche;
        }
    }

    public boolean inUpdate() {
        synchronized (this) {
            return inUpdate;
        }
    }

    private void out() {
        TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
        ArrayList<Person> outPersons = new ArrayList<>();
        ArrayList<Person> f1Persons = new ArrayList<>();
        for (Person person : personsInElevator) {
            if (person.getToFloor().equals(curFloor)) {
                outPersons.add(person);
            }
            if (curFloor.equals(sharedFloor)) {
                if (isUp && person.getToFloor().ordinal() < curFloor.ordinal()) {
                    f1Persons.add(person);
                } else if (!isUp && person.getToFloor().ordinal() > curFloor.ordinal()) {
                    f1Persons.add(person);
                }
            }
        }
        for (Person person : outPersons) {
            TimableOutput.println(String.format("OUT-S-%d-%s-%d", person.getPersonId(),
                    curFloor.name(), id));
            curNum--;
            personsInElevator.remove(person);
        }

        for (Person person : f1Persons) {
            TimableOutput.println(String.format("OUT-F-%d-%s-%d", person.getPersonId(),
                    curFloor.name(), id));
            curNum--;
            person.setFromFloor(curFloor);
            personsInElevator.remove(person);
        }
        Scheduler.getMasterRequestTable().returnPerson(f1Persons);
    }

    private void in() {
        synchronized (requestTable) {
            if (requestTable.getRequestMap().containsKey(curFloor)) {
                ArrayList<Person> inPersons = new ArrayList<>();
                if (curNum < maxNum) {
                    for (Person person : requestTable.getRequestMap().get(curFloor)) {
                        //TimableOutput.println(person.getPersonId());
                        //TimableOutput.println(person.getFromFloor().name());
                        //TimableOutput.println(person.getToFloor().name());
                        if (person.needIn(curFloor, dir, maxFloor, minFloor)) {
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

    private void sche() {
        synchronized (this) {
            couldReceive = false;
            isInSche = true; // 标记进入临时调度
        }
        ScheRequest scheRequest = requestTable.getScheRequests().get(0);
        String toFloor = scheRequest.getToFloor();
        Floor floor = Floor.valueOf(toFloor);

        int curFloorNum = curFloor.ordinal();
        int toFloorNum = floor.ordinal();
        dir = curFloorNum < toFloorNum;
        try {
            sleep(11);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        double speed = scheRequest.getSpeed() * 1000;
        TimableOutput.println(String.format("SCHE-BEGIN-%d", id));
        while (!curFloor.equals(floor)) {
            move(dir, speed);
        }

        flush();//将电梯候乘表清空
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloor.name(), id));
        TimableOutput.println(String.format("SCHE-END-%d", id));
        synchronized (this) {
            couldReceive = true;
            isInSche = false; // 标记进入临时调度
        }
    }

    private void flush() {
        ArrayList<Person> notArrivePersons = placePeopleInEle(0);
        notArrivePersons.addAll(requestTable.getPersonRequests());
        Scheduler.getMasterRequestTable().returnPerson(notArrivePersons);
        requestTable.clearAll();
    }

    private ArrayList<Person> placePeopleInEle(int type) {
        // 0:sche,1Upd.
        ArrayList<Person> notArrivePersons = new ArrayList<>();
        ArrayList<Person> arrivePersons = new ArrayList<>();
        if (type == 0) {
            TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
        }
        if (!personsInElevator.isEmpty()) {
            if (type == 1) {
                TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
            }

            for (Person person : personsInElevator) {
                if (!person.getToFloor().equals(curFloor)) {
                    notArrivePersons.add(person);
                } else {
                    arrivePersons.add(person);
                }
                curNum--;
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

        }
        return notArrivePersons;
    }

    public void addShareData(ShareData shareData) {
        this.shareData = shareData;
    }

    private void update() {
        synchronized (this) {
            inUpdate = true;
        }

        UpdateRequest updateRequest = requestTable.getUpdateRequests().get(0);
        int id1 = updateRequest.getElevatorAId();
        int id2 = updateRequest.getElevatorBId();
        Floor targetFloor = Floor.valueOf(updateRequest.getTransferFloor());
        sharedFloor = targetFloor;
        Floor[] floors = Floor.values();
        ArrayList<Person> putBackPersons = prepareUpdate();
        int targetFloorNum = targetFloor.ordinal();
        if (this.id == id1) {
            this.minFloor = targetFloor;
            curFloor = floors[targetFloorNum + 1];
            isUp = true;
        } else if (this.id == id2) {
            this.maxFloor = targetFloor;
            curFloor = floors[targetFloorNum - 1];
            isUp = false;
        }
        try {
            this.shareData.isReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Person person : putBackPersons) {
            //TimableOutput.println(person.toString());
            //TimableOutput.println(person.getPersonId());
        }
        if (putBackPersons.size() == 0) {
            //   TimableOutput.println("ZERO!!!!!!");
        }
        Scheduler.getMasterRequestTable().returnPerson(putBackPersons);
        requestTable.clearAll();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            this.shareData.updateEnd();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (this) {
            inUpdate = false;
            isUpdated = true;
            speed = 200.0;
        }
    }

    private ArrayList<Person> prepareUpdate() {
        ArrayList<Person> notArrivePersons = new ArrayList<>();
        if (!personsInElevator.isEmpty()) {
            notArrivePersons = placePeopleInEle(1);
            try {
                sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println(String.format("CLOSE-%s-%d", curFloor.name(), id));
        }
        synchronized (requestTable.getPersonRequests()) {
            notArrivePersons.addAll(requestTable.getPersonRequests());
        }
        return notArrivePersons;
    }

    public boolean canReceivePerson(Person person) {
        int maxFloorNum = maxFloor.ordinal();
        int minFloorNum = minFloor.ordinal();
        int fromFloorNum = person.getFromFloor().ordinal();
        int toFloorNum = person.getToFloor().ordinal();
        if (isUpdated) {
            if (fromFloorNum > maxFloorNum || fromFloorNum < minFloorNum) {
                return false;
            }
            if (isUp && fromFloorNum == minFloorNum) {
                return toFloorNum >= minFloorNum;
            }
            if (!isUp && fromFloorNum == maxFloorNum) {
                return toFloorNum <= maxFloorNum;
            }
        }
        return true;

    }

    public boolean isEnd() {
        return requestTable.isEmpty() && personsInElevator.isEmpty();
    }

    public Floor getCurFloor() {
        return curFloor;
    }

    public boolean getDir() {
        return dir;
    }

    public int getCurNum() {
        return curNum;
    }

    public int waittingSize() {
        return requestTable.getPersonRequests().size();
    }

}
