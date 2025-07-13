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

            // REPL built-in commands
            String cmd = line.trim().toLowerCase();
            switch (cmd) {
                case "help":
                    System.out.println("Available commands:");
                    System.out.println("  file <path>    Run a file");
                    System.out.println("  reset          Reset interpreter state");
                    System.out.println("  clear          Clear console");
                    System.out.println("  context        List global variables");
                    System.out.println("  functions      List defined functions");
                    System.out.println("  modules        List imported modules");
                    System.out.println("  dump           Dump full interpreter context");
                    System.out.println("  version        Show language version");
                    System.out.println("  exit           Exit REPL");
                    continue;
                case "reset":
                    interpreter = new Interpreter();
                    System.out.println("Interpreter reset.");
                    continue;
                case "clear":
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    continue;
                case "context":
                    System.out.println("Global context: " + interpreter.globals.getAll().keySet());
                    continue;
                case "functions":
                    interpreter.globals.getAll().forEach((k, v) -> {
                        if (v.type == Type.FUNCTION) System.out.println("- " + k);
                    });
                    continue;
                case "modules":
                    interpreter.globals.getAll().forEach((k, v) -> {
                        if (v.type == Type.MODULE) System.out.println("- " + k);
                    });
                    continue;
                case "dump":
                    interpreter.globals.getAll().forEach((k, v) ->
                            System.out.println(k + ": " + v.type + " = " + v.data));
                    continue;
                case "version":
                    System.out.println("Flare Language v1.1.0");
                    continue;
            }

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
                    if (interpreter.globals.contains("main()")) {
                        mainFunc = interpreter.globals.get("main()");
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