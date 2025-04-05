import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.oocourse.elevator2.ScheRequest;

public class RequestTable {
    private boolean isEnd;
    private HashMap<Floor, HashSet<Person>> personRequestMap;
    private ArrayList<Person> personRequests;
    private ArrayList<ScheRequest> scheRequests;

    public RequestTable() {
        isEnd = false;
        personRequestMap = new HashMap<>();
        personRequests = new ArrayList<>();
        scheRequests = new ArrayList<>();
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
        notifyAll();
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
        if (personRequests.isEmpty() && !isEnd) {
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
        if (scheRequests.isEmpty() && !isEnd) {
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

    public synchronized void waitForPerson() {
        try {
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
}
