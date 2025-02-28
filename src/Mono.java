import java.math.BigInteger;
import java.util.HashMap;

public class Mono {
    private BigInteger coe;
    private HashMap<String,Integer> Vs;
    public Mono(BigInteger coe) {
        this.coe = coe;
        Vs = new HashMap<>();

    }
    public Mono(String var,  int pow) {
        this.coe =  BigInteger.valueOf(1);
        this.Vs = new HashMap<>();
        this.Vs.put(var, pow);
    }



        public HashMap<String, Integer> mulHash(HashMap<String, Integer> poly1, HashMap<String, Integer> poly2) {

            HashMap<String, Integer> result = new HashMap<>();


            for (String term1 : poly1.keySet()) {
                for (String term2 : poly2.keySet()) {
                   if(term1.equals(term2)){int combinedExponent = poly1.get(term1) + poly2.get(term2);
                       result.put(term1, result.getOrDefault(term1, 0) + combinedExponent);}
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



    public Mono(Mono a, Mono b) {
        this.coe = a.coe.multiply(b.coe);
        this.Vs=mulHash(a.Vs, b.Vs);
    }
//    public static HashMap<String, Integer> mulHash(HashMap<String, Integer> poly1, HashMap<String, Integer> poly2) {
//
//        HashMap<String, Integer> result = new HashMap<>();
//
//
//        for (String term1 : poly1.keySet()) {
//            for (String term2 : poly2.keySet()) {
//
//                String combinedTerm = term1 + term2;
//                int combinedExponent = poly1.get(term1) + poly2.get(term2);
//
//                result.put(combinedTerm, result.getOrDefault(combinedTerm, 0) + combinedExponent);
//            }
//        }
//
//        return result;
//    }
    public BigInteger getCoe(){
        return this.coe;
    }
    public HashMap<String,Integer> getHash(){
        return Vs;
    }

    public void setCoe(BigInteger coe){
        this.coe = coe;
    }
}
