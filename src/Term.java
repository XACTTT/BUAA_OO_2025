import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

   public void addFactor(BigInteger num) {
        Factor temp =new Num(num);
        factors.add(temp);
   }
   public Poly cal(){
        Poly poly = new Poly();
        poly.init();
        for (Factor factor : this.factors) {
         poly=poly.mul(factor.cal());
        }
        return poly;
   }
}
