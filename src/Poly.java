import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Poly {
    private final ArrayList<Unit> poly = new ArrayList<>();

    public void print() {
        System.out.println(this.toString1());
    }

    public String toString1() { //print之前先化简，要求sin和cos里不能为空
        StringBuilder sb = new StringBuilder();
        int first = 0;
        int printSign = 0;
        for (Unit unit : poly) {
            int sign = 0;
            if (!unit.getCoe().equals(BigInteger.ZERO)) {
                printSign = 1;
                if (unit.getCoe().equals(BigInteger.ONE)) {
                    if (first != 0) {
                        sb.append('+');
                    }
                } else if (unit.getCoe().equals(BigInteger.valueOf(-1))) {
                    sb.append('-');
                } else {
                    if (first != 0) {
                        if (unit.getCoe().compareTo(BigInteger.valueOf(0)) > 0) {
                            sb.append('+');
                        }
                    }
                    sb.append(unit.getCoe());
                    sign = 1;
                }
                HashMap<String, Integer> a = unit.getHash();
                HashMap<Poly, Integer> sinMap = unit.getsinHash();
                HashMap<Poly, Integer> cosMap = unit.getcosHash();
                if (!(a.isEmpty() && sinMap.isEmpty() && cosMap.isEmpty())) {
                    int len1 = sb.length();
                    sb.append(printVar(a, sign));
                    if (len1 != sb.length()) {
                        sign = 1;
                    }
                    int len2 = sb.length();
                    sb.append(printSin(sinMap, sign));
                    if (len2 != sb.length()) {
                        sign = 1;
                    }
                    sb.append(printCos(cosMap, sign));

                } else {
                    if (sign == 0) {
                        sb.append(1);
                    }
                }
            }
            first = 1;
        }
        if (printSign == 0) {
            sb.append("0");
        }
        return sb.toString();
    }

    public String printVar(HashMap<String, Integer> a, int sign1) {
        StringBuilder sb = new StringBuilder();
        int sign = sign1;
        for (HashMap.Entry<String, Integer> entry : a.entrySet()) {
            String k = entry.getKey();
            Integer v = entry.getValue();
            if (sign == 1) {
                if (v > 1) {
                    sb.append("*").append(k).append("^").append(v);
                } else if (v == 1) {
                    sb.append("*").append(k);
                }
            } else {
                if (v > 1) {
                    sb.append(k).append("^").append(v);
                    sign = 1;
                } else if (v == 1) {
                    sb.append(k);
                    sign = 1;
                } else {
                    sb.append(1);
                    sign = 1;
                }
            }
        }
        return sb.toString();
    }

    public String printSin(HashMap<Poly, Integer> sinMap, int sign1) {
        StringBuilder sb = new StringBuilder();
        int sign = sign1;
        for (HashMap.Entry<Poly, Integer> entry : sinMap.entrySet()) {
            Poly p = entry.getKey();
            Integer v = entry.getValue();
            if (sign == 1) {
                //System.out.print("*sin(");
                sb.append("*sin(");
                if (p.isFactor()) {
                    sb.append(p.toString1());
                    // System.out.print(")");
                    sb.append(")");
                } else {
                    // System.out.print("(");
                    sb.append("(");
                    sb.append(p.toString1());
                    //System.out.print("))");
                    sb.append("))");
                }
                if (v > 1) {
                    //System.out.print("^" + v);
                    sb.append("^").append(v);
                }
            } else {
                if (v >= 1) {
                    //System.out.print("sin(");
                    sb.append("sin(");
                    if (p.isFactor()) {
                        sb.append(p.toString1());
                        // System.out.print(")");
                        sb.append(")");
                    } else {
                        // System.out.print("(");
                        sb.append("(");
                        sb.append(p.toString1());
                        //System.out.print("))");
                        sb.append("))");
                    }
                    sign = 1;
                    if (v != 1) {
                        //System.out.print("^" + v);
                        sb.append("^").append(v);
                    }

                } else {
                    // System.out.print(1);
                    sb.append("1");
                    sign = 1;
                }
            }
        }
        return sb.toString();
    }

    public String printCos(HashMap<Poly, Integer> cosMap, int sign1) {
        StringBuilder sb = new StringBuilder();
        int sign = sign1;
        for (HashMap.Entry<Poly, Integer> entry : cosMap.entrySet()) {
            Poly p = entry.getKey();
            Integer v = entry.getValue();
            if (!p.isZero()) {
                if (sign == 1) {
                    //System.out.print("*sin(");
                    sb.append("*cos(");
                    if (p.isFactor()) {
                        sb.append(p.toString1());
                        // System.out.print(")");
                        sb.append(")");
                    } else {
                        // System.out.print("(");
                        sb.append("(");
                        sb.append(p.toString1());
                        //System.out.print("))");
                        sb.append("))");
                    }
                    if (v > 1) {
                        //System.out.print("^" + v);
                        sb.append("^").append(v);
                    }
                } else {
                    if (v >= 1) {
                        //System.out.print("sin(");
                        sb.append("cos(");
                        if (p.isFactor()) {
                            sb.append(p.toString1());
                            // System.out.print(")");
                            sb.append(")");
                        } else {
                            // System.out.print("(");
                            sb.append("(");
                            sb.append(p.toString1());
                            //System.out.print("))");
                            sb.append("))");
                        }
                        sign = 1;
                        if (v != 1) {
                            //System.out.print("^" + v);
                            sb.append("^").append(v);
                        }

                    } else {
                        // System.out.print(1);
                        sb.append("1");
                        sign = 1;
                    }
                }
            }
        }
        if (sign == 0) {
            sb.append("1");
        }
        return sb.toString();
    }

    public void init() {
        poly.add(new Unit(BigInteger.valueOf(1)));
    }

    public void addUnit(String var, int pow) {
        Unit e = new Unit(var, pow);
        poly.add(e);
    }

    public void addUnit(BigInteger coe) {
        Unit e = new Unit(coe);
        poly.add(e);
    }

    public void addUnit(String name, Expr expr, int pow) {
        Unit e = new Unit(name, expr, pow);
        poly.add(e);
    }

    public void addMerge(Unit m) {

        int sign = 0;
        Unit temp = null;
        for (Unit unit : poly) {
            if (unit.equals(m)) {
                sign = 1;
                temp = unit;
            }
        }
        if (sign == 0) {
            poly.add(m);
        } else {
            BigInteger coe = temp.getCoe().add(m.getCoe());
            if (!coe.equals(BigInteger.ZERO)) {
                temp.setCoe(coe);
            } else {
                poly.remove(temp);
            }

        }
        // 要重写，支持三角函数add合并
    }

    public Poly mulMerge(Unit m) {
        Poly ans = new Poly();
        for (Unit unit : poly) {
            Unit temp = new Unit(m, unit);
            ans.addMerge(temp);
        }
        return ans;
    }

    public Poly add(Poly p) {

        for (Unit unit : p.poly) {
            this.addMerge(unit);
        }
        return this;
    }

    public Poly mul(Poly p) {
        Poly ans = new Poly();
        for (Unit unit : p.poly) {
            ans = ans.add(this.mulMerge(unit));

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

    public boolean equals(Poly p) {
        int sign1 = 1;
        for (Unit unit : this.poly) {
            int sign2 = 0;
            for (Unit unit1 : p.poly) {
                if (unit.equals(unit1) && unit1.getCoe().equals(unit.getCoe())) {
                    sign2 = 1;
                }
            }
            if (sign2 != 1) {
                sign1 = 0;
            }
        }

        int sign3 = 1;
        for (Unit unit : p.poly) {
            int sign4 = 0;
            for (Unit unit1 : this.poly) {
                if (unit.equals(unit1) && unit1.getCoe().equals(unit.getCoe())) {
                    sign4 = 1;
                }
            }
            if (sign4 != 1) {
                sign3 = 0;
            }
        }
        return sign1 == 1 && sign3 == 1;
    }

    public boolean isFactor() {
        if (this.poly.size() == 1) {
            return poly.get(0).isFactor();
        } else {
            return false;
        }
    }

    public Poly deepClone() {
        Poly cloned = new Poly();
        for (Unit unit : this.poly) {
            cloned.poly.add(unit.deepClone());
        }
        return cloned;
    }

    public void simplify() {
        for (Unit unit : poly) {
            if (unit.isZero()) {
                unit.setCoe(new BigInteger("0"));
            }
        }

    }

    public boolean isZero() {
        int zerosin = 1;
        for (Unit unit : poly) {
            if (!unit.isZero()) {
                zerosin = 0;
            }
        }
        return zerosin == 1;
    }

}
