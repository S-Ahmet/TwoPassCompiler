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
            ASTNode statement = parseStatement();

            if (statement != null) {
                root.addChild(statement);
            }
        }

        System.out.println("\nSyntax analizi tamamlandı.");
        return root;
    }

    private ASTNode parseStatement() {
        if (matchKeyword("int") || matchKeyword("float")) {
            return parseDeclaration();
        }

        if (matchKeyword("if")) {
            return parseIfStatement();
        }

        if (matchKeyword("while")) {
            return parseWhileStatement();
        }

        if (matchKeyword("print")) {
            return parsePrintStatement();
        }

        if (checkType("IDENTIFIER")) {
            return parseAssignment();
        }

        error("Beklenmeyen token: " + peek().getValue());
        advance();
        return null;
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

        ASTNode expressionNode = parseExpressionUntil(";");
        assignmentNode.addChild(expressionNode);

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }

        return assignmentNode;
    }

    private ASTNode parseIfStatement() {
        ASTNode ifNode = new ASTNode("If Statement");

        if (!matchValue("(")) {
            error("'(' bekleniyor");
            return ifNode;
        }

        ASTNode conditionNode = parseExpressionUntil(")");
        ifNode.addChild(new ASTNode("Condition"));
        ifNode.getChildren().get(ifNode.getChildren().size() - 1).addChild(conditionNode);

        if (!matchValue(")")) {
            error("')' bekleniyor");
        }

        ASTNode ifBlock = parseBlock("If Block");
        ifNode.addChild(ifBlock);

        if (!isAtEnd() && checkValue("else")) {
            advance();

            ASTNode elseBlock = parseBlock("Else Block");
            ifNode.addChild(elseBlock);
        }

        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        ASTNode whileNode = new ASTNode("While Statement");

        if (!matchValue("(")) {
            error("'(' bekleniyor");
            return whileNode;
        }

        ASTNode conditionNode = parseExpressionUntil(")");
        ASTNode conditionWrapper = new ASTNode("Condition");
        conditionWrapper.addChild(conditionNode);
        whileNode.addChild(conditionWrapper);

        if (!matchValue(")")) {
            error("')' bekleniyor");
        }

        ASTNode whileBlock = parseBlock("While Block");
        whileNode.addChild(whileBlock);

        return whileNode;
    }

    private ASTNode parsePrintStatement() {
        ASTNode printNode = new ASTNode("Print Statement");

        if (!matchValue("(")) {
            error("'(' bekleniyor");
            return printNode;
        }

        ASTNode expressionNode = parseExpressionUntil(")");
        printNode.addChild(expressionNode);

        if (!matchValue(")")) {
            error("')' bekleniyor");
        }

        if (!matchValue(";")) {
            error("';' bekleniyor");
        }

        return printNode;
    }

    private ASTNode parseBlock(String blockName) {
        ASTNode blockNode = new ASTNode(blockName);

        if (!matchValue("{")) {
            error("'{' bekleniyor");
            return blockNode;
        }

        while (!isAtEnd() && !checkValue("}")) {
            ASTNode statement = parseStatement();

            if (statement != null) {
                blockNode.addChild(statement);
            }
        }

        if (!matchValue("}")) {
            error("'}' bekleniyor");
        }

        return blockNode;
    }

    private ASTNode parseExpressionUntil(String endValue) {
        ASTNode expressionNode = new ASTNode("Expression");

        while (!isAtEnd() && !checkValue(endValue)) {
            Token token = advance();
            expressionNode.addChild(new ASTNode(token.getType() + ": " + token.getValue()));
        }

        return expressionNode;
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
        if (!isAtEnd()) {
            System.out.println(
                    "SYNTAX ERROR -> Line "
                            + peek().getLine()
                            + " : "
                            + message
            );
        } else {
            System.out.println("SYNTAX ERROR -> Dosya sonunda hata: " + message);
        }
    }
}