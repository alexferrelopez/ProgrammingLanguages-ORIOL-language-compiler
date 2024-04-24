package FrontEnd;

public abstract class TermiNoTerm {
    private String name;
    private boolean isTerminal;

    public TermiNoTerm(String name, boolean isTerminal) {
        this.name = name;
        this.isTerminal = isTerminal;
    }

    public String getName() {
        return name;
    }

    public boolean isTerminal(){
        return isTerminal;
    }
}
