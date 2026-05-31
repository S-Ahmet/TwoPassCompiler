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
        checkTypeCompatibility();

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

            if (isTypeKeyword(current) && next.getType().equals("IDENTIFIER")) {
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

            if (isTypeKeyword(current) && next.getType().equals("IDENTIFIER")) {
                declared.add(next.getValue());
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);

            if (current.getType().equals("IDENTIFIER")) {
                boolean isDeclarationName =
                        i > 0 && isTypeKeyword(tokens.get(i - 1));

                if (!isDeclarationName && !declared.contains(current.getValue())) {
                    errors.add("SEMANTIC ERROR -> Line " + current.getLine()
                            + " : '" + current.getValue() + "' değişkeni tanımlanmadan kullanılmış.");
                }
            }
        }
    }

    private void checkTypeCompatibility() {
        for (int i = 0; i < tokens.size() - 2; i++) {

            Token variable = tokens.get(i);
            Token equalSign = tokens.get(i + 1);
            Token value = tokens.get(i + 2);

            if (variable.getType().equals("IDENTIFIER") && equalSign.getValue().equals("=")) {

                String variableType = symbolTable.getType(variable.getValue(), "global");

                if (variableType == null) {
                    continue;
                }

                String valueType = getValueType(value);

                if (valueType == null) {
                    continue;
                }

                if (variableType.equals("int") && !valueType.equals("int")) {
                    errors.add("SEMANTIC ERROR -> Line " + variable.getLine()
                            + " : int değişkene " + valueType + " değer atanamaz.");
                }

                if (variableType.equals("float") && valueType.equals("string")) {
                    errors.add("SEMANTIC ERROR -> Line " + variable.getLine()
                            + " : float değişkene string değer atanamaz.");
                }
            }
        }
    }

    private String getValueType(Token token) {
        if (token.getType().equals("INTEGER_LITERAL")) {
            return "int";
        }

        if (token.getType().equals("FLOAT_LITERAL")) {
            return "float";
        }

        if (token.getType().equals("STRING_LITERAL")) {
            return "string";
        }

        if (token.getType().equals("IDENTIFIER")) {
            return symbolTable.getType(token.getValue(), "global");
        }

        return null;
    }

    private boolean isTypeKeyword(Token token) {
        return token.getType().equals("KEYWORD") &&
                (token.getValue().equals("int") || token.getValue().equals("float"));
    }

    public List<String> getErrors() {
        return errors;
    }
}