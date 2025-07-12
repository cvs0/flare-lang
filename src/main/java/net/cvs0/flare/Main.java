package net.cvs0.flare;

import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.ast.expr.VariableReference;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Type;

import java.util.List;
import java.util.Scanner;

/**
 * Main class with a simple REPL for the custom language.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        System.out.println("Welcome to the Flare REPL. Type 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null || line.trim().equalsIgnoreCase("exit")) break;
            if (line.trim().startsWith("file ")) {
                String filePath = line.trim().substring(5).trim();
                try {
                    java.nio.file.Path path = java.nio.file.Paths.get(filePath);
                    String source = java.nio.file.Files.readString(path);
                    Lexer lexer = new Lexer(source);
                    List<Token> tokens = lexer.tokenize();
                    Parser parser = new Parser(tokens);
                    List<Statement> statements = parser.parse();
                    interpreter.interpret(statements);

                    Value mainFunc = null;
                    if (interpreter.globals.contains("main")) {
                        mainFunc = interpreter.globals.get("main");
                    }
                    if (mainFunc != null && mainFunc.type == Type.FUNCTION) {
                        try {
                            interpreter.visitFunctionCall(new net.cvs0.flare.ast.FunctionCall(
                                    new VariableReference(new Token(TokenType.IDENTIFIER, "main", null, 0, 0)),
                                    new Token(TokenType.LEFT_PAREN, "(", null, 0, 0),
                                    java.util.Collections.emptyList()
                            ));
                            interpreter.getFiberManager().joinAll();
                        } catch (Exception e) {
                            System.out.println("Error calling main(): " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading file: " + e.getMessage());
                }
                continue;
            }
            try {
                Lexer lexer = new Lexer(line);
                List<Token> tokens = lexer.tokenize();
                Parser parser = new Parser(tokens);
                List<Statement> statements = parser.parse();
                interpreter.interpret(statements);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }
}
