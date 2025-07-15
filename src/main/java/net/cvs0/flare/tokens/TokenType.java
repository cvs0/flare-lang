package net.cvs0.flare.tokens;

/**
 * TokenType defines all possible types of tokens in the language.
 * Easily extensible for new keywords or operators.
 */
public enum TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
    SEMICOLON, COMMA, DOT, DOT_DOT, ASSIGN, PLUS, MINUS, STAR, SLASH, COLON,
    QUESTION_MARK, QUESTION_QUESTION,

    EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    AND_AND, OR_OR,
    NULLABLE,

    IDENTIFIER, NUMBER, STRING, NULL,

    IF, ELSE, INT, FLOAT, STRING_TYPE, BOOLEAN, LIST_TYPE, BUFFER_TYPE, BYTES_TYPE, TRUE, FALSE, FUNC, IMPORT, RETURN, REGION,
    TAG,
    VARIANT,
    SWITCH,
    TYPEOF,
    CASE,
    FIBER,
    FOR,
    VAR,
    PLUS_ASSIGN,
    AS,
    WHILE,

    EOF
}
