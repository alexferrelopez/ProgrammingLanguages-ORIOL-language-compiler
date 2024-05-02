package FrontEnd;

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

    public boolean isTerminal(){
        return isTerminal;
    }

    @Override
    public String toString() { //For debug purposes
        return name;
    }
}