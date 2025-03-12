import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> normalFuncData = new ArrayList<>();
        ArrayList<String> funcData = new ArrayList<>();
        String funcNum1 = scanner.nextLine();
        int num = Integer.parseInt(funcNum1);
        for (int i = 0; i < num; i++) {
            normalFuncData.add(scanner.nextLine());
        }
        Func func = null;
        String funcNum2 = scanner.nextLine();
        if (funcNum2.equals("1")) {
            for (int i = 0; i < 3; i++) {
                funcData.add(scanner.nextLine());
            }
            Funcparser funcparser = new Funcparser(normalFuncData,funcData);
            func = funcparser.parseFunc();
        }

        String input = scanner.nextLine();
        input = input.replaceAll("[ \t]", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer, func);
        Expr expr = parser.parseExpr();
        Poly answer = expr.cal();
        answer.simplify();
        answer.print();
    }
}
