package gui;

import lexer.Lexer;
import lexer.Token;
import parser.Parser;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        String code = """
                int x;
                int y;
                float result;

                x = 10;
                y = 3;
                result = x + y * 2;
                """;

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(code);

        System.out.println("--- TOKENS ---");
        for (Token token : tokens) {
            System.out.println(token);
        }

        lexer.getSymbolTable().printTable();

        Parser parser = new Parser(tokens);
        parser.parse();
    }
}