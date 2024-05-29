package frontEnd.sintaxis.grammar.derivationRules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import frontEnd.sintaxis.grammar.AbstractSymbol;

public class NonTerminalSymbol extends AbstractSymbol {

    private final boolean isAxioma;

    @JsonCreator
    public NonTerminalSymbol(@JsonProperty("name") String name, @JsonProperty("isAxioma") boolean isAxioma) {
        super(name, false);
        this.isAxioma = isAxioma;
    }

    public NonTerminalSymbol(String name) {
        super(name, false);
        this.isAxioma = false;
    }

    @JsonIgnore
    public boolean isAxioma() {
        return isAxioma;
    }
}
