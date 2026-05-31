package parser;

import ast.ASTNode;
import lexer.Token;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private ASTNode root = new ASTNode("Program");

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {

        while (!isAtEnd()) {

            if (matchKeyword("int") || matchKeyword("float")) {
                root.addChild(parseDeclaration());
            }
            else if (checkType("IDENTIFIER")) {
                root.addChild(parseAssignment());
            }
            else {
                error("Beklenmeyen token: " + peek().getValue());
                advance();
            }
        }

        System.out.println("\nSyntax analizi tamamlandı.");
        return root;
    }

    private ASTNode parseDeclaration() {

        Token type = previous();

        ASTNode declarationNode = new ASTNode("Declaration");
        declarationNode.addChild(new ASTNode("Type: " + type.getValue()));

        if (!matchType("IDENTIFIER")) {
            error("Değişken adı bekleniyor");
            return declarationNode;
        }

        Token identifier = previous();
        declarationNode.addChild(new ASTNode("Identifier: " + identifier.getValue()));

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }

        return declarationNode;
    }

    private ASTNode parseAssignment() {

        Token identifier = advance();

        ASTNode assignmentNode = new ASTNode("Assignment");
        assignmentNode.addChild(new ASTNode("Identifier: " + identifier.getValue()));

        if (!matchValue("=")) {
            error("'=' bekleniyor");
            return assignmentNode;
        }

        ASTNode expressionNode = new ASTNode("Expression");

        if (!(matchType("INTEGER_LITERAL")
                || matchType("FLOAT_LITERAL")
                || matchType("IDENTIFIER"))) {

            error("Atama değeri bekleniyor");
            return assignmentNode;
        }

        expressionNode.addChild(new ASTNode("Value: " + previous().getValue()));

        while (!isAtEnd() && !checkValue(";")) {
            Token operatorOrValue = advance();
            expressionNode.addChild(
                    new ASTNode(operatorOrValue.getType() + ": " + operatorOrValue.getValue())
            );
        }

        assignmentNode.addChild(expressionNode);

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }

        return assignmentNode;
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