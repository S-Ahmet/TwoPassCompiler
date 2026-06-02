package semantic;

import lexer.Token;
import symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticAnalyzer {

    private static final String TYPE_INT = "int";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_UNKNOWN = "unknown";
    private static final String TYPE_INVALID = "invalid";

    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final List<String> errors = new ArrayList<>();

    public SemanticAnalyzer(List<Token> tokens, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
    }

    public void analyze() {
        errors.clear();
        Map<String, String> declared = new HashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);

            if (isTypeKeyword(current)) {
                i = analyzeDeclaration(i, declared);
                continue;
            }

            if (current.getType().equals("IDENTIFIER")) {
                if (isAssignmentAt(i)) {
                    i = analyzeAssignment(i, declared);
                } else if (!declared.containsKey(current.getValue())) {
                    addError(current, "'" + current.getValue() + "' used before declaration.");
                }

                continue;
            }

            if (current.getValue().equals("if") || current.getValue().equals("while")) {
                i = analyzeCondition(i, declared, current.getValue());
                continue;
            }

            if (current.getValue().equals("print")) {
                i = analyzePrint(i, declared);
            }
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    private int analyzeDeclaration(int index, Map<String, String> declared) {
        Token type = tokens.get(index);

        if (index + 1 >= tokens.size() || !tokens.get(index + 1).getType().equals("IDENTIFIER")) {
            return index;
        }

        Token identifier = tokens.get(index + 1);

        if (declared.containsKey(identifier.getValue())) {
            addError(identifier, "'" + identifier.getValue() + "' is already declared.");
        } else {
            String symbolType = symbolTable.getType(identifier.getValue(), "global");
            declared.put(identifier.getValue(), symbolType != null ? symbolType : type.getValue());
        }

        return index + 1;
    }

    private int analyzeAssignment(int index, Map<String, String> declared) {
        Token variable = tokens.get(index);
        String variableType = declared.get(variable.getValue());

        if (variableType == null) {
            addError(variable, "'" + variable.getValue() + "' assigned before declaration.");
        }

        int expressionStart = index + 2;
        int expressionEnd = findExpressionEnd(expressionStart, ";");
        String valueType = inferExpressionType(expressionStart, expressionEnd, declared);

        if (variableType != null && !isAssignable(variableType, valueType)) {
            addError(variable, "Cannot assign " + valueType + " expression to " + variableType + " variable.");
        }

        return expressionEnd;
    }

    private int analyzeCondition(int index, Map<String, String> declared, String statementName) {
        if (index + 1 >= tokens.size() || !tokens.get(index + 1).getValue().equals("(")) {
            return index;
        }

        int expressionStart = index + 2;
        int expressionEnd = findExpressionEnd(expressionStart, ")");
        String conditionType = inferExpressionType(expressionStart, expressionEnd, declared);

        if (!conditionType.equals(TYPE_BOOLEAN) && !conditionType.equals(TYPE_UNKNOWN) && !conditionType.equals(TYPE_INVALID)) {
            addError(tokens.get(index), statementName + " condition must be a comparison or logical expression.");
        }

        return expressionEnd;
    }

    private int analyzePrint(int index, Map<String, String> declared) {
        if (index + 1 >= tokens.size() || !tokens.get(index + 1).getValue().equals("(")) {
            return index;
        }

        int expressionStart = index + 2;
        int expressionEnd = findExpressionEnd(expressionStart, ")");
        inferExpressionType(expressionStart, expressionEnd, declared);
        return expressionEnd;
    }

    private String inferExpressionType(int start, int end, Map<String, String> declared) {
        if (start >= end) {
            return TYPE_INVALID;
        }

        ExpressionTypeParser parser = new ExpressionTypeParser(start, end, declared);
        return parser.parse();
    }

    private int findExpressionEnd(int start, String endValue) {
        int depth = 0;

        for (int i = start; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getValue().equals("(")) {
                depth++;
            } else if (token.getValue().equals(")")) {
                if (depth == 0 && endValue.equals(")")) {
                    return i;
                }

                depth--;
            }

            if (depth == 0 && token.getValue().equals(endValue)) {
                return i;
            }
        }

        return tokens.size();
    }

    private boolean isAssignmentAt(int index) {
        return index + 1 < tokens.size() && tokens.get(index + 1).getValue().equals("=");
    }

    private boolean isAssignable(String variableType, String valueType) {
        if (valueType.equals(TYPE_UNKNOWN) || valueType.equals(TYPE_INVALID)) {
            return true;
        }

        if (variableType.equals(TYPE_INT)) {
            return valueType.equals(TYPE_INT);
        }

        if (variableType.equals(TYPE_FLOAT)) {
            return valueType.equals(TYPE_INT) || valueType.equals(TYPE_FLOAT);
        }

        return false;
    }

    private boolean isTypeKeyword(Token token) {
        return token.getType().equals("KEYWORD") &&
                (token.getValue().equals(TYPE_INT) || token.getValue().equals(TYPE_FLOAT));
    }

    private boolean isNumeric(String type) {
        return type.equals(TYPE_INT) || type.equals(TYPE_FLOAT);
    }

    private void addError(Token token, String message) {
        errors.add("SEMANTIC ERROR -> Line " + token.getLine() + " : " + message);
    }

    private class ExpressionTypeParser {
        private final int end;
        private final Map<String, String> declared;
        private int current;

        ExpressionTypeParser(int start, int end, Map<String, String> declared) {
            this.current = start;
            this.end = end;
            this.declared = declared;
        }

        String parse() {
            String type = parseLogicalOr();

            while (!isAtEnd()) {
                current++;
            }

            return type;
        }

        private String parseLogicalOr() {
            String left = parseLogicalAnd();

            while (match("||")) {
                Token operator = previous();
                String right = parseLogicalAnd();
                left = logicalResult(operator, left, right);
            }

            return left;
        }

        private String parseLogicalAnd() {
            String left = parseEquality();

            while (match("&&")) {
                Token operator = previous();
                String right = parseEquality();
                left = logicalResult(operator, left, right);
            }

            return left;
        }

        private String parseEquality() {
            String left = parseComparison();

            while (match("==") || match("!=")) {
                Token operator = previous();
                String right = parseComparison();
                left = comparisonResult(operator, left, right);
            }

            return left;
        }

        private String parseComparison() {
            String left = parseTerm();

            while (match("<") || match(">") || match("<=") || match(">=")) {
                Token operator = previous();
                String right = parseTerm();
                left = comparisonResult(operator, left, right);
            }

            return left;
        }

        private String parseTerm() {
            String left = parseFactor();

            while (match("+") || match("-")) {
                Token operator = previous();
                String right = parseFactor();
                left = arithmeticResult(operator, left, right);
            }

            return left;
        }

        private String parseFactor() {
            String left = parseUnary();

            while (match("*") || match("/")) {
                Token operator = previous();
                String right = parseUnary();
                left = arithmeticResult(operator, left, right);
            }

            return left;
        }

        private String parseUnary() {
            if (match("!")) {
                Token operator = previous();
                String operand = parseUnary();

                if (!operand.equals(TYPE_BOOLEAN) && !operand.equals(TYPE_UNKNOWN) && !operand.equals(TYPE_INVALID)) {
                    addError(operator, "Logical NOT requires a boolean expression.");
                    return TYPE_INVALID;
                }

                return TYPE_BOOLEAN;
            }

            if (match("-") || match("+")) {
                Token operator = previous();
                String operand = parseUnary();

                if (!isNumeric(operand) && !operand.equals(TYPE_UNKNOWN) && !operand.equals(TYPE_INVALID)) {
                    addError(operator, "Unary numeric operator requires int or float expression.");
                    return TYPE_INVALID;
                }

                return operand;
            }

            return parsePrimary();
        }

        private String parsePrimary() {
            if (isAtEnd()) {
                return TYPE_INVALID;
            }

            Token token = advance();

            if (token.getType().equals("INTEGER_LITERAL")) {
                return TYPE_INT;
            }

            if (token.getType().equals("FLOAT_LITERAL")) {
                return TYPE_FLOAT;
            }

            if (token.getType().equals("STRING_LITERAL")) {
                return TYPE_STRING;
            }

            if (token.getType().equals("IDENTIFIER")) {
                String type = declared.get(token.getValue());

                if (type == null) {
                    addError(token, "'" + token.getValue() + "' used before declaration.");
                    return TYPE_UNKNOWN;
                }

                return type;
            }

            if (token.getValue().equals("(")) {
                String nestedType = parseLogicalOr();

                if (!isAtEnd() && peek().getValue().equals(")")) {
                    advance();
                }

                return nestedType;
            }

            return TYPE_INVALID;
        }

        private String arithmeticResult(Token operator, String left, String right) {
            if (!isNumeric(left) || !isNumeric(right)) {
                if (!left.equals(TYPE_UNKNOWN) && !right.equals(TYPE_UNKNOWN) &&
                        !left.equals(TYPE_INVALID) && !right.equals(TYPE_INVALID)) {
                    addError(operator, "Arithmetic operator requires numeric operands.");
                }

                return TYPE_INVALID;
            }

            if (left.equals(TYPE_FLOAT) || right.equals(TYPE_FLOAT) || operator.getValue().equals("/")) {
                return TYPE_FLOAT;
            }

            return TYPE_INT;
        }

        private String comparisonResult(Token operator, String left, String right) {
            if (left.equals(TYPE_INVALID) || right.equals(TYPE_INVALID)) {
                return TYPE_INVALID;
            }

            if (left.equals(TYPE_UNKNOWN) || right.equals(TYPE_UNKNOWN)) {
                return TYPE_UNKNOWN;
            }

            if ((isNumeric(left) && isNumeric(right)) || left.equals(right)) {
                return TYPE_BOOLEAN;
            }

            addError(operator, "Comparison operands are not compatible.");
            return TYPE_INVALID;
        }

        private String logicalResult(Token operator, String left, String right) {
            if (left.equals(TYPE_BOOLEAN) && right.equals(TYPE_BOOLEAN)) {
                return TYPE_BOOLEAN;
            }

            if (!left.equals(TYPE_UNKNOWN) && !right.equals(TYPE_UNKNOWN) &&
                    !left.equals(TYPE_INVALID) && !right.equals(TYPE_INVALID)) {
                addError(operator, "Logical operator requires boolean operands.");
            }

            return TYPE_INVALID;
        }

        private boolean match(String value) {
            if (!isAtEnd() && peek().getValue().equals(value)) {
                advance();
                return true;
            }

            return false;
        }

        private Token advance() {
            return tokens.get(current++);
        }

        private Token previous() {
            return tokens.get(current - 1);
        }

        private Token peek() {
            return tokens.get(current);
        }

        private boolean isAtEnd() {
            return current >= end;
        }
    }
}
