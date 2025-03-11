import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> funcData = new ArrayList<>();
        String funcNum = scanner.nextLine();
        Func func = null;
        if (funcNum.equals("1")) {
            for (int i = 0; i < 3; i++) {
                funcData.add(scanner.nextLine());
            }
            Funcparser funcparser = new Funcparser(funcData);
            func = funcparser.parseFunc();
        }


        String input = scanner.nextLine();
        input = input.replaceAll("[ \t]", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer, func);
        Expr expr = parser.parseExpr();
        Poly answer = expr.cal();
        answer.print();
    }
}
