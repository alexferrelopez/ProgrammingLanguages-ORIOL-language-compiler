package FrontEnd;

public class NoTerminal extends TermiNoTerm{

    private boolean isAxioma;

    public NoTerminal(String name, boolean isAxioma) {
        super(name);
        this.isAxioma = isAxioma;
    }

    public NoTerminal(String name){
        super(name);
        this.isAxioma= false;
    }

    public boolean isAxioma() {
        return isAxioma;
    }
}
