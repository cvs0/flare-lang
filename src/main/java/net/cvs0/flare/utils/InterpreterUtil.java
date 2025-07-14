package net.cvs0.flare.utils;

import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.Type;
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
        
        switch (typeToken.type) {
            case INT: return new Value(Type.INT, 0);
            case FLOAT: return new Value(Type.FLOAT, 0.0);
            case STRING_TYPE: return new Value(Type.STRING, "");
            case BOOLEAN: return new Value(Type.BOOL, false);
            case LIST_TYPE: return new Value(Type.LIST, new ArrayList<>());
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
                case "any":     return Type.ANY;
                default:
                    throw new RuntimeException("Unknown nullable type: " + baseType);
            }
        }
        
        // Handle regular types
        switch (typeToken.type) {
            case INT:          return Type.INT;
            case FLOAT:        return Type.FLOAT;
            case STRING_TYPE:  return Type.STRING;
            case BOOLEAN:      return Type.BOOL;
            case LIST_TYPE:    return Type.LIST;
            // allow explicit "any"
            case IDENTIFIER:
                if ("any".equals(typeToken.lexeme)) return Type.ANY;
                // fall through to error
            default:
                throw new RuntimeException("Unknown type token: " + typeToken.lexeme);
        }
    }
}

