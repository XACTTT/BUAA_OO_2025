import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> funcData = new ArrayList<>();
        String funcNum = scanner.nextLine();
    //    if (funcNum != 0){
    //        for (int i = 0; i < 3; i++){
    //            funcData.add(scanner.next());
    //        }
    //    }
    //    Funcparser funcparser = new Funcparser(funcData);
        String input = scanner.nextLine();
        input = input.replaceAll("[ \t]", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly answer = expr.cal();
        answer.print();
    }
}
