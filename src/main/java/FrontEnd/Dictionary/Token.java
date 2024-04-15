package FrontEnd.Dictionary;

class Token {
    private final TokenType type;
    private final String lexeme;

    Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return type.getPattern() + " " + lexeme;
    }
}