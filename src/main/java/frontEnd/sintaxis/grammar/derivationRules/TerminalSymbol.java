package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.grammar.AbstractSymbol;

public class TerminalSymbol extends AbstractSymbol {

    public final static String EPSILON = "ε";
    private Token token;

    public TerminalSymbol(String name) {
        super(name, true);
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public boolean isEpsilon() {
        return this.getName().equals(EPSILON);
    }
}