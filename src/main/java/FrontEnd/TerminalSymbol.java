package FrontEnd;

public class TerminalSymbol extends AbstractSymbol {

    final static String EPSILON = "Ɛ";

    public TerminalSymbol(String name) {
        super(name, true);
    }
}