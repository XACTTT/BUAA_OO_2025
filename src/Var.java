public class Var implements Factor {
    private final String name;
    private  int pow;

    public Var(String name) {
        this.name = name;
        this.pow = 1;
    }

    @Override
    public void setPow(int pow) {
        this.pow=pow;
    }

    @Override
    public Poly cal() {
        Poly poly = new Poly();
        poly.addMono(this.name, this.pow);
        return poly;
    }
}
