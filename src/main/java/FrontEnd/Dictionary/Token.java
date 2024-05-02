package frontend.Dictionary;

import frontend.AbstractSymbol;
import frontend.Dictionary.TokenEnums.ReservedSymbol;
import org.jetbrains.annotations.Nullable;

public class Token {
    private final TokenType type;
    private final String lexeme;

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
}