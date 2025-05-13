import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.*;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private HashMap<Integer, Person> acquaintance = new HashMap<>();
    private HashMap<Integer, Integer> value = new HashMap<>();
    private HashMap<Integer, Tag> tags = new HashMap<>();
    private ArrayList<Integer> receivedArticles = new ArrayList<>();
    private ArrayList<MessageInterface> messages = new ArrayList<>();
    private int money;
    private int socialvalue;
    private int bestValueId = id;
    private int bestValue = Integer.MIN_VALUE;

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

    @Override
    public List<Integer> getReceivedArticles() {
        return receivedArticles;
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        if (receivedArticles.size() < 5) {
            return receivedArticles;
        } else {
            List<Integer> ans = receivedArticles.subList(0, 5);
            return ans;
        }
    }

    @Override
    public void addSocialValue(int num) {
        this.socialvalue += num;
    }

    @Override
    public int getSocialValue() {
        return socialvalue;
    }

    @Override
    public List<MessageInterface> getMessages() {
        return messages;
    }

    @Override
    public List<MessageInterface> getReceivedMessages() {
        if (messages.size() < 5) {
            return messages;
        } else {
            List<MessageInterface> ans = messages.subList(0, 5);
            return ans;
        }
    }

    @Override
    public void addMoney(int num) {
        this.money += num;
    }

    @Override
    public int getMoney() {
        return money;
    }

    public void changeValue(int id, int newValue) {
        value.remove(id);
        value.put(id, newValue);

        if (id == bestValueId) {
            if (newValue > bestValue) {
                bestValue = newValue;
            } else if (newValue < bestValue) {
                buildMaxValueId();
            }
        } else if (newValue > bestValue) {
            bestValue = newValue;
            bestValueId = id;
        } else if (newValue == bestValue && id < bestValueId) {
            bestValueId = id;
        }

    }

    public void delAcquaintance(int id) {
        acquaintance.remove(id);
    }

    public void delValue(int id) {
        value.remove(id);
        if (id == bestValueId) {
            buildMaxValueId();
        }

    }

    public int getAcquaintenceSize() {
        return acquaintance.size();
    }

    public int chooseMaxValueId() {
        return bestValueId;
    }

    public void buildMaxValueId() {
        int max = -Integer.MAX_VALUE;
        int ansId = id;
        for (Integer i : value.keySet()) {
            if (value.get(i) > max) {
                max = value.get(i);
                ansId = i;
            } else if (value.get(i) == max) {
                if (i < ansId) {
                    ansId = i;
                }
            }
        }
        bestValueId = ansId;
        bestValue = max;
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

    public void insertArticle(int id) {
        receivedArticles.add(0, id);
    }

    public void insertMessage(MessageInterface message) {
        messages.add(0, message);
    }

    public void removeArticle(int id) {
        receivedArticles.remove((Integer) id);
    }

}
