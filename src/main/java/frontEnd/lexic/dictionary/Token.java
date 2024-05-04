package frontEnd.lexic.dictionary;

import frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol;
import org.jetbrains.annotations.Nullable;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private int line;
    private int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public Token(TokenType type, int line, int column) {
        this.type = type;
        this.lexeme = getLexemeWithoutRegex(type.getPattern());
        this.line = line;
        this.column = column;
    }

    /**
     * Creates a new Token.
     * @param type the type of the token (implements TypeToken interface).
     */
    public Token(TokenType type) {
        this.type = type;
        this.lexeme = getLexemeWithoutRegex(type.getPattern());     // Avoid having "\\" in the lexeme because of Regex.
    }

    /**
     * Creates a new Token.
     * @param type the type of the token (implements TypeToken interface).
     * @param lexeme the lexeme of the token, can be null if the lexeme is not applicable.
     */
    public Token(TokenType type, @Nullable String lexeme) {
        this.type = type;
        if (lexeme == null) {
            this.lexeme = getLexemeWithoutRegex(type.getPattern()); // Avoid having "\\" in the lexeme because of Regex.
        }
        else {
            this.lexeme = lexeme;
        }
    }

    public void setPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public boolean isEOF() {
        return this.type == ReservedSymbol.EOF;
    }

    private String getLexemeWithoutRegex(String lexeme) {
        return lexeme.replace("\\", "");
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }
    @Override
    public String toString() {
        return type + " " + lexeme; // Just for debug purpose.
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}