import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
       input= input.replaceAll("[ \t]", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr=parser.parseExpr();
          Poly answer=  expr.cal();
          answer.print();
    }
}
