import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//import com.oocourse.elevator2.TimableOutput;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

public class RequestTable {
    private boolean isEnd;
    private HashMap<Floor, HashSet<Person>> personRequestMap;
    private ArrayList<Person> personRequests;
    private ArrayList<ScheRequest> scheRequests;
    private ArrayList<UpdateRequest> updateRequests;

    public RequestTable() {
        isEnd = false;
        personRequestMap = new HashMap<>();
        personRequests = new ArrayList<>();
        scheRequests = new ArrayList<>();
        updateRequests = new ArrayList<>();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return personRequestMap.isEmpty() && personRequests.isEmpty() && scheRequests.isEmpty();
    }

    public synchronized ArrayList<Person> getPersonRequests() {
        return personRequests;
    }

    public synchronized HashMap<Floor, HashSet<Person>> getRequestMap() {
        notifyAll();
        return personRequestMap;
    }

    public synchronized ArrayList<ScheRequest> getScheRequests() {
        notifyAll();
        return scheRequests;
    }

    public synchronized ArrayList<UpdateRequest> getUpdateRequests() {
        notifyAll();
        return updateRequests;
    }

    public synchronized void addPersonRequest(Person person) {
        personRequests.add(person);
        if (personRequestMap.containsKey(person.getFromFloor())) {
            personRequestMap.get(person.getFromFloor()).add(person);
        } else {
            HashSet<Person> persons = new HashSet<>();
            persons.add(person);
            personRequestMap.put(person.getFromFloor(), persons);
        }
        notifyAll();
    }

    public synchronized void addScheRequest(ScheRequest sche) {
        scheRequests.add(sche);
        notifyAll();
    }

    public synchronized void addUpdateRequest(UpdateRequest update) {
        updateRequests.add(update);
        notifyAll();
    }

    public synchronized void removeEleRequest(Person person) {
        personRequests.remove(person);
        if (personRequestMap.containsKey(person.getFromFloor())) {
            personRequestMap.get(person.getFromFloor()).remove(person);

            if (personRequestMap.get(person.getFromFloor()).isEmpty()) {
                personRequestMap.remove(person.getFromFloor());
            }
        }
        notifyAll();
    }

    public synchronized Person getRequestFromMasterTable() {
        if (scheRequests.isEmpty() && updateRequests.isEmpty()
                && personRequests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (personRequests.isEmpty()) {
            return null;
        }
        notifyAll();
        Person person = personRequests.get(0);
        personRequestMap.get(person.getFromFloor()).remove(person);
        if (personRequestMap.get(person.getFromFloor()).isEmpty()) {
            personRequestMap.remove(person.getFromFloor());
        }
        return personRequests.remove(0);
    }

    public synchronized ScheRequest getScheRequestFromMasterTable() {
        if (scheRequests.isEmpty() && updateRequests.isEmpty()
                && personRequests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (scheRequests.isEmpty()) {
            return null;
        }
        notifyAll();
        return scheRequests.remove(0);
    }

    public synchronized UpdateRequest getUpdateFromMasterTable() {
        if (scheRequests.isEmpty() && updateRequests.isEmpty()
                && personRequests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (updateRequests.isEmpty()) {
            return null;
        }
        notifyAll();
        return updateRequests.remove(0);
    }

    public synchronized void waitForPerson() {
        try {
            //    TimableOutput.println("nowtime3333333333333333333"+isEnd);
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void returnPerson(ArrayList<Person> person) {
        for (Person p : person) {
            this.addPersonRequest(p);
        }
    }

    public synchronized void clearAll(){
        personRequestMap.clear();
        personRequests.clear();
        scheRequests.clear();
        updateRequests.clear();
    }

    public synchronized ArrayList<Person> getAndClearPersonRequests() {
        ArrayList<Person> copy = new ArrayList<>(personRequests);
        personRequests.clear(); // 清空操作与获取合并为原子操作
        return copy;
    }
}
