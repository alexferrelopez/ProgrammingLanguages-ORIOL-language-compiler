package frontEnd.sintaxis.grammar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TerminalSymbol.class, name = "TerminalSymbol"),
        @JsonSubTypes.Type(value = NonTerminalSymbol.class, name = "NonTerminalSymbol")
})
public abstract class AbstractSymbol {
    private final String name;
    private final boolean isTerminal;

    public AbstractSymbol(String name, boolean isTerminal) {
        this.name = name;
        this.isTerminal = isTerminal;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public String toString() { //For debug purposes
        return name;
    }
}