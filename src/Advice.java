public class Advice {
    private final Type type;

    public Advice(Type type) {
        this.type = type;
    }

    public enum Type {
        MOVE,WAIT,TURN,OPEN,END
    }
}
