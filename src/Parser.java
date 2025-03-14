import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    private final Lexer lexer;
    private final HashMap<String, Func> functions;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.functions = null;
    }

    public Parser(Lexer lexer, HashMap<String, Func> functions) {
        this.lexer = lexer;
        this.functions = functions;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        if (lexer.getCurToken().getType() == Token.Type.ADD
                || lexer.getCurToken().getType() == Token.Type.SUB) {
            if (lexer.getCurToken().getType() == Token.Type.SUB) {
                lexer.nextToken();
                expr.addTerm(parseTerm(-1));
            } else {
                lexer.nextToken();
                expr.addTerm(parseTerm(1));
            }
        } else {
            expr.addTerm(parseTerm(1));
        }

        while (!lexer.isEnd() && (lexer.getCurToken().getType() == Token.Type.ADD
                || lexer.getCurToken().getType() == Token.Type.SUB)) {
            if (lexer.getCurToken().getType() == Token.Type.SUB) {
                lexer.nextToken();
                expr.addTerm(parseTerm(-1));
            } else {
                lexer.nextToken();
                expr.addTerm(parseTerm(1));
            }
        }
        return expr;
    }

    public Term parseTerm(int flag1) {
        Term term = new Term();
        int flag2 = 1;
        if (lexer.getCurToken().getType() == Token.Type.ADD ||
                lexer.getCurToken().getType() == Token.Type.SUB) {
            if (lexer.getCurToken().getType() == Token.Type.SUB) {
                flag2 = -1;
            }
            lexer.nextToken();
        }
        int flag = flag1 * flag2;
        term.addFactor(parseFactor());
        while (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.MUL) {
            lexer.nextToken();
            term.addFactor(parseFactor());
        }
        if (flag == -1) {
            term.addFactor(BigInteger.valueOf(-1));
        }
        return term;
    }

    public Factor parseFactor() {
        Token token = lexer.getCurToken();
        if (token.getType() == Token.Type.NUM ||
                token.getType() == Token.Type.ADD ||
                token.getType() == Token.Type.SUB) {
            Num num = parseNum();
            getPow(num);
            return num;
        } else if (token.getType() == Token.Type.VAR) {
            Var var = parseVar();
            getPow(var);
            return var;
        } else if (token.getType() == Token.Type.DIFF) {
            lexer.nextToken();
            lexer.nextToken();
            Expr diffExpr = new Expr();
            diffExpr = parseExpr();
            lexer.nextToken();
            Factor diff = new Diff("diff", diffExpr);
            return diff;
        } else if (token.getType() == Token.Type.SIN ||
                token.getType() == Token.Type.COS) {

            int signSign = 0;
            if (token.getType() == Token.Type.SIN) {
                signSign = 1;
            }
            lexer.nextToken();
            lexer.nextToken();
            Expr triExpr = new Expr();
            triExpr = parseExpr();
            getPow(triExpr);
            lexer.nextToken();
            Factor tri = null;
            if (signSign == 1) {
                tri = new Tri("sin", triExpr);
            } else {
                tri = new Tri("cos", triExpr);
            }
            getPow(tri);
            return tri;
        } else if (token.getType() == Token.Type.FUN) {
            return handleFun(token);
        } else {
            Expr subExpr = new Expr();
            lexer.nextToken();
            subExpr = parseExpr();
            lexer.nextToken();
            getPow(subExpr);
            return subExpr;
        }
    }

    public Expr handleFun(Token token) {
        String name;
        name = token.getContent();
        int num;
        if (name.equals("f")) {
            lexer.nextToken();
            lexer.nextToken();
            num = Integer.parseInt(lexer.getCurToken().getContent());
            lexer.nextToken();
            lexer.nextToken();
            lexer.nextToken();
        } else {
            lexer.nextToken();
            lexer.nextToken();
            num = 0;
        }
        ArrayList<Factor> parameter = new ArrayList<>();
        Factor factor = parseFactor();
        parameter.add(factor);
        while (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.COMMA) {
            lexer.nextToken();
            Factor nextfactor = parseFactor();
            parameter.add(nextfactor);
        }
        lexer.nextToken();
        return parseFunc(parameter, num, name);
    }

    private void getPow(Factor factor) {
        if (!lexer.isEnd()) {
            if (lexer.getCurToken().getType() == Token.Type.POW) {
                lexer.nextToken();
                if (lexer.getCurToken().getType() == Token.Type.ADD) {
                    lexer.nextToken();
                }
                factor.setPow(Integer.parseInt(lexer.getCurToken().getContent()));
                lexer.nextToken();
            } else {
                factor.setPow(1);
            }
        } else {
            factor.setPow(1);
        }
    }

    public Num parseNum() {
        Num num;

        Token token = lexer.getCurToken();
        if (token.getType() == Token.Type.NUM) {
            num = new Num(new BigInteger(token.getContent()));
        } else {
            lexer.nextToken();
            String s = token.getContent() + lexer.getCurToken().getContent();
            num = new Num(new BigInteger(s));
        }
        lexer.nextToken();
        return num;

    }

    public Var parseVar() {
        Var var;
        Token token = lexer.getCurToken();
        lexer.nextToken();
        var = new Var(token.getContent());
        return var;
    }

    public Expr parseFunc(ArrayList<Factor> factors, int num, String name) {
        ArrayList<String> factorParas = new ArrayList<>();
        for (int i = 0; i < factors.size(); i++) {
            String para = factors.get(i).cal().toString1();
            factorParas.add(para);
        }

        Func function = this.functions.get(name);
        ArrayList<String> replacePara = function.getParas();
        String funcExpr = function.getExpr(num);
        StringBuilder realFuncExpr = new StringBuilder();
        for (int i = 0; i < funcExpr.length(); i++) {
            int sign = 0;
            for (int j = 0; j < replacePara.size(); j++) {
                if (Character.toString(funcExpr.charAt(i)).equals(replacePara.get(j))) {
                    sign = 1;
                    realFuncExpr.append("(");
                    realFuncExpr.append(factorParas.get(j));
                    realFuncExpr.append(")");
                }
            }
            if (sign == 0) {
                realFuncExpr.append(funcExpr.charAt(i));

            }

        }
        Lexer tplexer = new Lexer(realFuncExpr.toString());
        Parser tpparser = new Parser(tplexer);
        return tpparser.parseExpr();
    }

}
