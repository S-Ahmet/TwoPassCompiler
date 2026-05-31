package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String[] keywords = {
            "int", "float", "if", "else", "while", "print"
    };

    public List<Token> tokenize(String sourceCode) {

        List<Token> tokens = new ArrayList<>();

        String[] lines = sourceCode.split("\n");

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {

            String line = lines[lineNumber];

            String[] words = line.split("\\s+");

            for (String word : words) {

                if (word.isEmpty()) continue;

                boolean isKeyword = false;

                for (String keyword : keywords) {
                    if (word.equals(keyword)) {
                        tokens.add(new Token("KEYWORD", word, lineNumber + 1));
                        isKeyword = true;
                        break;
                    }
                }

                if (isKeyword) continue;

                if (word.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    tokens.add(new Token("IDENTIFIER", word, lineNumber + 1));
                }
                else if (word.matches("\\d+")) {
                    tokens.add(new Token("INTEGER_LITERAL", word, lineNumber + 1));
                }
                else if (word.matches("\\d+\\.\\d+")) {
                    tokens.add(new Token("FLOAT_LITERAL", word, lineNumber + 1));
                }
            }
        }

        return tokens;
    }
}