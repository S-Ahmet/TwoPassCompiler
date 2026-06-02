package lexer;

import symboltable.SymbolTable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String[] keywords = {
            "int", "float", "if", "else", "while", "print"
    };

    private SymbolTable symbolTable = new SymbolTable();

    public String readSourceFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public List<Token> tokenize(String sourceCode) {
        symbolTable = new SymbolTable();
        List<Token> tokens = new ArrayList<>();

        int line = 1;
        int i = 0;

        while (i < sourceCode.length()) {
            char current = sourceCode.charAt(i);

            if (current == '\n') {
                line++;
                i++;
                continue;
            }

            if (Character.isWhitespace(current)) {
                i++;
                continue;
            }

            if (current == '/' && peek(sourceCode, i + 1) == '/') {
                i += 2;

                while (i < sourceCode.length() && sourceCode.charAt(i) != '\n') {
                    i++;
                }

                continue;
            }

            if (current == '/' && peek(sourceCode, i + 1) == '*') {
                int commentStartLine = line;
                i += 2;
                boolean closed = false;

                while (i < sourceCode.length()) {
                    if (sourceCode.charAt(i) == '\n') {
                        line++;
                    }

                    if (sourceCode.charAt(i) == '*' && peek(sourceCode, i + 1) == '/') {
                        i += 2;
                        closed = true;
                        break;
                    }

                    i++;
                }

                if (!closed) {
                    tokens.add(new Token("LEXICAL_ERROR", "Unclosed block comment", commentStartLine));
                }

                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                StringBuilder word = new StringBuilder();

                while (i < sourceCode.length() &&
                        (Character.isLetterOrDigit(sourceCode.charAt(i)) || sourceCode.charAt(i) == '_')) {
                    word.append(sourceCode.charAt(i));
                    i++;
                }

                String value = word.toString();
                tokens.add(new Token(isKeyword(value) ? "KEYWORD" : "IDENTIFIER", value, line));
                continue;
            }

            if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                boolean isFloat = false;
                int dotCount = 0;

                while (i < sourceCode.length() &&
                        (Character.isDigit(sourceCode.charAt(i)) || sourceCode.charAt(i) == '.')) {
                    if (sourceCode.charAt(i) == '.') {
                        isFloat = true;
                        dotCount++;
                    }

                    number.append(sourceCode.charAt(i));
                    i++;
                }

                if (dotCount > 1 || number.charAt(number.length() - 1) == '.') {
                    tokens.add(new Token("LEXICAL_ERROR", "Malformed number: " + number, line));
                } else if (isFloat) {
                    tokens.add(new Token("FLOAT_LITERAL", number.toString(), line));
                } else {
                    tokens.add(new Token("INTEGER_LITERAL", number.toString(), line));
                }

                continue;
            }

            if (current == '"') {
                StringBuilder text = new StringBuilder();
                int stringStartLine = line;
                i++;

                while (i < sourceCode.length() && sourceCode.charAt(i) != '"') {
                    if (sourceCode.charAt(i) == '\n') {
                        line++;
                    }

                    text.append(sourceCode.charAt(i));
                    i++;
                }

                if (i < sourceCode.length() && sourceCode.charAt(i) == '"') {
                    i++;
                    tokens.add(new Token("STRING_LITERAL", text.toString(), stringStartLine));
                } else {
                    tokens.add(new Token("LEXICAL_ERROR", "Unclosed string literal", stringStartLine));
                }

                continue;
            }

            if (i + 1 < sourceCode.length()) {
                String twoChar = sourceCode.substring(i, i + 2);

                if (twoChar.equals("==") || twoChar.equals("!=") ||
                        twoChar.equals("<=") || twoChar.equals(">=") ||
                        twoChar.equals("&&") || twoChar.equals("||")) {
                    tokens.add(new Token("OPERATOR", twoChar, line));
                    i += 2;
                    continue;
                }
            }

            if ("+-*/=<>!".indexOf(current) != -1) {
                tokens.add(new Token("OPERATOR", String.valueOf(current), line));
                i++;
                continue;
            }

            if (";(),{}".indexOf(current) != -1) {
                tokens.add(new Token("DELIMITER", String.valueOf(current), line));
                i++;
                continue;
            }

            tokens.add(new Token("LEXICAL_ERROR", "Invalid character: " + current, line));
            i++;
        }

        buildSymbolTable(tokens);
        return tokens;
    }

    private char peek(String sourceCode, int index) {
        if (index >= sourceCode.length()) {
            return '\0';
        }

        return sourceCode.charAt(index);
    }

    private void buildSymbolTable(List<Token> tokens) {
        for (int i = 0; i < tokens.size() - 2; i++) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);
            Token afterNext = tokens.get(i + 2);

            if (isTypeKeyword(current) &&
                    next.getType().equals("IDENTIFIER") &&
                    afterNext.getValue().equals(";")) {
                symbolTable.addSymbol(next.getValue(), current.getValue(), "global");
            }
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    private boolean isKeyword(String word) {
        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTypeKeyword(Token token) {
        return token.getType().equals("KEYWORD") &&
                (token.getValue().equals("int") || token.getValue().equals("float"));
    }
}
