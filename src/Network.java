import com.oocourse.spec3.exceptions.*;

import com.oocourse.spec3.main.*;

import java.util.*;

public class Network implements NetworkInterface {
    private HashMap<Integer, PersonInterface> persons = new HashMap<>();
    private HashMap<Integer, OfficialAccountInterface> accounts = new HashMap<>();
    private HashMap<Integer, Integer> articles = new HashMap<>();//文章id——贡献者id
    private HashMap<Integer, Integer> articleContributors = new HashMap<>();
    private HashSet<TagInterface> tags = new HashSet<>();
    private HashMap<Integer, MessageInterface> messages = new HashMap<>();
    private HashMap<Integer, Integer> emojiIdList = new HashMap<>();
    private HashMap<Integer, Integer> emojiHeatList = new HashMap<>();
    private HashMap<Integer, Integer> emojiMessageList = new HashMap<>();
    private int tripleNum = 0;
    private int length = -1;

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
        for (TagInterface tag : tags) {
            if (tag.hasPerson(getPerson(id2)) && tag.hasPerson(getPerson(id1))) {
                ((Tag) tag).changeValueSum(getPerson(id1), getPerson(id2),
                        0, value);
            }
        }
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
            for (TagInterface tag : tags) {
                if (tag.hasPerson(getPerson(id2)) && tag.hasPerson(getPerson(id1))) {
                    ((Tag) tag).changeValueSum(getPerson(id1), getPerson(id2),
                            oldValue1, value + oldValue1);
                }
            }
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
            for (TagInterface tag : tags) {
                if (tag.hasPerson(getPerson(id2)) && tag.hasPerson(getPerson(id1))) {
                    ((Tag) tag).changeValueSum(getPerson(id1), getPerson(id2),
                            getPerson(id1).queryValue(getPerson(id2)), 0);
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
        length = 0;
        if (id1 == id2) {
            return true;
        }


        Queue<Person> queuePersons = new LinkedList<>();
        HashSet<Integer> visitedId = new HashSet<>();


        queuePersons.add((Person) getPerson(id1));
        visitedId.add(id1);
        int level = 0; // 初始化层级为0

        while (!queuePersons.isEmpty()) {
            int levelSize = queuePersons.size(); // 当前层的节点数
            level++; // 进入下一层

            for (int i = 0; i < levelSize; i++) {
                Person current = queuePersons.poll();

                for (Person person : current.getAcquaintance().values()) {
                    if (person.getId() == id2) {
                        length = level;
                        return true;
                    }
                    if (!visitedId.contains(person.getId())) {
                        visitedId.add(person.getId());
                        queuePersons.add(person);
                    }
                }
            }
        }
        length = -1;
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
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
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
    public int queryTagValueSum(int personId, int tagId) throws PersonIdNotFoundException,
            TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        return getPerson(personId).getTag(tagId).getValueSum();
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
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public void addMessage(MessageInterface message) throws EqualMessageIdException, EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new EqualMessageIdException(message.getId());
        }
        if ((message instanceof EmojiMessageInterface) && !containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) {
            throw new EmojiIdNotFoundException(((EmojiMessageInterface) message).getEmojiId());
        }
        if ((message instanceof ForwardMessageInterface) && !containsArticle(((ForwardMessageInterface) message).getArticleId())) {
            throw new ArticleIdNotFoundException(((ForwardMessageInterface) message).getArticleId());
        }
        if ((message instanceof ForwardMessageInterface) && containsArticle(((ForwardMessageInterface) message).getArticleId()) && !(message.getPerson1().getReceivedArticles().contains(((ForwardMessageInterface) message).getArticleId()))) {
            throw new ArticleIdNotFoundException(((ForwardMessageInterface) message).getArticleId());
        }
        if ((!(message instanceof EmojiMessageInterface) ||
                containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) &&
                (!(message instanceof ForwardMessageInterface) ||
                        (containsArticle(((ForwardMessageInterface) message).getArticleId()) &&
                                (message.getPerson1().getReceivedArticles().
                                        contains(((ForwardMessageInterface) message).getArticleId()))))
                && message.getType() == 0 && message.getPerson1().equals(message.getPerson2())
        ) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(message.getId(), message);
        if (message instanceof EmojiMessageInterface) {
            emojiMessageList.put(message.getId(), ((EmojiMessageInterface) message).getEmojiId());
        }

    }

    @Override
    public MessageInterface getMessage(int id) {
        if (containsMessage(id)) {
            return messages.get(id);
        }
        return null;
    }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        if (getMessage(id).getType() == 0 && !(getMessage(id).getPerson1().isLinked(getMessage(id).getPerson2()))) {
            throw new RelationNotFoundException(getMessage(id).getPerson1().getId(), getMessage(id).getPerson2().getId());
        }
        if (getMessage(id).getType() == 1 && !getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            throw new TagIdNotFoundException(getMessage(id).getTag().getId());
        }

        if (getMessage(id).getType() == 0) {
            getMessage(id).getPerson1().addSocialValue(getMessage(id).getSocialValue());
            getMessage(id).getPerson2().addSocialValue(getMessage(id).getSocialValue());
            if (getMessage(id) instanceof RedEnvelopeMessageInterface) {
                int money = ((RedEnvelopeMessageInterface) (getMessage(id))).getMoney();
                getMessage(id).getPerson1().addMoney(-money);
                getMessage(id).getPerson2().addMoney(money);
            } else if (getMessage(id) instanceof ForwardMessageInterface) {
                ((Person) getMessage(id).getPerson2()).insertArticle(((ForwardMessageInterface) getMessage(id)).getArticleId());
            } else if (getMessage(id) instanceof EmojiMessageInterface) {
                int old = emojiHeatList.get(((EmojiMessageInterface) getMessage(id)).getEmojiId());
                emojiHeatList.remove(((EmojiMessageInterface) getMessage(id)).getEmojiId());
                emojiHeatList.put(((EmojiMessageInterface) getMessage(id)).getEmojiId(), old + 1);
            }
            ((Person) getMessage(id).getPerson2()).insertMessage(getMessage(id));
            messages.remove(id);
        } else if (getMessage(id).getType() == 1 && getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            getMessage(id).getPerson1().addSocialValue(getMessage(id).getSocialValue());
            ((Tag) getMessage(id).getTag()).addSocialValue(getMessage(id).getSocialValue());

            if (getMessage(id) instanceof RedEnvelopeMessageInterface && getMessage(id).getTag().getSize() > 0) {
                int size = getMessage(id).getTag().getSize();
                int i = ((RedEnvelopeMessageInterface) (getMessage(id))).getMoney() / size;

                getMessage(id).getPerson1().addMoney(-i * size);
                ((Tag) getMessage(id).getTag()).addMoney(i);

            } else if (getMessage(id) instanceof ForwardMessageInterface && getMessage(id).getTag().getSize() > 0) {
                ((Tag) getMessage(id).getTag()).insertArticle(((ForwardMessageInterface) getMessage(id)).getArticleId());
            } else if (getMessage(id) instanceof EmojiMessageInterface) {
                int old = emojiHeatList.get(((EmojiMessageInterface) getMessage(id)).getEmojiId());
                emojiHeatList.remove(((EmojiMessageInterface) getMessage(id)).getEmojiId());
                emojiHeatList.put(((EmojiMessageInterface) getMessage(id)).getEmojiId(), old + 1);
            }
            ((Tag) getMessage(id).getTag()).insertMessage(getMessage(id));
            messages.remove(id);
        }

    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).getSocialValue();
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).getReceivedMessages();
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojiIdList.containsKey(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        }
        emojiIdList.put(id, id);
        emojiHeatList.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }

        return getPerson(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        }

        return emojiHeatList.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        HashMap<Integer, Integer> deleteIds = new HashMap<>();
        for (Integer emojiId : emojiHeatList.keySet()) {
            if (emojiHeatList.get(emojiId) < limit) {
                deleteIds.put(emojiId, emojiId);
            }
        }
        for (Integer emojiId : deleteIds.keySet()) {
            emojiHeatList.remove(emojiId);
            emojiIdList.remove(emojiId);
        }
        HashMap<Integer, Integer> deleteIds2 = new HashMap<>();
        for (MessageInterface message : messages.values()) {
            if (message instanceof EmojiMessageInterface) {
                if (deleteIds.containsKey(((EmojiMessageInterface) message).getEmojiId())) {
                    deleteIds2.put(message.getId(), message.getId());
                }
            }
        }
        for (Integer messageId : deleteIds2.keySet()) {
            messages.remove(messageId);
        }
        return emojiHeatList.size();
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

    @Override
    public int queryCoupleSum() {
        int ans = 0;
        for (PersonInterface person1 : persons.values()) {
            if (!((Person) person1).getAcquaintance().isEmpty() &&
                    !((Person) getPerson(((Person) person1).chooseMaxValueId())).
                            getAcquaintance().isEmpty() &&
                    person1.getId() == ((Person) getPerson(((Person) person1).
                            chooseMaxValueId())).chooseMaxValueId()) {
                ans++;
            }
        }
        return ans / 2;
    }

    @Override
    public int queryShortestPath(int id1, int id2) throws PersonIdNotFoundException,
            PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        isCircle(id1, id2);
        if (length == -1) {
            throw new PathNotFoundException(id1, id2);
        }
        return length;
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name) throws
            PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        }
        OfficialAccount account = new OfficialAccount(personId, accountId, name);
        account.addFollower(getPerson(personId));
        accounts.put(accountId, account);
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(accountId);

    }

    @Override
    public boolean containsArticle(int id) {
        return articles.containsKey(id);
    }

    @Override
    public void contributeArticle(int personId, int accountId, int articleId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }
        if (!accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        articles.put(articleId, personId);//ensure1
        accounts.get(accountId).addArticle(getPerson(personId), articleId);//ensure1
        // ((OfficialAccount) accounts.get(accountId)).addContribution(personId, 1);
        articleContributors.put(articleId, personId);//ensure3
        for (Person person : ((OfficialAccount) accounts.get(accountId)).getFlowers().values()) {
            person.insertArticle(articleId);
        }
    }

    @Override
    public void deleteArticle(int personId, int accountId, int articleId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }

        accounts.get(accountId).removeArticle(articleId);
        ((OfficialAccount) accounts.get(accountId)).addContribution(articles.get(articleId), -1);
        for (Person person : ((OfficialAccount) accounts.get(accountId)).getFlowers().values()) {
            person.removeArticle(articleId);
        }

    }

    @Override
    public void followOfficialAccount(int personId, int accountId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new EqualPersonIdException(personId);
        }
        accounts.get(accountId).addFollower(getPerson(personId));
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).queryReceivedArticles();
    }
}
