package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Symbol.Symbol;

public interface SymbolTableInterface {

	/**
	 * Check if the symbol table contains a symbol with the given name and scope.
	 * @param name	the name of the symbol.
	 * @return	true if the symbol table contains the symbol, false otherwise.
	 */
	boolean contains(String name);

	/**
	 * Get the symbol with the given name and scope.
	 * @param name	the name of the symbol.
	 * @return	the symbol with the given name and scope, or null if the symbol table does not contain the symbol.
	 */
	Symbol<?> getSymbol(String name);

	/**
	 * Insert a symbol into the symbol table.
	 * @param symbol	the symbol to insert.
	 */
	void insertSymbol(Symbol<?> symbol);
}