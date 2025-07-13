package net.cvs0.flare;

import net.cvs0.flare.context.LexerContext;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexer converts source code into a list of tokens.
 * Designed for easy extension and clean separation of concerns.
 */
public class Lexer {
    private final LexerContext ctx;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("string", TokenType.STRING_TYPE);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("func", TokenType.FUNC);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("region", TokenType.REGION);
        keywords.put("tag", TokenType.TAG);
        keywords.put("variant", TokenType.VARIANT);
        keywords.put("switch", TokenType.SWITCH);
        keywords.put("typeof", TokenType.TYPEOF);
        keywords.put("case", TokenType.CASE);
        keywords.put("fiber", TokenType.FIBER);
        keywords.put("for", TokenType.FOR);
        keywords.put("var", TokenType.VAR);
        keywords.put("as", TokenType.AS);
        keywords.put("while", TokenType.WHILE);
        keywords.put("null", TokenType.NULL);
    }

    public Lexer(String source) {
        this.ctx = new LexerContext(source);
    }

    /**
     * Tokenizes the entire source code and returns the list of tokens.
     */
    public List<Token> tokenize() {
        while (!ctx.isAtEnd()) {
            ctx.start = ctx.current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, ctx.line, ctx.column));
        return tokens;
    }

    private void scanToken() {
        char c = ctx.advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '+':
                if (match('=')) {
                    addToken(TokenType.PLUS_ASSIGN);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '?':
                if (match('?')) {
                    addToken(TokenType.QUESTION_QUESTION);
                } else {
                    addToken(TokenType.QUESTION_MARK);
                }
                break;
            case '/':
                if (ctx.peek() == '/') {
                    while (ctx.peek() != '\n' && !ctx.isAtEnd()) ctx.advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND_AND);
                } else {
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR_OR);
                } else {
                }
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case ' ': case '\r': case '\t':
                break;
            case '\n':
                break;
            case '"': string(); break;
            case ':': addToken(TokenType.COLON); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                }
                break;
        }
    }

    private boolean match(char expected) {
        if (ctx.isAtEnd()) return false;
        if (ctx.source.charAt(ctx.current) != expected) return false;
        ctx.current++;
        ctx.column++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = ctx.source.substring(ctx.start, ctx.current);
        tokens.add(new Token(type, text, literal, ctx.line, ctx.column));
    }

    private void identifier() {
        while (isAlphaNumeric(ctx.peek())) ctx.advance();
        String text = ctx.source.substring(ctx.start, ctx.current);

        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
        
        if (match('?')) {
            addToken(TokenType.QUESTION_MARK);
        }
    }

    private void number() {
        while (isDigit(ctx.peek())) ctx.advance();
        if (ctx.peek() == '.' && isDigit(ctx.peekNext())) {
            ctx.advance();
            while (isDigit(ctx.peek())) ctx.advance();
        }
        String text = ctx.source.substring(ctx.start, ctx.current);
        addToken(TokenType.NUMBER, Double.parseDouble(text));
    }

    private void string() {
        while (ctx.peek() != '"' && !ctx.isAtEnd()) {
            ctx.advance();
        }
        if (ctx.isAtEnd()) {
            return;
        }
        ctx.advance();
        String value = ctx.source.substring(ctx.start + 1, ctx.current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
