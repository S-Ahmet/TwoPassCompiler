package parser;

import ast.ASTNode;
import lexer.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private final List<String> errors = new ArrayList<>();
    private int current = 0;
    private ASTNode root = new ASTNode("Program");
    private List<String> expressionEndValues = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        current = 0;
        errors.clear();
        root = new ASTNode("Program");

        while (!isAtEnd()) {
            ASTNode statement = parseStatement();

            if (statement != null) {
                root.addChild(statement);
            }
        }

        return root;
    }

    public List<String> getErrors() {
        return errors;
    }

    private ASTNode parseStatement() {
        if (checkType("LEXICAL_ERROR")) {
            advance();
            return null;
        }

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

        error("Unexpected token: " + peek().getValue());
        advance();
        return null;
    }

    private ASTNode parseDeclaration() {
        Token type = previous();

        ASTNode declarationNode = new ASTNode("Declaration");
        declarationNode.addChild(new ASTNode("Type: " + type.getValue()));

        if (!matchType("IDENTIFIER")) {
            error("Variable name expected");
            synchronizeTo(";");
            matchValue(";");
            return declarationNode;
        }

        Token identifier = previous();
        declarationNode.addChild(new ASTNode("Identifier: " + identifier.getValue()));

        if (!matchValue(";")) {
            error("';' expected");
            synchronizeTo(";");
            matchValue(";");
        }

        return declarationNode;
    }

    private ASTNode parseAssignment() {
        Token identifier = advance();

        ASTNode assignmentNode = new ASTNode("Assignment");
        assignmentNode.addChild(new ASTNode("Identifier: " + identifier.getValue()));

        if (!matchValue("=")) {
            error("'=' expected");
            synchronizeTo(";");
            matchValue(";");
            return assignmentNode;
        }

        assignmentNode.addChild(parseExpression(";"));

        if (!matchValue(";")) {
            error("';' expected");
            synchronizeTo(";");
            matchValue(";");
        }

        return assignmentNode;
    }

    private ASTNode parseIfStatement() {
        ASTNode ifNode = new ASTNode("If Statement");

        if (!matchValue("(")) {
            error("'(' expected after if");
            return ifNode;
        }

        ASTNode conditionWrapper = new ASTNode("Condition");
        conditionWrapper.addChild(parseExpression(")"));
        ifNode.addChild(conditionWrapper);

        if (!matchValue(")")) {
            error("')' expected after if condition");
        }

        ifNode.addChild(parseBlock("If Block"));

        if (!isAtEnd() && checkValue("else")) {
            advance();
            ifNode.addChild(parseBlock("Else Block"));
        }

        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        ASTNode whileNode = new ASTNode("While Statement");

        if (!matchValue("(")) {
            error("'(' expected after while");
            return whileNode;
        }

        ASTNode conditionWrapper = new ASTNode("Condition");
        conditionWrapper.addChild(parseExpression(")"));
        whileNode.addChild(conditionWrapper);

        if (!matchValue(")")) {
            error("')' expected after while condition");
        }

        whileNode.addChild(parseBlock("While Block"));
        return whileNode;
    }

    private ASTNode parsePrintStatement() {
        ASTNode printNode = new ASTNode("Print Statement");

        if (!matchValue("(")) {
            error("'(' expected after print");
            return printNode;
        }

        printNode.addChild(parseExpression(")"));

        if (!matchValue(")")) {
            error("')' expected after print expression");
        }

        if (!matchValue(";")) {
            error("';' expected after print statement");
            synchronizeTo(";");
            matchValue(";");
        }

        return printNode;
    }

    private ASTNode parseBlock(String blockName) {
        ASTNode blockNode = new ASTNode(blockName);

        if (!matchValue("{")) {
            error("'{' expected");
            return blockNode;
        }

        while (!isAtEnd() && !checkValue("}")) {
            ASTNode statement = parseStatement();

            if (statement != null) {
                blockNode.addChild(statement);
            }
        }

        if (!matchValue("}")) {
            error("'}' expected");
        }

        return blockNode;
    }

    private ASTNode parseExpression(String... endValues) {
        List<String> previousEndValues = expressionEndValues;
        expressionEndValues = Arrays.asList(endValues);

        if (isAtEnd() || isExpressionEnd()) {
            error("Expression expected");
            expressionEndValues = previousEndValues;
            return new ASTNode("Missing Expression");
        }

        ASTNode expression = parseLogicalOr();

        while (!isAtEnd() && !isExpressionEnd()) {
            if (isRecoveryBoundary()) {
                break;
            }

            error("Unexpected token in expression: " + peek().getValue());
            advance();
        }

        expressionEndValues = previousEndValues;
        return expression;
    }

    private ASTNode parseLogicalOr() {
        ASTNode node = parseLogicalAnd();

        while (matchOperator("||")) {
            node = binaryNode(previous(), node, parseLogicalAnd());
        }

        return node;
    }

    private ASTNode parseLogicalAnd() {
        ASTNode node = parseEquality();

        while (matchOperator("&&")) {
            node = binaryNode(previous(), node, parseEquality());
        }

        return node;
    }

    private ASTNode parseEquality() {
        ASTNode node = parseComparison();

        while (matchOperator("==") || matchOperator("!=")) {
            node = binaryNode(previous(), node, parseComparison());
        }

        return node;
    }

    private ASTNode parseComparison() {
        ASTNode node = parseTerm();

        while (matchOperator("<") || matchOperator(">") ||
                matchOperator("<=") || matchOperator(">=")) {
            node = binaryNode(previous(), node, parseTerm());
        }

        return node;
    }

    private ASTNode parseTerm() {
        ASTNode node = parseFactor();

        while (matchOperator("+") || matchOperator("-")) {
            node = binaryNode(previous(), node, parseFactor());
        }

        return node;
    }

    private ASTNode parseFactor() {
        ASTNode node = parseUnary();

        while (matchOperator("*") || matchOperator("/")) {
            node = binaryNode(previous(), node, parseUnary());
        }

        return node;
    }

    private ASTNode parseUnary() {
        if (matchOperator("!") || matchOperator("-") || matchOperator("+")) {
            Token operator = previous();
            ASTNode node = new ASTNode("Unary Operator: " + operator.getValue());
            node.addChild(parseUnary());
            return node;
        }

        return parsePrimary();
    }

    private ASTNode parsePrimary() {
        if (matchType("IDENTIFIER") || matchType("INTEGER_LITERAL") ||
                matchType("FLOAT_LITERAL") || matchType("STRING_LITERAL")) {
            Token token = previous();
            return new ASTNode(token.getType() + ": " + token.getValue());
        }

        if (matchValue("(")) {
            ASTNode node = new ASTNode("Grouped Expression");
            node.addChild(parseExpression(")"));

            if (!matchValue(")")) {
                error("')' expected in expression");
            }

            return node;
        }

        if (!isAtEnd()) {
            error("Expression value expected, found: " + peek().getValue());

            if (!isExpressionEnd() && !isRecoveryBoundary()) {
                advance();
            }
        } else {
            error("Expression value expected at end of file");
        }

        return new ASTNode("Invalid Expression");
    }

    private ASTNode binaryNode(Token operator, ASTNode left, ASTNode right) {
        ASTNode node = new ASTNode("Binary Operator: " + operator.getValue());
        node.addChild(left);
        node.addChild(right);
        return node;
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

    private boolean matchOperator(String operator) {
        if (checkType("OPERATOR") && checkValue(operator)) {
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
        if (isAtEnd()) {
            return false;
        }

        return peek().getType().equals(type);
    }

    private boolean checkValue(String value) {
        if (isAtEnd()) {
            return false;
        }

        return peek().getValue().equals(value);
    }

    private boolean isExpressionEnd() {
        if (isAtEnd()) {
            return true;
        }

        return expressionEndValues.contains(peek().getValue());
    }

    private boolean isRecoveryBoundary() {
        if (isAtEnd()) {
            return true;
        }

        return checkValue(";") || checkValue("{") || checkValue("}");
    }

    private void synchronizeTo(String value) {
        while (!isAtEnd() && !checkValue(value)) {
            advance();
        }
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

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
            errors.add("SYNTAX ERROR -> Line "
                    + peek().getLine()
                    + " : "
                    + message);
        } else {
            errors.add("SYNTAX ERROR -> End of file : " + message);
        }
    }
}
