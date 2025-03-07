public class Tri implements Factor {
    private String name;
    private Expr expr;
    private int pow;

    public Tri(String name, Expr expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    public void setPow(int pow) {
        this.pow = pow;
    }

    public Poly cal() {
        Poly poly = new Poly();
        poly.addUnit(this.name,this.expr,this.pow);
        return poly;
    }

    ;
}
