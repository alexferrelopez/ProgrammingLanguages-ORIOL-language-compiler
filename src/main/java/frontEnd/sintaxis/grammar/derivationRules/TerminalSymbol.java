package frontEnd.sintaxis.grammar.derivationRules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.grammar.AbstractSymbol;

public class TerminalSymbol extends AbstractSymbol {
    public final static String EPSILON = "ε";
    private Token token;

    @JsonCreator
    public TerminalSymbol(@JsonProperty("name") String name, @JsonProperty("token") Token token) {
        super(name, true);
        this.token = token;
    }

    public TerminalSymbol(String name) {
        super(name, true);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @JsonIgnore
    public boolean isEpsilon() {
        return this.getName().equals(EPSILON);
    }
}