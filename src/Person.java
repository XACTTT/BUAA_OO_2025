import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private HashMap<Integer, Person> acquaintance = new HashMap<>();
    private HashMap<Integer, Integer> value = new HashMap<>();
    private HashMap<Integer, Tag> tags = new HashMap<>();

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public boolean strictEquals(PersonInterface person) {
        if (person instanceof Person) {
            if (((Person) person).id == this.id) {
                if (((Person) person).name.equals(this.name)) {
                    if (((Person) person).age == this.age) {
                        return true;//todo
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    public void addAcquaintance(Person person) {
        if (!acquaintance.containsKey(person.getId())) {
            acquaintance.put(person.getId(), person);
        }
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public TagInterface getTag(int id) {
        return tags.getOrDefault(id, null);
    }

    @Override
    public void addTag(TagInterface tag) {
        if (!tags.containsKey(tag.getId())) {
            tags.put(tag.getId(), (Tag) tag);
        }
    }

    @Override
    public void delTag(int id) {
        tags.remove(id);
    }

    public boolean equals(Person person) {
        if (person == null) {
            return false;
        } else {
            return id == person.getId();
        }
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        return acquaintance.containsKey(person.getId()) ||
                id == person.getId();
    }

    @Override
    public int queryValue(PersonInterface person) {
        if (acquaintance.containsKey(person.getId())) {
            return value.get(person.getId());
        } else {
            return 0;
        }
    }

    public void changeValue(int id, int newValue) {
        value.remove(id);
        value.put(id, newValue);
    }

    public void delAcquaintance(int id) {
        acquaintance.remove(id);
    }

    public void delValue(int id) {
        value.remove(id);
    }

    public int getAcquaintenceSize() {
        return acquaintance.size();
    }

    public int chooseMaxValueId() {
        int max = -Integer.MAX_VALUE;
        int ansId = -1;
        for (Integer i : value.keySet()) {
            if (value.get(i) >= max) {
                max = value.get(i);
                ansId = i;
            }
        }
        return ansId;
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public void delRelatedTags(int personId) {
        for (TagInterface tag : tags.values()) {
            if (acquaintance.containsKey(personId)) {
                if (tag.hasPerson(acquaintance.get(personId))) {
                    tag.delPerson(acquaintance.get(personId));
                }
            }
        }
    }
}
