package gui;

import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String code;

        try {
            code = Files.readString(Path.of("program.txt"));
        } catch (IOException e) {
            System.out.println("Dosya okunamadı: " + e.getMessage());
            return;
        }

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(code);

        System.out.println("--- SOURCE CODE ---");
        System.out.println(code);

        System.out.println("--- TOKENS ---");
        for (Token token : tokens) {
            System.out.println(token);
        }

        lexer.getSymbolTable().printTable();

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        SemanticAnalyzer semanticAnalyzer =
                new SemanticAnalyzer(tokens, lexer.getSymbolTable());

        semanticAnalyzer.analyze();

        System.out.println("\n--- AST / PARSE TREE ---");
        ast.print("");
    }
}