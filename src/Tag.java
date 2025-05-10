import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private int id;
    private HashMap<Integer, Person> persons = new HashMap<>();
    private int valueSum = 0;

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
            for (Person p : persons.values()) {
                if (p.isLinked(person)) {
                    valueSum += 2 * p.queryValue(person);
                }
            }
            persons.put(person.getId(), (Person) person);

        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return valueSum;
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
            for (Person p : persons.values()) {
                if (p.isLinked(person)) {
                    valueSum -= 2 * p.queryValue(person);
                }
            }
            persons.remove(person.getId());
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    public void changeValueSum(PersonInterface person1, PersonInterface person2,
        int oldValue, int newValue) {
        if (person1.isLinked(person2)) {
            valueSum -= 2 * oldValue;
            valueSum += 2 * newValue;
        }
    }
}
