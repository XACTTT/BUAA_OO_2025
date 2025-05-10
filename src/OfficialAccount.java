import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.HashMap;

public class OfficialAccount implements OfficialAccountInterface {
    private int ownerId;
    private int id;
    private String name;
    private HashMap<Integer, Person> flowers = new HashMap<>();
    private HashMap<Integer,Integer> articles = new HashMap<>();
    private HashMap<Integer, Integer> contributions = new HashMap<>();

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;

    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }

    @Override
    public void addFollower(PersonInterface person) {
        if (!containsFollower(person)) {
            flowers.put(person.getId(), (Person) person);
            // TODO
            contributions.put(person.getId(), 0);//??right?
        }
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return flowers.containsKey(person.getId());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        if (!containsArticle(id)) {
            articles.put(id,id);
            int old = contributions.get(person.getId());
            contributions.remove(person.getId());
            contributions.put(person.getId(), old + 1);
        }
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.containsKey(id);
    }

    @Override
    public void removeArticle(int id) {
        if (containsArticle(id)) {
            articles.remove(id);
        }
    }

    @Override
    public int getBestContributor() {
        int max = Integer.MIN_VALUE;
        int bestId = Integer.MAX_VALUE;
        for (Integer key : contributions.keySet()) {
            if (contributions.get(key) > max) {
                bestId = key;
                max = contributions.get(key);
            } else if (contributions.get(key) == max) {
                if (bestId > key) {
                    bestId = key;
                }
            }
        }
        return bestId;
    }

    public void addContribution(int followerId, int value) {
        int old = contributions.get(followerId);
        contributions.remove(followerId);
        contributions.put(followerId, old + value);
    }

    public HashMap<Integer, Person> getFlowers() {
        return flowers;
    }
}
