import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;

import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Queue;
import java.util.HashSet;
import java.util.LinkedList;

public class Network implements NetworkInterface {
    private HashMap<Integer, PersonInterface> persons = new HashMap<>();
    private int tripleNum = 0;

    public Network() {
    }

    public PersonInterface[] getPersons() {
        ArrayList<PersonInterface> personList = new ArrayList<>(persons.values());
        return personList.toArray(new PersonInterface[0]);
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public PersonInterface getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (!persons.containsKey(person.getId())) {
            persons.put(person.getId(), person);
        } else {
            throw new EqualPersonIdException(person.getId());
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
            EqualRelationException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        }
        ((Person) getPerson(id1)).addAcquaintance((Person) getPerson(id2));
        ((Person) getPerson(id2)).addAcquaintance((Person) getPerson(id1));
        ((Person) getPerson(id1)).changeValue(id2, value);
        ((Person) getPerson(id2)).changeValue(id1, value);

        for (PersonInterface person : persons.values()) {
            if (person.getId() != id1 && person.getId() != id2) {
                if (getPerson(id1).isLinked(person) &&
                        getPerson(id2).isLinked(person)) {
                    tripleNum++;
                }
            }
        }

    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
            EqualPersonIdException, RelationNotFoundException {

        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        }
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        if (getPerson(id1).queryValue(getPerson(id2)) + value > 0) {
            int oldValue1 = getPerson(id1).queryValue(getPerson(id2));
            int oldValue2 = getPerson(id2).queryValue(getPerson(id1));
            ((Person) getPerson(id1)).changeValue(id2, value + oldValue1);

            ((Person) getPerson(id2)).changeValue(id1, value + oldValue2);
        } else {
            for (PersonInterface person : persons.values()) {
                if (person.getId() != id1 && person.getId() != id2) {
                    if (getPerson(id1).isLinked(person) &&
                            getPerson(id2).isLinked(person)) {
                        tripleNum--;
                    }
                }
            }

            ((Person) getPerson(id1)).delRelatedTags(id2);
            ((Person) getPerson(id2)).delRelatedTags(id1);
            ((Person) getPerson(id1)).delAcquaintance(id2);
            ((Person) getPerson(id2)).delAcquaintance(id1);
            ((Person) getPerson(id1)).delValue(id2);
            ((Person) getPerson(id2)).delValue(id1);

        }

    }

    @Override
    public int queryValue(int id1, int id2) throws PersonIdNotFoundException,
            RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2) && containsPerson(id1)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (containsPerson(id1) && containsPerson(id2) &&
                !getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }

        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (id1 == id2) {
            return true;
        }

        Queue<Person> queuePersons = new LinkedList<>();
        HashSet<Integer> visitedId = new HashSet<>();


        queuePersons.add((Person) getPerson(id1));
        visitedId.add(id1);

        while (!queuePersons.isEmpty()) {
            Person current = queuePersons.poll();

            for (Person person : current.getAcquaintance().values()) {

                if (person.getId() == id2) {
                    return true;
                }

                if (!visitedId.contains(person.getId())) {
                    visitedId.add(person.getId());
                    queuePersons.add(person);
                }
            }
        }

        return false;

    }

    @Override
    public int queryTripleSum() {
        return tripleNum;
    }

    @Override
    public void addTag(int personId, TagInterface tag) throws PersonIdNotFoundException,
            EqualTagIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (containsPerson(personId) &&
                getPerson(personId).containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        }
        getPerson(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException,
            RelationNotFoundException, TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2) && containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId2);
        }
        if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        }

        if (!getPerson(personId1).isLinked(getPerson(personId2))) {
            throw new RelationNotFoundException(personId1, personId2);
        }

        if (!getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new EqualPersonIdException(personId1);
        }
        if (getPerson(personId2).getTag(tagId).getSize() <= 999) {
            getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId) throws PersonIdNotFoundException,
            TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        return getPerson(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException,
            TagIdNotFoundException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        if (!getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        if (!getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        }
        getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
    }

    @Override
    public void delTag(int personId, int tagId) throws PersonIdNotFoundException,
            TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        getPerson(personId).delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id) throws PersonIdNotFoundException,
            AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        if (((Person) getPerson(id)).getAcquaintenceSize() == 0) {
            throw new AcquaintanceNotFoundException(id);
        }
        return ((Person) getPerson(id)).chooseMaxValueId();
    }
}
