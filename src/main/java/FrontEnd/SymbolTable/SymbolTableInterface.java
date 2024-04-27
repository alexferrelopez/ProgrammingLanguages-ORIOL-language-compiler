package FrontEnd.SymbolTable;

import java.util.List;

public interface SymbolTableInterface {

	/**
	 * Check if the symbol table contains a symbol with the given name and scope.
	 * @param name	the name of the symbol.
	 * @param numScope	the scope of the symbol.
	 * @return	true if the symbol table contains the symbol, false otherwise.
	 */
	boolean contains(String name, int numScope);

	/**
	 * Get the symbol with the given name and scope.
	 * @param name	the name of the symbol.
	 * @return	the symbol with the given name and scope, or null if the symbol table does not contain the symbol.
	 */
	Symbol<?> get(String name);

	/**
	 * Get all symbols with the given name.
	 * @return	a list of symbols with the given name.
	 */
	List<Symbol<?>> getAll();

	/**
	 * Insert a symbol into the symbol table.
	 * @param symbol	the symbol to insert.
	 */
	void insert(Symbol<?> symbol);

	/**
	 * Get the number of symbols in the symbol table.
	 * @return	the number of symbols in the symbol table.
	 */
	int size();

	/**
	 * Check if the symbol table is empty.
	 * @return	true if the symbol table is empty, false otherwise.
	 */
	boolean isEmpty();
}