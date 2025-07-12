package net.cvs0.flare.examples;

import net.cvs0.flare.Interpreter;
import net.cvs0.flare.Lexer;
import net.cvs0.flare.Parser;
import net.cvs0.flare.StandardLibrary;
import net.cvs0.flare.Value;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.tokens.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LibraryExample {
    public static void main(String[] args) {
        // Create a new interpreter
        Interpreter interpreter = new Interpreter();
        
        // Register a custom library
        Map<String, Function<List<Value>, Value>> customLib = new HashMap<>();
        
        // Add a function to the custom library
        customLib.put("greet", arguments -> {
            if (arguments.size() != 1 || arguments.get(0).type != Type.STRING) {
                throw new RuntimeException("custom.greet expects 1 string argument");
            }
            String name = (String) arguments.get(0).data;
            System.out.println("Hello, " + name + "!");
            return null;
        });
        
        // Register the custom library
        StandardLibrary.registerLibrary("custom", customLib);
        
        // Example code that uses the standard libraries and custom library
        String source = """
            // Import standard libraries
            import std;
            import math;
            import string;
            import custom;
            
            // Use standard library functions
            std.print("Using standard libraries:");
            std.print("Square root of 16 is: " + math.sqrt(16));
            std.print("Length of 'Hello' is: " + string.length("Hello"));
            
            // Use custom library function
            custom.greet("World");
        """;
        
        // Parse and execute the code
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());
        List<Statement> statements = parser.parse();
        interpreter.interpret(statements);
    }
}