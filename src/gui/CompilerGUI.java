package gui;

import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;
import symboltable.Symbol;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
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
    private final Color GRAY = new Color(55, 65, 81);
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

        JLabel subtitle = new JLabel("Lexical Analysis | Syntax Analysis | Semantic Analysis");
        subtitle.setForeground(new Color(180, 180, 190));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(BG);
        header.add(title);
        header.add(subtitle);

        sourceCodeArea = createTextArea(true);
        sourceCodeArea.setText(String.join("\n",
                "int x;",
                "int y;",
                "float result;",
                "",
                "x = 10;",
                "y = 3;",
                "result = x + y * 2;",
                "",
                "if (result > 15) {",
                "    print(\"Result is large\");",
                "} else {",
                "    print(\"Result is small\");",
                "}",
                "",
                "while (x > 0) {",
                "    x = x - 1;",
                "}"
        ));

        JButton loadButton = createButton("LOAD FILE", GRAY);
        loadButton.addActionListener(e -> loadSourceFile());

        JButton analyzeButton = createButton("ANALYZE", BLUE);
        analyzeButton.addActionListener(e -> analyzeCode());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(PANEL);
        buttonPanel.add(loadButton);
        buttonPanel.add(analyzeButton);

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(PANEL);
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel sourceLabel = new JLabel("Source Code");
        sourceLabel.setForeground(Color.WHITE);
        sourceLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

        leftPanel.add(sourceLabel, BorderLayout.NORTH);
        leftPanel.add(wrap(sourceCodeArea), BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

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

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        return button;
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
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(14, 14, 14, 14));
        return area;
    }

    private JScrollPane wrap(JTextArea area) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(EDITOR);
        return pane;
    }

    private void loadSourceFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Lexer lexer = new Lexer();

        try {
            sourceCodeArea.setText(lexer.readSourceFile(chooser.getSelectedFile().toPath()));
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "File could not be loaded: " + exception.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void analyzeCode() {
        String code = sourceCodeArea.getText();

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(code);

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        SemanticAnalyzer semanticAnalyzer =
                new SemanticAnalyzer(tokens, lexer.getSymbolTable());
        semanticAnalyzer.analyze();

        showTokens(tokens);
        showSymbolTable(lexer);
        showLineMapping(code, tokens);
        astArea.setText("--- AST / PARSE TREE ---\n" + ast.toTreeString());
        errorArea.setText(buildErrorOutput(tokens, parser.getErrors(), semanticAnalyzer.getErrors()));
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

        if (lexer.getSymbolTable().getSymbols().isEmpty()) {
            builder.append("(empty)\n");
        }

        for (Symbol symbol : lexer.getSymbolTable().getSymbols()) {
            builder.append(symbol.getName()).append("\t")
                    .append(symbol.getType()).append("\t")
                    .append(symbol.getScope()).append("\t")
                    .append(symbol.getMemoryLocation()).append("\n");
        }

        symbolTableArea.setText(builder.toString());
    }

    private void showLineMapping(String code, List<Token> tokens) {
        String[] lines = code.split("\\R", -1);
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

    private String buildErrorOutput(List<Token> tokens, List<String> syntaxErrors, List<String> semanticErrors) {
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

        for (String syntaxError : syntaxErrors) {
            builder.append(syntaxError).append("\n");
        }

        for (String semanticError : semanticErrors) {
            builder.append(semanticError).append("\n");
        }

        if (builder.length() == 0) {
            builder.append("No errors.");
        }

        return builder.toString();
    }
}
