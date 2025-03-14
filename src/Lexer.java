import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            if (input.charAt(pos) != 's' && input.charAt(pos) != 'c' && input.charAt(pos) != 'd'
                    && !(input.charAt(pos) >= '0' && input.charAt(pos) <= '9')) {
                if (input.charAt(pos) == '(') {
                    tokens.add(new Token(Token.Type.LP, "("));
                } else if (input.charAt(pos) == ')') {
                    tokens.add(new Token(Token.Type.RP, ")"));
                } else if (input.charAt(pos) == '+') {
                    tokens.add(new Token(Token.Type.ADD, "+"));
                } else if (input.charAt(pos) == '*') {
                    tokens.add(new Token(Token.Type.MUL, "*"));
                } else if (input.charAt(pos) == '-') {
                    tokens.add(new Token(Token.Type.SUB, "-"));
                } else if (input.charAt(pos) == '^') {
                    tokens.add(new Token(Token.Type.POW, "^"));
                } else if (input.charAt(pos) == 'x' || input.charAt(pos) == 'y') {
                    String var = input.substring(pos, pos + 1);
                    tokens.add(new Token(Token.Type.VAR, var));
                } else if (input.charAt(pos) == 'f' || input.charAt(pos) == 'g' ||
                        input.charAt(pos) == 'h') {
                    tokens.add(new Token(Token.Type.FUN, Character.toString(input.charAt(pos))));
                } else if (input.charAt(pos) == '{') {
                    tokens.add(new Token(Token.Type.LBP, "{"));
                } else if (input.charAt(pos) == '}') {
                    tokens.add(new Token(Token.Type.RBP, "}"));
                } else if (input.charAt(pos) == ',') {
                    tokens.add(new Token(Token.Type.COMMA, ","));
                }
                pos++;
            } else if (input.charAt(pos) == 's') {
                tokens.add(new Token(Token.Type.SIN, "sin"));
                pos = pos + 3;
            } else if (input.charAt(pos) == 'c') {
                tokens.add(new Token(Token.Type.COS, "cos"));
                pos = pos + 3;
            } else if (input.charAt(pos) == 'd') {
                tokens.add(new Token(Token.Type.DIFF, "dx"));
                pos = pos + 2;
            } else {
                char now = input.charAt(pos);
                StringBuilder sb = new StringBuilder();
                while (now >= '0' && now <= '9') {
                    sb.append(now);
                    pos++;
                    if (pos >= input.length()) {
                        break;
                    }
                    now = input.charAt(pos);
                }
                tokens.add(new Token(Token.Type.NUM, sb.toString()));
            }
        }
    }

    public Token getCurToken() {
        return tokens.get(index);
    }

    public void nextToken() {
        index++;
    }

    public boolean isEnd() {
        return index >= tokens.size();
    }
}
