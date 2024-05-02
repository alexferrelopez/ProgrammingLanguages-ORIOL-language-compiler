package frontend;

public class TerminalSymbol extends AbstractSymbol {

    final static String EPSILON = "ε";

    public TerminalSymbol(String name) {
        super(name, true);
    }
}