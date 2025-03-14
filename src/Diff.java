public class Diff implements Factor {
    private String name;
    private Expr expr;
    public Diff(String name, Expr expr) {
        this.name = name;
        this.expr = expr;
    }
    public void setPow(int num) {
    }

    public Poly cal() {
        Poly p = new Poly();
        p=expr.cal();
        return p.diffcal();
    }
}
