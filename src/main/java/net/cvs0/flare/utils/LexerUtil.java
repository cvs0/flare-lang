package net.cvs0.flare.utils;

/**
 * Utility methods for the Flare language lexer.
 */
public class LexerUtil {
    private LexerUtil() {}

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    public static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

