package FrontEnd.SymbolTable;

import java.util.ArrayList;

public class SymbolTable {
    private int id; // The id of the symbol table.
    private ArrayList<SymbolTable> symbolTables; // The list of symbol tables.
    private ArrayList<Symbol> symbols;  // The list of symbols.

    /**
     * Constructor of the SymbolTable class.
     */
    public SymbolTable() {
        this.symbolTables = new ArrayList<SymbolTable>();
        this.symbols = new ArrayList<Symbol>();
    }

    /**
     * Add a symbol table to the list of symbol tables.
     * @param symbolTable The symbol table to add.
     */
    public void addSymbolTable(SymbolTable symbolTable) {
        this.symbolTables.add(symbolTable);
    }

    /**
     * Add a symbol to the list of symbols.
     * @param symbol The symbol to add.
     */
    public void addSymbol(Symbol symbol) {
        this.symbols.add(symbol);
    }

    /**
     * Get the list of symbol tables.
     * @return The list of symbol tables.
     */
    public ArrayList<SymbolTable> getSymbolTables() {
        return this.symbolTables;
    }

    /**
     * Get the list of symbols.
     * @return The list of symbols.
     */
    public ArrayList<Symbol> getSymbols() {
        return this.symbols;
    }

    /**
     * Get a symbol by its name.
     * @param symbolName The name of the symbol.
     * @return The symbol with the name.
     */
    public Symbol getSymbol(String symbolName) {
        for (Symbol symbol : this.symbols) {
            if (symbol.getName().equals(symbolName)) {
                return symbol;
            }
        }
        return null;
    }

    /**
     * Get a symbol table by its id.
     * @param symbolTableId The id of the symbol table.
     * @return The symbol table with the id.
     */
    public SymbolTable getSymbolTable(int symbolTableId) {
        for (SymbolTable symbolTable : this.symbolTables) {
            if (symbolTable.getId() == symbolTableId) {
                return symbolTable;
            }
        }
        return null;
    }

    /**
     * Check if the symbol table contains a symbol.
     * @param symbolName The name of the symbol.
     * @return True if the symbol table contains the symbol, false otherwise.
     */
    public boolean containsSymbol(String symbolName) {
        for (Symbol symbol : this.symbols) {
            if (symbol.getName().equals(symbolName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the symbol table contains a symbol table.
     * @param symbolTableId The id of the symbol table.
     * @return True if the symbol table contains the symbol table, false otherwise.
     */
    public boolean containsSymbolTable(int symbolTableId) {
        for (SymbolTable symbolTable : this.symbolTables) {
            if (symbolTable.getId() == symbolTableId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the id of the symbol table.
     * @param id The id of the symbol table.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the id of the symbol table.
     * @return The id of the symbol table.
     */
    public int getId() {
        return this.id;
    }
}
