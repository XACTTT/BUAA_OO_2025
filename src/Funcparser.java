import java.util.ArrayList;
import java.util.HashMap;

public class Funcparser {
    private final ArrayList<String> customFunclist;
    private final ArrayList<String> recurFunclist;

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
        if (!recurFunclist.isEmpty()) {
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
        }

        return funcMap;
    }

    public HashMap<String, Func> parseFunc() {
        HashMap<String, Func> funclist = new HashMap<>();
        HashMap<String, ArrayList<String>> funcMap;
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


        for (int i = 0; i < 2; i++) {
            processFunc(recurfuncExpr.get(i), paras, functions, funcs);
        }

        String recExpr = recurfuncExpr.get(2).replaceAll("\\+\\+", "+")
            .replaceAll("\\+-", "-");

        for (int offset = 0; offset < 4; offset++) {
            String replacedExpr = recExpr.replaceAll("n-1", String.valueOf(offset + 1))
                .replaceAll("n-2", String.valueOf(offset));
            processFunc(replacedExpr, paras, functions, funcs);
        }

        return new Func(paras, funcs);
    }

    private void processFunc(String expr, ArrayList<String> paras,
        HashMap<String, Func> functions, ArrayList<String> funcs) {
        Func func = new Func(paras, funcs);
        functions.put("f", func);

        Lexer lexer = new Lexer(expr);
        Parser parser = new Parser(lexer, functions);
        Expr parsedExpr = parser.parseExpr();

        String result = parsedExpr.cal().toString1();
        funcs.add(result);

        functions.remove("f");
    }
}

