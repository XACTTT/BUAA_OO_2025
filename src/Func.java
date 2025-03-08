import java.util.ArrayList;

public class Func {
    private ArrayList<String> paras;
    private ArrayList<String> funExprs;

    public Func(ArrayList<String> paras, ArrayList<String> funExprs) {
        this.paras = paras;
        this.funExprs = funExprs;
    }

    public String getExpr(int num) {
        return funExprs.get(num);
    }

    public ArrayList<String> getParas() {
        return paras;
    }
}
