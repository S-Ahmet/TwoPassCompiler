# Two Pass Compiler

This project is a manually implemented two-pass compiler for a small C-like
teaching language. It was developed for the System Programming / Compiler Design
final project.

## Features

- Pass 1: lexical analysis
- Pass 2: syntax analysis and semantic analysis
- Hand-written lexer and recursive-descent parser
- Token stream display
- Symbol table display
- AST / parse tree display
- Lexical, syntax, and semantic error reporting with line numbers
- Line-by-line source-to-token mapping
- Java Swing GUI
- Source file loading from the GUI

No compiler-generator tools such as Lex, Yacc, or ANTLR are used.

## Supported Language

The compiler supports:

- `int` and `float` variable declarations
- Assignment statements
- Arithmetic expressions with operator precedence: `+`, `-`, `*`, `/`
- Comparison operators: `==`, `!=`, `<`, `>`, `<=`, `>=`
- Logical operators: `&&`, `||`, `!`
- `if / else` statements
- `while` loops
- `print(...)` statements
- Integer, floating-point, and string literals
- Single-line comments with `//`
- Block comments with `/* ... */`

## BNF Grammar

```bnf
<program>        ::= <statement>*

<statement>      ::= <declaration>
                   | <assignment>
                   | <if_statement>
                   | <while_statement>
                   | <print_statement>

<declaration>    ::= <type> IDENTIFIER ";"
<type>           ::= "int" | "float"

<assignment>     ::= IDENTIFIER "=" <expression> ";"

<if_statement>   ::= "if" "(" <expression> ")" <block> [ "else" <block> ]
<while_statement>::= "while" "(" <expression> ")" <block>
<print_statement>::= "print" "(" <expression> ")" ";"

<block>          ::= "{" <statement>* "}"

<expression>     ::= <logical_or>
<logical_or>     ::= <logical_and> ( "||" <logical_and> )*
<logical_and>    ::= <equality> ( "&&" <equality> )*
<equality>       ::= <comparison> ( ( "==" | "!=" ) <comparison> )*
<comparison>     ::= <term> ( ( "<" | ">" | "<=" | ">=" ) <term> )*
<term>           ::= <factor> ( ( "+" | "-" ) <factor> )*
<factor>         ::= <unary> ( ( "*" | "/" ) <unary> )*
<unary>          ::= ( "!" | "-" | "+" ) <unary> | <primary>
<primary>        ::= IDENTIFIER
                   | INTEGER_LITERAL
                   | FLOAT_LITERAL
                   | STRING_LITERAL
                   | "(" <expression> ")"
```

## Project Structure

```text
src/
  ast/ASTNode.java
  gui/Main.java
  gui/CompilerGUI.java
  lexer/Lexer.java
  lexer/Token.java
  parser/Parser.java
  semantic/SemanticAnalyzer.java
  symboltable/Symbol.java
  symboltable/SymbolTable.java
program.txt
samples/
PROJECT_REPORT.md
```

## How to Run

Open the project in IntelliJ IDEA and run:

```text
src/gui/Main.java
```

Or compile and run from a terminal with JDK 21:

```powershell
javac -encoding UTF-8 -d out src\ast\ASTNode.java src\lexer\Token.java src\symboltable\Symbol.java src\symboltable\SymbolTable.java src\lexer\Lexer.java src\parser\Parser.java src\semantic\SemanticAnalyzer.java src\gui\CompilerGUI.java src\gui\Main.java
java -cp out gui.Main
```

If Windows uses an old Java runtime in `PATH`, run the `java.exe` from the same
JDK installation that provides `javac`.

## Demo Test Files

The `samples` directory contains programs for the project demo:

- `valid_program.txt`
- `lexical_error.txt`
- `syntax_error.txt`
- `semantic_undeclared.txt`
- `semantic_duplicate.txt`
- `semantic_type_mismatch.txt`

Load each file with the `LOAD FILE` button and press `ANALYZE`.

## Error Types Demonstrated

- Lexical error: invalid characters, malformed numbers, unclosed strings/comments
- Syntax error: missing operands, missing delimiters, malformed statements
- Semantic error: duplicate declaration, use before declaration, incompatible types
