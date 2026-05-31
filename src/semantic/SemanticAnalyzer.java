package semantic;

import lexer.Token;
import symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticAnalyzer {

    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final List<String> errors = new ArrayList<>();

    public SemanticAnalyzer(List<Token> tokens, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
    }

    public void analyze() {
        checkDuplicateDeclarations();
        checkUndeclaredVariables();

        System.out.println("\n--- SEMANTIC ANALYSIS ---");

        if (errors.isEmpty()) {
            System.out.println("Semantic analiz tamamlandı. Hata yok.");
        } else {
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }

    private void checkDuplicateDeclarations() {
        Set<String> declared = new HashSet<>();

        for (int i = 0; i < tokens.size() - 1; i++) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);

            if (current.getType().equals("KEYWORD") &&
                    (current.getValue().equals("int") || current.getValue().equals("float")) &&
                    next.getType().equals("IDENTIFIER")) {

                String variableName = next.getValue();

                if (declared.contains(variableName)) {
                    errors.add("SEMANTIC ERROR -> Line " + next.getLine()
                            + " : '" + variableName + "' değişkeni zaten tanımlanmış.");
                } else {
                    declared.add(variableName);
                }
            }
        }
    }

    private void checkUndeclaredVariables() {
        Set<String> declared = new HashSet<>();

        for (int i = 0; i < tokens.size() - 1; i++) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);

            if (current.getType().equals("KEYWORD") &&
                    (current.getValue().equals("int") || current.getValue().equals("float")) &&
                    next.getType().equals("IDENTIFIER")) {

                declared.add(next.getValue());
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);

            if (current.getType().equals("IDENTIFIER")) {

                boolean isDeclarationName =
                        i > 0 &&
                                tokens.get(i - 1).getType().equals("KEYWORD") &&
                                (tokens.get(i - 1).getValue().equals("int") ||
                                        tokens.get(i - 1).getValue().equals("float"));

                if (!isDeclarationName && !declared.contains(current.getValue())) {
                    errors.add("SEMANTIC ERROR -> Line " + current.getLine()
                            + " : '" + current.getValue() + "' değişkeni tanımlanmadan kullanılmış.");
                }
            }
        }
    }

    public List<String> getErrors() {
        return errors;
    }
}