import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private int id;
    private int socialValue;
    private int type;
    private PersonInterface person1;
    private PersonInterface person2;
    private TagInterface tag;

    /*@ ensures type == 0;
     @ ensures tag == null;
     @ ensures id == messageId;
     @ ensures socialValue == messageSocialValue;
     @ ensures person1 == messagePerson1;
     @ ensures person2 == messagePerson2;
     @*/
    public Message(int messageId, int messageSocialValue, PersonInterface messagePerson1, PersonInterface messagePerson2) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.person2 = messagePerson2;
        this.tag = null;
        this.type = 0;

    }

    /*@ ensures type == 1;
      @ ensures person2 == null;
      @ ensures id == messageId;
      @ ensures socialValue == messageSocialValue;
      @ ensures person1 == messagePerson1;
      @ ensures tag == messageTag;
      @*/
    public Message(int messageId, int messageSocialValue, PersonInterface messagePerson1, TagInterface messageTag) {
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.tag = messageTag;
        this.type = 1;
        this.person2 = null;
    }


    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public PersonInterface getPerson1() {
        return person1;
    }

    @Override
    public PersonInterface getPerson2() {
        if (person2 != null) {
            return person2;
        }
        return null;
    }

    @Override
    public TagInterface getTag() {
        if (tag != null) {
            return tag;
        }
        return null;
    }

    public boolean equals(Message message) {
        if (message != null && this.getId() == message.getId()) {
            return true;
        } else {
            return false;
        }
    }
}
