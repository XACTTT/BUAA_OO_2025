import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Unit {
    private BigInteger coe;
    private HashMap<String, Integer> vs;
    private HashMap<Poly, Integer> sinMap;
    private HashMap<Poly, Integer> cosMap;

    public Unit() {
        coe = new BigInteger("0");
        vs = new HashMap<>();
        sinMap = new HashMap<>();
        cosMap = new HashMap<>();
    }

    public Unit(BigInteger coe) {
        this.coe = coe;
        vs = new HashMap<>();
        sinMap = new HashMap<>();
        cosMap = new HashMap<>();
    }

    public Unit(String name, Expr expr, int pow) {
        this.coe = BigInteger.valueOf(1);
        this.vs = new HashMap<>();
        this.sinMap = new HashMap<>();
        this.cosMap = new HashMap<>();
        if (name.equals("sin")) {
            this.sinMap.put(expr.cal(), pow);//expr在这才计算
        } else {
            this.cosMap.put(expr.cal(), pow);
        }
    }

    public Unit(String var, int pow) {
        this.coe = BigInteger.valueOf(1);
        this.vs = new HashMap<>();
        this.vs.put(var, pow);
        this.sinMap = new HashMap<>();
        this.cosMap = new HashMap<>();
    }

    public Unit(Unit m, Unit n) {
        Unit a = m.deepClone();
        Unit b = n.deepClone();
        this.coe = a.coe.multiply(b.coe);
        this.vs = mulHash(a.vs, b.vs);
        this.sinMap = multriHash(a.sinMap, b.sinMap);
        this.cosMap = multriHash(a.cosMap, b.cosMap);
    }

    public HashMap<String, Integer>
    mulHash(HashMap<String, Integer> poly1, HashMap<String, Integer> poly2) {

        HashMap<String, Integer> result = new HashMap<>();

        for (String term1 : poly1.keySet()) {
            for (String term2 : poly2.keySet()) {
                if (term1.equals(term2)) {
                    int combinedExponent = poly1.get(term1) + poly2.get(term2);
                    result.put(term1, result.getOrDefault(term1, 0) + combinedExponent);
                }
            }
        }

        for (String term1 : poly1.keySet()) {
            if (!result.containsKey(term1)) {
                result.put(term1, poly1.get(term1));
            }
        }

        for (String term2 : poly2.keySet()) {
            if (!result.containsKey(term2)) {
                result.put(term2, poly2.get(term2));
            }
        }

        return result;
    }

    public HashMap<Poly, Integer>
    multriHash(HashMap<Poly, Integer> poly1, HashMap<Poly, Integer> poly2) {
        HashMap<Poly, Integer> result = new HashMap<>();
        Poly temp = null;
        for (Poly term1 : poly1.keySet()) {
            int isFind = 0;
            for (Poly term2 : poly2.keySet()) {
                if (term1.equals(term2)) {
                    isFind = 1;
                    int value = poly1.get(term1) + poly2.get(term2);
                    result.put(term1, value);
                    temp = term2;
                }
            }
            if (isFind == 0) {
                result.put(term1, poly1.get(term1));
            } else {
                poly2.remove(temp);
            }
        }
        for (Poly term1 : poly2.keySet()) {
            result.put(term1, poly2.get(term1));
        }
        return result;
    }

    public BigInteger getCoe() {
        return this.coe;
    }

    public void setCoe(BigInteger coe) {
        this.coe = coe;
    }

    public HashMap<String, Integer> getHash() {
        return vs;
    }

    public HashMap<Poly, Integer> getsinHash() {
        return sinMap;
    }

    public HashMap<Poly, Integer> getcosHash() {
        return cosMap;
    }

    public boolean equals(Unit unit) {
        if (this.varequals(this.vs, unit.vs)) {
            return hashEquals(this.sinMap, unit.sinMap) && hashEquals(this.cosMap, unit.cosMap);
        } else {
            return false;
        }
    }

    public boolean varequals(HashMap<String, Integer> map1, HashMap<String, Integer> map2) {
        int sign1 = 1;
        for (String string1 : map1.keySet()) {
            int sign2 = 0;
            for (String string2 : map2.keySet()) {
                if (string1.equals(string2) && map1.get(string1).equals(map2.get(string2))) {
                    sign2 = 1;
                }
            }
            if (sign2 != 1) {
                sign1 = 0;
            }
        }

        int sign3 = 1;
        for (String string1 : map2.keySet()) {
            int sign4 = 0;
            for (String string2 : map1.keySet()) {
                if (string1.equals(string2) && map1.get(string1).equals(map2.get(string2))) {
                    sign4 = 1;
                }
            }
            if (sign4 != 1) {
                sign3 = 0;
            }
        }
        return sign1 == 1 && sign3 == 1;

    }

    public boolean hashEquals(HashMap<Poly, Integer> map1, HashMap<Poly, Integer> map2) {
        int sign1 = 1;
        for (Poly poly1 : map1.keySet()) {
            int sign2 = 0;
            for (Poly poly2 : map2.keySet()) {
                if (poly1.equals(poly2) && map1.get(poly1).equals(map2.get(poly2))) {
                    sign2 = 1;
                }
            }
            if (sign2 != 1) {
                sign1 = 0;
            }
        }

        int sign3 = 1;
        for (Poly poly1 : map2.keySet()) {
            int sign4 = 0;
            for (Poly poly2 : map1.keySet()) {
                if (poly1.equals(poly2)) {
                    if (map1.get(poly2).equals(map2.get(poly1))) {
                        sign4 = 1;
                    }
                }
            }
            if (sign4 != 1) {
                sign3 = 0;
            }
        }
        return sign1 == 1 && sign3 == 1;

    }

    public boolean isFactor() {
        if (!this.vs.isEmpty() && this.sinMap.isEmpty() && this.cosMap.isEmpty() &&
                this.coe.equals(BigInteger.valueOf(1))) {
            return true;
        }
        if (this.vs.isEmpty() && this.sinMap.size() == 1 && this.cosMap.isEmpty() &&
                this.coe.equals(BigInteger.valueOf(1))) {
            return true;
        }
        if (this.vs.isEmpty() && this.sinMap.isEmpty() && this.cosMap.size() == 1 &&
                this.coe.equals(BigInteger.valueOf(1))) {
            return true;
        }
        return this.vs.isEmpty() && this.sinMap.isEmpty() && this.cosMap.isEmpty();
    }

    public Unit deepClone() {
        Unit cloned = new Unit();

        // 1. 克隆不可变对象 coe
        cloned.coe = this.coe; // BigInteger 不可变，直接引用

        // 2. 克隆 vs（String 和 Integer 均不可变）
        cloned.vs = new HashMap<>();
        for (Map.Entry<String, Integer> entry : this.vs.entrySet()) {
            cloned.vs.put(entry.getKey(), entry.getValue());
        }

        // 3. 深克隆 sinMap
        cloned.sinMap = new HashMap<>();
        for (Map.Entry<Poly, Integer> entry : this.sinMap.entrySet()) {
            // 深克隆 Poly 作为 key
            Poly clonedKey = entry.getKey().deepClone();
            cloned.sinMap.put(clonedKey, entry.getValue());
        }

        // 4. 深克隆 cosMap
        cloned.cosMap = new HashMap<>();
        for (Map.Entry<Poly, Integer> entry : this.cosMap.entrySet()) {
            Poly clonedKey = entry.getKey().deepClone();
            cloned.cosMap.put(clonedKey, entry.getValue());
        }

        return cloned;
    }
}