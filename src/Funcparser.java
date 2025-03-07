import java.util.ArrayList;
import java.util.HashMap;

public class Funcparser {
    private String define0;
    private String define1;
    private String recursionDf;
    private String para1;
    private String para2;
    private ArrayList<Poly> funcpolys;

    Funcparser(ArrayList<String> funcData) {
        this.funcpolys = new ArrayList<>();
        for (String func : funcData) {
            func = func.replaceAll("[ \t]", "");
        }
        for (int i = 0; i < funcData.size(); i++) {
            if (funcData.get(i).charAt(2) == '0') {
                this.define0 = funcData.get(i);
            } else if (funcData.get(i).charAt(2) == '1') {
                this.define1 = funcData.get(i);
            } else if (funcData.get(i).charAt(2) == 'n') {
                this.recursionDf = funcData.get(i);
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
        this.para1 = define.substring(pos);
        pos++;
        if (define.charAt(pos) == ',') {
            pos++;
            this.para2 = define.substring(pos);
            pos += 3;
            String funcExpr = define.substring(pos, define.length() - 1);
            HashMap<String, String> ret = new HashMap<>();
            ret.put(this.para1, funcExpr);
            ret.put(this.para2, funcExpr);
            return ret;
        } else {
            pos += 2;
            String funcExpr = define.substring(pos, define.length() - 1);
            HashMap<String, String> ret = new HashMap<>();
            ret.put(this.para1, funcExpr);
            return ret;
        }
    }


    public void parseFunc() {
        HashMap<String, String> define0 = parseDefine(this.define0);
        HashMap<String, String> define1 = parseDefine(this.define1);
        if (define0.size() == 1 && define1.size() == 1) {
            int pos=0;
          ArrayList<String> para=   getRecursionPara(this.recursionDf);
//
        } else {

        }

    }

    public ArrayList<String> getRecursionPara(String recursionDf) {
        ArrayList<String> recursionPara = new ArrayList<>();
        return null;
    }

    public Expr exprGet(){
        return null;
    }
}
