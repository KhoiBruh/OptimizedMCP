package net.optifine.expr;

public enum TokenType {
    IDENTIFIER("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_:."),
    NUMBER("0123456789", "0123456789."),
    OPERATOR("+-*/%!&|<>=", "&|="),
    COMMA(","),
    BRACKET_OPEN("("),
    BRACKET_CLOSE(")");

    public static final TokenType[] VALUES = values();
    private final String charsFirst;
    private final String charsNext;

    TokenType(String charsFirst) {
        this(charsFirst, "");
    }

    TokenType(String charsFirst, String charsNext) {
        this.charsFirst = charsFirst;
        this.charsNext = charsNext;
    }

    public static TokenType getTypeByFirstChar(char ch) {
        for (TokenType tokentype : VALUES) {
            if (tokentype.getCharsFirst().indexOf(ch) >= 0) {
                return tokentype;
            }
        }

        return null;
    }

    public String getCharsFirst() {
        return charsFirst;
    }

    public boolean hasCharNext(char ch) {
        return charsNext.indexOf(ch) >= 0;
    }

    private static class Const {
    }
}
