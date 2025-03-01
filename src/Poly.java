import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Poly {
    private final ArrayList<Mono> poly = new ArrayList<>();

    public void print() {
        int first=0;
        for (Mono mono : poly) {
            int sign = 0;
            if (!mono.getCoe().equals(BigInteger.ZERO)) {
                if (mono.getCoe().equals(BigInteger.ONE)) {
                    if(first!=0){
                        System.out.print('+');
                    }
                } else if (mono.getCoe().equals(BigInteger.valueOf(-1))) {
                    System.out.print("-");
                } else {
                    if (first!=0) {
                        if(mono.getCoe().compareTo(BigInteger.valueOf(0))>0) {
                            System.out.print('+');
                        }
                    }
                    System.out.print(mono.getCoe());
                    sign = 1;
                }
                HashMap<String, Integer> a = mono.getHash();
                if (!a.isEmpty()) {
                    for (HashMap.Entry<String, Integer> entry : a.entrySet()) {
                        String k = entry.getKey();
                        Integer v = entry.getValue();

                        if (sign == 1) {
                            if (v > 1) {
                                System.out.print("*" + k + "^" + v);
                            } else if (v == 1) {
                                System.out.print("*" + k);
                            }
                        } else {
                            if (v > 1) {
                                System.out.print(k + "^" + v);
                            } else if (v == 1) {
                                System.out.print(k);
                            } else {
                                System.out.print(1);
                            }
                        }
                    }
                } else {
                    if (sign == 0) {
                        System.out.print(1);
                    }
                }
            }
            first=1;
        }
    }

    public void init() {
        poly.add(new Mono(BigInteger.valueOf(1)));
    }

    public void addMono(String var, int pow) {
        Mono e = new Mono(var, pow);
        poly.add(e);
    }

    public void addMono(BigInteger coe) {
        Mono e = new Mono(coe);
        poly.add(e);
    }

    public void addMerge(Mono m) {
        HashMap<String, Integer> hm = m.getHash();
        int sign = 0;
        Mono temp = null;
        for (Mono mono : poly) {
            if (mono.getHash().equals(hm)) {
                sign = 1;
                temp = mono;
            }
        }
        if (sign == 0) {
            poly.add(m);
        } else {
            BigInteger coe = temp.getCoe().add(m.getCoe());
            temp.setCoe(coe);
        }
    }


    public Poly mulMerge(Mono m) {
        Poly ans = new Poly();
        for (Mono mono : poly) {
            Mono temp = new Mono(m, mono);
            ans.addMerge(temp);
        }
        return ans;
    }

    public Poly add(Poly p) {

        for (Mono mono : p.poly)
            this.addMerge(mono);
        return this;
    }

    public Poly mul(Poly p) {
        Poly ans = new Poly();
        for (Mono mono : p.poly) {
            ans = ans.add(this.mulMerge(mono));

        }
        return ans;
    }

    public Poly powCal(int pow) {
        Poly ans = new Poly();
        if (pow == 0) {
            ans.init();
        } else if (pow == 1) {
            ans = this;
        } else {
            ans = this;
            for (int i = 1; i < pow; i++) {
                ans = this.mul(ans);
            }
        }
        return ans;
    }
}
