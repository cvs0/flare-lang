package net.cvs0.flare.tokens;

/**
 * Represents a token produced by the lexer.
 * Contains type, lexeme (raw text), literal value, and position info.
 */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("%s '%s' %s (line %d, col %d)", type, lexeme, literal, line, column);
    }
}

