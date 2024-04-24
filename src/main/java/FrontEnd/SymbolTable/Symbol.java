package FrontEnd.SymbolTable;

public class Symbol {
    private String name; // The name of the symbol.
    private String type; // The type of the symbol.
    private String value; // The value of the symbol.
    private String scope; // The scope of the symbol.

    // TODO -> recordar que un Symbol és un node d'un arbre, per tant, ha de tenir una llista de fills.

    public Symbol(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

}
