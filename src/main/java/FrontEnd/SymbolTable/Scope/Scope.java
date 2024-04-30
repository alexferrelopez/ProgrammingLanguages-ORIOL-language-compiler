package FrontEnd.SymbolTable.Scope;

import FrontEnd.SymbolTable.Symbol.Symbol;


import java.util.ArrayList;
import java.util.List;

public class Scope {
	private List<Scope> children = new ArrayList<>();
	private Scope parent;
	private int scopeId; 					// Scope ID, represents the level of the scope
	private List<Symbol> symbols; 		// List of symbols in the scopes

	public Scope(int scopeId) {
		this.scopeId = scopeId;
	}

	/**
	 * Add a symbol to the scope.
	 * @param symbol	the symbol to add.
	 */
	public void addSymbol(Symbol<?> symbol) {
		this.symbols.add(symbol);
	}

	/**
	 * Find a symbol in the scope.
	 * @param name	the name of the symbol.
	 * @return	the symbol with the given name, or null if the symbol is not in the scope.
	 */
	public Symbol<?> findSymbol(String name) {
		for (Symbol<?> symbol : this.symbols) {
			if (symbol.getName().equals(name)) {
				return symbol;
			}
		}
		return null;
	}

	/**
	 * Get the ID of the scope.
	 * @return	the ID of the scope.
	 */
	public int getID() {
		return this.scopeId;
	}

	public void addChild(Scope scope) {
	}

	public List<Scope> getChildren() {
		return children;
	}

	public void removeChild(Scope scope) {
		this.children.remove(scope);
	}
}
