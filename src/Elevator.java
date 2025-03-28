import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Thread.sleep;

public class Elevator implements Runnable {
    private int id;
    private int maxNum = 6;
    private int curNum = 0; // 当前人数
    private Floor curFloor = Floor.F1; //当前楼层
    private boolean dir = true; // true表示往上
    private RequestTable requestTable;
    private Strategy strategy;
    private HashSet<Person> personsInElevator;

    public Elevator(int id, RequestTable eleRequestTable) {
        this.id = id;
        this.requestTable = eleRequestTable;
        this.strategy = new Strategy(eleRequestTable);
        personsInElevator = new HashSet<>();
    }

    @Override
    public void run() {
        while (true) {
            Advice.Type advice = strategy.getAdvice(curNum, maxNum, curFloor, dir,
                    personsInElevator);

            if (advice.equals(Advice.Type.END)) {
                break;
            } else if (advice.equals(Advice.Type.MOVE)) {
                move(dir);
            } else if (advice.equals(Advice.Type.WAIT)) {
                requestTable.waitForPerson();
            } else if (advice.equals(Advice.Type.TURN)) {
                this.dir = !this.dir;
            } else if (advice.equals(Advice.Type.OPEN)) {
                exchangePerson();
            }

        }


    }

    private void move(boolean dir) {
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
            sleep(400);
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

    private void out() {
        TimableOutput.println(String.format("OPEN-%s-%d", curFloor.name(), id));
        ArrayList<Person> outPersons = new ArrayList<>();
        for (Person person : personsInElevator) {
            if (person.getToFloor().equals(curFloor)) {
                outPersons.add(person);
                curNum--;
            }
        }
        for (Person person : outPersons) {
            TimableOutput.println(String.format("OUT-%d-%s-%d", person.getPersonId(),
                    curFloor.name(), id));
            personsInElevator.remove(person);
        }
    }

    private void in() {
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
