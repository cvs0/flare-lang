package net.cvs0.flare.utils;

import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.Type;
import net.cvs0.flare.Value;
import net.cvs0.flare.context.ExecutionContext;

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
        switch (typeToken.type) {
            case INT: return new Value(Type.INT, 0);
            case FLOAT: return new Value(Type.FLOAT, 0.0);
            case STRING_TYPE: return new Value(Type.STRING, "");
            case BOOLEAN: return new Value(Type.BOOL, false);
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
}

