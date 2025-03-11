import java.util.ArrayList;
import java.util.HashMap;

public class Funcparser {
    private String define0;
    private String define1;
    private String recursionDf;
    private String para1;
    private String para2;

    Funcparser(ArrayList<String> funcData) {
        ArrayList<String>  funcpolys = new ArrayList<>();
        for (String func : funcData) {
            func = func.replaceAll("[ \t]", "");
            func = func.replaceAll("x", "u");
            func = func.replaceAll("y", "v");
            funcpolys.add(func);
        }
        for (int i = 0; i < funcpolys.size(); i++) {
            if (funcpolys.get(i).charAt(2) == '0') {
                this.define0 = funcpolys.get(i);
            } else if (funcpolys.get(i).charAt(2) == '1') {
                this.define1 = funcpolys.get(i);
            } else if (funcpolys.get(i).charAt(2) == 'n') {
                this.recursionDf = funcpolys.get(i);
            }
        }
        this.para1 = null;
        this.para2 = null;
    }

    public HashMap<String, String> parseDefine(String define) {

        int pos = 0;
        while (define.charAt(pos) != '(') {
            pos++;
        }

        pos++;
        this.para1 = define.substring(pos, pos + 1);
        pos++;
        if (define.charAt(pos) == ',') {
            pos++;
            this.para2 = define.substring(pos, pos + 1);
            pos += 3;
            String funcExpr = define.substring(pos);
            HashMap<String, String> ret = new HashMap<>();
            ret.put(this.para1, funcExpr);
            ret.put(this.para2, funcExpr);
            return ret;
        } else {
            pos += 2;
            String funcExpr = define.substring(pos);
            HashMap<String, String> ret = new HashMap<>();
            ret.put(this.para1, funcExpr);
            return ret;
        }
    }

    public Func parseFunc() {
        HashMap<String, String> define0 = parseDefine(this.define0);
        HashMap<String, String> define1 = parseDefine(this.define1);
        ArrayList<String> paras = new ArrayList<>();
        ArrayList<String> funcs = new ArrayList<>();
        String ffun0 = define0.get(this.para1);
        String ffun1 = define1.get(this.para1);
        for (String para : define0.keySet()) {
            paras.add(this.para1);
            paras.add(this.para2);
            break;
        }
        funcs.add(ffun0);
        funcs.add(ffun1);
        String recDfExpr = this.recursionDf.substring(this.recursionDf.indexOf("=") + 1);
        recDfExpr = recDfExpr.replaceAll("\\+\\+", "+");
        String fun2 = recDfExpr.replaceAll("n-1", "1");
        fun2 = fun2.replaceAll("n-2", "0");
        String fun3 = recDfExpr.replaceAll("n-1", "2");
        fun3 = fun3.replaceAll("n-2", "1");
        String fun4 = recDfExpr.replaceAll("n-1", "3");
        fun4 = fun4.replaceAll("n-2", "2");
        String fun5 = recDfExpr.replaceAll("n-1", "4");
        fun5 = fun5.replaceAll("n-2", "3");
        Func function2 = new Func(paras, funcs);
        Lexer lexer2 = new Lexer(fun2);
        Parser parser2 = new Parser(lexer2, function2);
        Expr expr2 = parser2.parseExpr();
        String ffun2 = expr2.cal().toString1();
        funcs.add(ffun2);
        Func function3 = new Func(paras, funcs);
        Lexer lexer3 = new Lexer(fun3);
        Parser parser3 = new Parser(lexer3, function3);
        Expr expr3 = parser3.parseExpr();
        String ffun3 = expr3.cal().toString1();
        funcs.add(ffun3);
        Func function4 = new Func(paras, funcs);
        Lexer lexer4 = new Lexer(fun4);
        Parser parser4 = new Parser(lexer4, function4);
        Expr expr4 = parser4.parseExpr();
        String ffun4 = expr4.cal().toString1();
        funcs.add(ffun4);
        Func function5 = new Func(paras, funcs);
        Lexer lexer5 = new Lexer(fun5);
        Parser parser5 = new Parser(lexer5, function5);
        Expr expr5 = parser5.parseExpr();
        String ffun5 = expr5.cal().toString1();
        funcs.add(ffun5);

        return new Func(paras, funcs);

    }

}

