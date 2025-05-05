import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private int id;
    private HashMap<Integer, Person> persons = new HashMap<>();
    private int valueSum;
    public Tag(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean equals(Tag tag) {
        if (tag != null) {
            return this.id == tag.getId();
        } else {
            return false;
        }
    }

    @Override
    public void addPerson(PersonInterface person) {
        if (!hasPerson(person)) {
            persons.put(person.getId(), (Person) person);
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        int sum = 0;//todo dongtai
        for (Person person : persons.values()) {
            for (Person person2 : persons.values()) {
                if(person.isLinked(person2)){
                    sum += person.queryValue(person2);
                }
            }
        }
        return sum;
    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        } else {
            int sum = 0;
            for (Person person : persons.values()) {
                sum += person.getAge();
            }
            return sum / persons.size();
        }
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        } else {
            int sum = 0;
            for (Person person : persons.values()) {
                sum += (person.getAge() - getAgeMean()) * (person.getAge() - getAgeMean());
            }
            return sum / persons.size();
        }
    }

    @Override
    public void delPerson(PersonInterface person) {
        if (hasPerson(person)) {
            persons.remove(person.getId());
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }
}
