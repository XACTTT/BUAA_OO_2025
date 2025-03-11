public class Token {
    private final Type type;
    private final String content;

    public Token(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public enum Type {
        ADD, MUL, LP, RP, NUM, VAR, POW, SUB,SIN,COS,FUN,LBP,RBP,COMMA
    }
}