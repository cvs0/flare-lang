package net.cvs0.flare.utils;

import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.Type;
import net.cvs0.flare.tokens.TypedType;
import net.cvs0.flare.Value;
import net.cvs0.flare.context.ExecutionContext;
import java.util.ArrayList;

public class InterpreterUtil {
    private InterpreterUtil() {}

    public static boolean isAnyType(Token typeToken) {
        return typeToken.type == TokenType.IDENTIFIER && "any".equals(typeToken.lexeme);
    }

    public static void defineVariable(ExecutionContext ctx, Token name, Token typeToken, Value initializer) {
        Value value = initializer;
        if (isAnyType(typeToken)) {
            value = initializer != null ? initializer : new Value(Type.ANY, null);
        } else if (initializer == null) {
            value = InterpreterUtil.defaultValue(typeToken);
        }
        ctx.define(name.lexeme, value);
    }

    public static Value defaultValue(Token typeToken) {
        // Handle nullable types (ending with ?)
        if (typeToken.type == TokenType.IDENTIFIER && typeToken.lexeme.endsWith("?")) {
            return new Value(tokenTypeToType(typeToken), null);
        }
        
        if (typeToken.type == TokenType.IDENTIFIER && typeToken.lexeme.contains("<")) {
            return createTypedDefaultValue(typeToken);
        }
        
        switch (typeToken.type) {
            case INT: return new Value(Type.INT, 0);
            case FLOAT: return new Value(Type.FLOAT, 0.0);
            case STRING_TYPE: return new Value(Type.STRING, "");
            case BOOLEAN: return new Value(Type.BOOL, false);
            case LIST_TYPE: return new Value(Type.LIST, new ArrayList<>());
            case BUFFER_TYPE: return new Value(Type.BUFFER, new ArrayList<>());
            case BYTES_TYPE: return new Value(Type.BYTES, new byte[0]);
            case VARIANT: return new Value(Type.VARIANT, null);
            case TAG: return new Value(Type.TAG, null);
            case NULL: return new Value(Type.NULL, null);
            default: throw new RuntimeException("Unknown type: " + typeToken.lexeme);
        }
    }


    public static boolean isTruthy(Value value) {
        if (value.type == Type.BOOL) return (boolean)value.data;
        if (value.type == Type.INT) return (int)value.data != 0;
        if (value.type == Type.FLOAT) return ((double)value.data) != 0.0;
        if (value.type == Type.STRING) return !((String)value.data).isEmpty();
        return value.data != null;
    }

    public static Type tokenTypeToType(Token typeToken) {
        // Handle nullable types (ending with ?)
        if (typeToken.type == TokenType.IDENTIFIER && typeToken.lexeme.endsWith("?")) {
            String baseType = typeToken.lexeme.substring(0, typeToken.lexeme.length() - 1);
            switch (baseType) {
                case "int":     return Type.INT;
                case "float":   return Type.FLOAT;
                case "string":  return Type.STRING;
                case "boolean": return Type.BOOL;
                case "list":    return Type.LIST;
                case "buffer":  return Type.BUFFER;
                case "bytes":   return Type.BYTES;
                case "any":     return Type.ANY;
                default:
                    throw new RuntimeException("Unknown nullable type: " + baseType);
            }
        }
        
        // Handle regular types
        if (typeToken.type == TokenType.IDENTIFIER && typeToken.lexeme.contains("<")) {
            return parseTypedType(typeToken).baseType;
        }
        
        switch (typeToken.type) {
            case INT:          return Type.INT;
            case FLOAT:        return Type.FLOAT;
            case STRING_TYPE:  return Type.STRING;
            case BOOLEAN:      return Type.BOOL;
            case LIST_TYPE:    return Type.LIST;
            case BUFFER_TYPE:  return Type.BUFFER;
            case BYTES_TYPE:   return Type.BYTES;
            case IDENTIFIER:
                if ("any".equals(typeToken.lexeme)) return Type.ANY;
                throw new RuntimeException("Unknown type token: " + typeToken.lexeme);
            case FHANDLE:
                return Type.FHANDLE;
            default:
                throw new RuntimeException("Unknown type token: " + typeToken.lexeme);
        }
    }
    
    private static Value createTypedDefaultValue(Token typeToken) {
        TypedType typedType = parseTypedType(typeToken);
        switch (typedType.baseType) {
            case LIST:
                return new Value(typedType, new ArrayList<>());
            case BUFFER:
                return new Value(typedType, new ArrayList<>());
            default:
                throw new RuntimeException("Unsupported typed type: " + typedType);
        }
    }
    
    public static TypedType parseTypedType(Token typeToken) {
        String lexeme = typeToken.lexeme;
        if (lexeme.endsWith("?")) {
            lexeme = lexeme.substring(0, lexeme.length() - 1);
        }
        
        int openBracket = lexeme.indexOf('<');
        int closeBracket = lexeme.indexOf('>');
        
        if (openBracket == -1 || closeBracket == -1) {
            throw new RuntimeException("Invalid typed type format: " + lexeme);
        }
        
        String baseTypeName = lexeme.substring(0, openBracket);
        String elementTypeName = lexeme.substring(openBracket + 1, closeBracket);
        
        Type baseType = stringToType(baseTypeName);
        Type elementType = stringToType(elementTypeName);
        
        return new TypedType(baseType, elementType);
    }
    
    private static Type stringToType(String typeName) {
        switch (typeName) {
            case "int": return Type.INT;
            case "float": return Type.FLOAT;
            case "string": return Type.STRING;
            case "boolean": return Type.BOOL;
            case "list": return Type.LIST;
            case "buffer": return Type.BUFFER;
            case "bytes": return Type.BYTES;
            case "any": return Type.ANY;
            default: throw new RuntimeException("Unknown type name: " + typeName);
        }
    }

    /**
     * Prints a formatted stack trace for interpreter errors.
     */
    public static void printStackTrace(RuntimeException e, java.util.Deque<String> callStack) {
        System.err.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        System.err.println("Stack trace:");
        if (callStack == null || callStack.isEmpty()) {
            System.err.println("  (empty)");
        } else {
            for (String frame : callStack) {
                System.err.println("  at " + frame);
            }
        }
    }
}
