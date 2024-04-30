package FrontEnd.SymbolTable.Scope;

import FrontEnd.SymbolTable.Symbol.Symbol;


import java.util.ArrayList;
import java.util.List;

public class ScopeNode {
	private List<ScopeNode> children = new ArrayList<>();	// List of child scopes
	private ScopeNode parent;								// Parent scope
	private int scopeLevel; 								// Level of the scope
	private List<Symbol> symbols; 							// List of symbols in the scopes

	public ScopeNode(int scopeLevel) {
		this.scopeLevel = scopeLevel;
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
	 * Get the level of the scope.
	 * @return	the level of the scope.
	 */
	public int getLevel() {
		return this.scopeLevel;
	}

	/**
	 * Add a child scope to the scope.
	 * @param scopeNode	the child scope to add.
	 */
	public void addChild(ScopeNode scopeNode) {
		this.children.add(scopeNode);
	}

	/**
	 * Get the children of the scope.
	 * @return the children of the scope.
	 */
	public List<ScopeNode> getChildren() {
		return children;
	}

	/**
	 * Remove a child scope from the scope.
	 * @param scopeNode	the child scope to remove.
	 */
	public void removeChild(ScopeNode scopeNode) {
		this.children.remove(scopeNode);
	}
}
