package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.sintaxis.grammar.AbstractSymbol;

public class TerminalSymbol extends AbstractSymbol {

    public final static String EPSILON = "ε";

    public TerminalSymbol(String name) {
        super(name, true);
    }
}