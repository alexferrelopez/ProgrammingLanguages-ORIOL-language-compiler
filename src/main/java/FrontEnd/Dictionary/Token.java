package FrontEnd.Dictionary;

import FrontEnd.Dictionary.TokenEnums.ReservedSymbol;
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
        this.lexeme = type.getPattern();
    }

    /**
     * Creates a new Token.
     * @param type the type of the token (implements TypeToken interface).
     * @param lexeme the lexeme of the token, can be null if the lexeme is not applicable.
     */
    public Token(TokenType type, @Nullable String lexeme) {
        this.type = type;
        if (lexeme == null) {
            this.lexeme = type.getPattern();
        }
        else {
            this.lexeme = lexeme;
        }
    }

    public boolean isEOF() {
        return this.type == ReservedSymbol.EOF;
    }

    @Override
    public String toString() {
        return type + " " + lexeme; // Just for debug purpose.
    }
}