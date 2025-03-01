import java.math.BigInteger;

public class Num implements Factor {
    private final BigInteger num;
    private int pow;

    public Num(BigInteger num) {
        this.num = num;
        this.pow = 1;
    }

    public void setPow(int k) {
        pow = k;
    }

    @Override
    public Poly cal() {
        Poly poly = new Poly();
        poly.addMono(this.num);
        return poly;
    }
}
