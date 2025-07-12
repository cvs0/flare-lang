package net.cvs0.flare.context;

/**
 * Holds the state of the lexer while scanning source code.
 * Can be extended for more advanced lexing features.
 */
public class LexerContext {
    public final String source;
    public int start = 0;
    public int current = 0;
    public int line = 1;
    public int column = 1;

    public LexerContext(String source) {
        this.source = source;
    }

    public boolean isAtEnd() {
        return current >= source.length();
    }

    public char advance() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    public char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    public char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
}

