package gui;

import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;
import symboltable.Symbol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class CompilerGUI extends JFrame {

    private JTextArea sourceCodeArea;
    private JTextArea tokenArea;
    private JTextArea symbolTableArea;
    private JTextArea astArea;
    private JTextArea errorArea;
    private JTextArea lineMappingArea;

    private final Color BG = new Color(18, 18, 22);
    private final Color PANEL = new Color(30, 30, 36);
    private final Color EDITOR = new Color(15, 15, 18);
    private final Color BORDER = new Color(70, 70, 80);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color TEXT = new Color(240, 240, 245);

    public CompilerGUI() {
        setTitle("Two Pass Compiler");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        UIManager.put("TabbedPane.selected", BLUE);
        UIManager.put("TabbedPane.tabInsets", new Insets(12, 22, 12, 22));

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Two Pass Compiler Project");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Lexical Analysis • Syntax Analysis • Semantic Analysis");
        subtitle.setForeground(new Color(180, 180, 190));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(BG);
        header.add(title);
        header.add(subtitle);

        sourceCodeArea = createTextArea(true);
        sourceCodeArea.setText("""
                int x;
                int y;
                float result;

                x = 10;
                y = 3;
                result = x + y * 2;

                if (result > 15) {
                    print("Result is large");
                } else {
                    print("Result is small");
                }

                while (x > 0) {
                    x = x - 1;
                }
                """);

        JButton analyzeButton = new JButton("ANALİZ ET");
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        analyzeButton.setBackground(BLUE);
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setFocusPainted(false);
        analyzeButton.setBorderPainted(false);
        analyzeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        analyzeButton.setBorder(new EmptyBorder(14, 20, 14, 20));
        analyzeButton.addActionListener(e -> analyzeCode());

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(PANEL);
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel sourceLabel = new JLabel("Kaynak Kod");
        sourceLabel.setForeground(Color.WHITE);
        sourceLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

        leftPanel.add(sourceLabel, BorderLayout.NORTH);
        leftPanel.add(wrap(sourceCodeArea), BorderLayout.CENTER);
        leftPanel.add(analyzeButton, BorderLayout.SOUTH);

        tokenArea = createTextArea(false);
        symbolTableArea = createTextArea(false);
        astArea = createTextArea(false);
        errorArea = createTextArea(false);
        lineMappingArea = createTextArea(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.setBackground(PANEL);
        tabbedPane.setForeground(Color.BLACK);

        tabbedPane.addTab("Tokens", wrap(tokenArea));
        tabbedPane.addTab("Symbol Table", wrap(symbolTableArea));
        tabbedPane.addTab("AST", wrap(astArea));
        tabbedPane.addTab("Errors", wrap(errorArea));
        tabbedPane.addTab("Source Analysis", wrap(lineMappingArea));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(PANEL);
        rightPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.40);
        splitPane.setBorder(null);
        splitPane.setBackground(BG);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JTextArea createTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setBackground(EDITOR);
        area.setForeground(TEXT);
        area.setCaretColor(Color.WHITE);
        area.setSelectionColor(BLUE);
        area.setSelectedTextColor(Color.WHITE);
        area.setBorder(new EmptyBorder(14, 14, 14, 14));
        return area;
    }

    private JScrollPane wrap(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(EDITOR);
        return pane;
    }

    private void analyzeCode() {
        String code = sourceCodeArea.getText();

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(code);

        showTokens(tokens);
        showSymbolTable(lexer);
        showLineMapping(code, tokens);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        SemanticAnalyzer semanticAnalyzer =
                new SemanticAnalyzer(tokens, lexer.getSymbolTable());
        semanticAnalyzer.analyze();

        System.out.println("\n--- AST / PARSE TREE ---");
        ast.print("");

        System.setOut(originalOut);

        String output = outputStream.toString();

        astArea.setText(extractAst(output));
        errorArea.setText(extractErrors(tokens, output));
    }

    private void showTokens(List<Token> tokens) {
        StringBuilder builder = new StringBuilder();

        for (Token token : tokens) {
            builder.append(token).append("\n");
        }

        tokenArea.setText(builder.toString());
    }

    private void showSymbolTable(Lexer lexer) {
        StringBuilder builder = new StringBuilder();

        builder.append("Name\tType\tScope\tMemory\n");
        builder.append("----------------------------------------\n");

        for (Symbol symbol : lexer.getSymbolTable().getSymbols()) {
            builder.append(symbol.getName()).append("\t")
                    .append(symbol.getType()).append("\t")
                    .append(symbol.getScope()).append("\t")
                    .append(symbol.getMemoryLocation()).append("\n");
        }

        symbolTableArea.setText(builder.toString());
    }

    private void showLineMapping(String code, List<Token> tokens) {
        String[] lines = code.split("\n");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;

            builder.append("Line ")
                    .append(lineNumber)
                    .append(" -> ")
                    .append(lines[i])
                    .append("\n");

            for (Token token : tokens) {
                if (token.getLine() == lineNumber) {
                    builder.append("   ")
                            .append(token.getValue())
                            .append(" (")
                            .append(token.getType())
                            .append(")")
                            .append("\n");
                }
            }

            builder.append("\n");
        }

        lineMappingArea.setText(builder.toString());
    }

    private String extractAst(String output) {
        int index = output.indexOf("--- AST / PARSE TREE ---");

        if (index != -1) {
            return output.substring(index);
        }

        return "AST oluşturulamadı.";
    }

    private String extractErrors(List<Token> tokens, String output) {
        StringBuilder builder = new StringBuilder();

        for (Token token : tokens) {
            if (token.getType().equals("LEXICAL_ERROR")) {
                builder.append("LEXICAL ERROR -> Line ")
                        .append(token.getLine())
                        .append(" : ")
                        .append(token.getValue())
                        .append("\n");
            }
        }

        String[] lines = output.split("\n");

        for (String line : lines) {
            if (line.contains("SYNTAX ERROR") || line.contains("SEMANTIC ERROR")) {
                builder.append(line).append("\n");
            }
        }

        if (builder.length() == 0) {
            builder.append("Hata yok.");
        }

        return builder.toString();
    }
}