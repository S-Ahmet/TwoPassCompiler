package gui;

import lexer.Lexer;
import lexer.Token;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        String code = """
                int x
                float y
                x 10
                y 5.5
                """;

        Lexer lexer = new Lexer();

        List<Token> tokens = lexer.tokenize(code);

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}