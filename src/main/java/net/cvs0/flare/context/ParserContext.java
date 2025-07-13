package net.cvs0.flare.context;

import java.util.List;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.TokenType;

/**
 * Holds parser state, such as the token list and current position.
 * Designed for clean separation and extensibility.
 */
public class ParserContext {
    public final List<Token> tokens;
    public int current = 0;

    public ParserContext(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    public Token peek() {
        return tokens.get(current);
    }

    public Token previous() {
        return tokens.get(current - 1);
    }

    public Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    public boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    public boolean check(TokenType... types) {
        if (isAtEnd()) return false;
        for (TokenType type : types) {
            if (peek().type == type) {
                return true;
            }
        }
        return false;
    }

    public boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    public Token peekNext() {
        if (current + 1 >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(current + 1);
    }
}
