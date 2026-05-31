package parser;

import lexer.Token;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() {

        while (!isAtEnd()) {

            if (matchKeyword("int") || matchKeyword("float")) {
                parseDeclaration();
            }
            else if (checkType("IDENTIFIER")) {
                parseAssignment();
            }
            else {
                error("Beklenmeyen token: " + peek().getValue());
                advance();
            }
        }

        System.out.println("\nSyntax analizi tamamlandı.");
    }

    private void parseDeclaration() {

        Token type = previous();

        if (!matchType("IDENTIFIER")) {
            error("Değişken adı bekleniyor");
            return;
        }

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }
    }

    private void parseAssignment() {

        advance();

        if (!matchValue("=")) {
            error("'=' bekleniyor");
            return;
        }

        if (!(matchType("INTEGER_LITERAL")
                || matchType("FLOAT_LITERAL")
                || matchType("IDENTIFIER"))) {

            error("Atama değeri bekleniyor");
            return;
        }

        while (!isAtEnd() && !checkValue(";")) {
            advance();
        }

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }
    }

    private boolean matchKeyword(String keyword) {

        if (checkValue(keyword)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean matchType(String type) {

        if (checkType(type)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean matchValue(String value) {

        if (checkValue(value)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean checkType(String type) {

        if (isAtEnd()) return false;

        return peek().getType().equals(type);
    }

    private boolean checkValue(String value) {

        if (isAtEnd()) return false;

        return peek().getValue().equals(value);
    }

    private Token advance() {

        if (!isAtEnd()) current++;

        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(String message) {

        System.out.println(
                "SYNTAX ERROR -> Line "
                        + peek().getLine()
                        + " : "
                        + message
        );
    }
}