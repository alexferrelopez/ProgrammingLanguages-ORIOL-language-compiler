package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.sintaxis.grammar.AbstractSymbol;

public class NonTerminalSymbol extends AbstractSymbol {

    private final boolean isAxioma;

    public NonTerminalSymbol(String name, boolean isAxioma) {
        super(name, false);
        this.isAxioma = isAxioma;
    }

    public NonTerminalSymbol(String name){
        super(name, false);
        this.isAxioma= false;
    }

    public boolean isAxioma() {
        return isAxioma;
    }
}
