import java.util.ArrayList;
import java.util.HashMap;

public class Funcparser {
    private ArrayList<String> customFunclist;
    private ArrayList<String> recurFunclist;

    Funcparser(ArrayList<String> cunstomFuncData, ArrayList<String> recurfuncData) {
        ArrayList<String> recurfunc = new ArrayList<>();
        for (String func : recurfuncData) {
            func = func.replaceAll("[ \t]", "");
            recurfunc.add(func);
        }
        this.recurFunclist = recurfunc;
        ArrayList<String> cunstomFunc = new ArrayList<>();
        for (String func : cunstomFuncData) {
            func = func.replaceAll("[ \t]", "");
            cunstomFunc.add(func);
        }
        this.customFunclist = cunstomFunc;
    }

    public HashMap<String, ArrayList<String>> parseFormalPara() {
        HashMap<String, ArrayList<String>> funcMap = new HashMap<>();
        for (String func : customFunclist) {
            ArrayList<String> paras = new ArrayList<>();
            String funcName = Character.toString(func.charAt(0));
            for (int i = 0; i < func.length(); i++) {
                if (func.charAt(i) == '=') {
                    break;
                }
                if (func.charAt(i) == 'x' || func.charAt(i) == 'y') {
                    paras.add(Character.toString(func.charAt(i)));
                }
            }
            funcMap.put(funcName, paras);
        }
        ArrayList<String> paras = new ArrayList<>();
        String recurfunc = recurFunclist.get(0);
        for (int i = 0; i < recurfunc.length(); i++) {
            if (recurfunc.charAt(i) == '=') {
                break;
            }
            if (recurfunc.charAt(i) == 'x' || recurfunc.charAt(i) == 'y') {
                paras.add(Character.toString(recurfunc.charAt(i)));
            }
        }
        funcMap.put("f", paras);
        return funcMap;
    }

    public HashMap<String, Func> parseFunc() {
        HashMap<String, Func> funclist = new HashMap<>();
        HashMap<String, ArrayList<String>> funcMap = new HashMap<>();
        funcMap = parseFormalPara();
        for (int i = 0; i < customFunclist.size(); i++) {
            String func = customFunclist.get(i);
            String funcName = Character.toString(func.charAt(0));
            String funcExpr = func.substring(func.indexOf("=") + 1);
            Func function = geneCusFunc(funcExpr, funcMap.get(funcName), funclist);
            funclist.put(funcName, function);
        }
        if (!recurFunclist.isEmpty()) {
            ArrayList<String> recurfuncExpr = new ArrayList<>();
            for (String func : recurFunclist) {
                if (func.charAt(2) == '0') {
                    recurfuncExpr.add(func.substring(func.indexOf("=") + 1));
                }
            }
            for (String func : recurFunclist) {
                if (func.charAt(2) == '1') {
                    recurfuncExpr.add(func.substring(func.indexOf("=") + 1));
                }
            }
            for (String func : recurFunclist) {
                if (func.charAt(2) == 'n') {
                    recurfuncExpr.add(func.substring(func.indexOf("=") + 1));
                }
            }
            Func function = geneRecFunc(recurfuncExpr, funcMap.get("f"), funclist);
            funclist.put("f", function);
            return funclist;
        }
        return funclist;
    }

    public Func geneCusFunc(String funString, ArrayList<String> paras,
        HashMap<String, Func> functions) {
        ArrayList<String> utiFuncString = new ArrayList<>();
        Lexer lexer = new Lexer(funString);
        Parser parser = new Parser(lexer, functions);
        Expr expr = parser.parseExpr();
        String funExpr = expr.cal().toString1();
        utiFuncString.add(funExpr);
        return new Func(paras, utiFuncString);
    }

    public Func geneRecFunc(ArrayList<String> recurfuncExpr, ArrayList<String> paras,
        HashMap<String, Func> functions) {
        ArrayList<String> funcs = new ArrayList<>();
        funcs.add(recurfuncExpr.get(0));
        funcs.add(recurfuncExpr.get(1));
        String recExpr = recurfuncExpr.get(2);
        recExpr = recExpr.replaceAll("\\+\\+", "+");
        recExpr = recExpr.replaceAll("--", "+");
        recExpr = recExpr.replaceAll("\\+-", "-");
        recExpr = recExpr.replaceAll("-\\+", "-");
        String fun2 = recExpr.replaceAll("n-1", "1");
        fun2 = fun2.replaceAll("n-2", "0");
        String fun3 = recExpr.replaceAll("n-1", "2");
        fun3 = fun3.replaceAll("n-2", "1");
        String fun4 = recExpr.replaceAll("n-1", "3");
        fun4 = fun4.replaceAll("n-2", "2");
        String fun5 = recExpr.replaceAll("n-1", "4");
        fun5 = fun5.replaceAll("n-2", "3");
        Func function2 = new Func(paras, funcs);
        functions.put("f", function2);
        Lexer lexer2 = new Lexer(fun2);
        Parser parser2 = new Parser(lexer2, functions);
        Expr expr2 = parser2.parseExpr();
        String ffun2 = expr2.cal().toString1();
        funcs.add(ffun2);
        functions.remove("f");
        Func function3 = new Func(paras, funcs);
        functions.put("f", function3);
        Lexer lexer3 = new Lexer(fun3);
        Parser parser3 = new Parser(lexer3, functions);
        Expr expr3 = parser3.parseExpr();
        String ffun3 = expr3.cal().toString1();
        funcs.add(ffun3);
        functions.remove("f");
        Func function4 = new Func(paras, funcs);
        functions.put("f", function4);
        Lexer lexer4 = new Lexer(fun4);
        Parser parser4 = new Parser(lexer4, functions);
        Expr expr4 = parser4.parseExpr();
        String ffun4 = expr4.cal().toString1();
        funcs.add(ffun4);
        functions.remove("f");
        Func function5 = new Func(paras, funcs);
        functions.put("f", function5);
        Lexer lexer5 = new Lexer(fun5);
        Parser parser5 = new Parser(lexer5, functions);
        Expr expr5 = parser5.parseExpr();
        String ffun5 = expr5.cal().toString1();
        funcs.add(ffun5);
        functions.remove("f");
        return new Func(paras, funcs);
    }
}

