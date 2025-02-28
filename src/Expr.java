import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> terms = new ArrayList<>();
    private int pow;

    public Expr() {
        this.pow = 1;
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public Poly cal() {
        Poly poly = new Poly();
        for (Term term : this.terms) {//不加this？
            poly.add(term.cal());
        }
        poly=poly.powCal(this.pow);
        return poly;
    }



    @Override
    public void setPow(int pow) {
        this.pow = pow;
    }

    public void print() {
        System.out.println("Expr " + this);
    }

}
