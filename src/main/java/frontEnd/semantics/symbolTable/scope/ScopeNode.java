package frontEnd.semantics.symbolTable.scope;

import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeNode {
	private final List<ScopeNode> children = new ArrayList<>();	// List of child scopes
	private final ScopeNode parent;								// Parent scope
	private final int scopeLevel; 								// Level of the scope
	private final Map<String, Symbol<?>> symbols; 				// List of symbols in the scopes (key = symbol name, value = symbol).
	private final ScopeType scopeType;

	public ScopeNode(int scopeLevel, ScopeType scopeType, ScopeNode parent) {
		this.symbols = new HashMap<>();
		this.scopeLevel = scopeLevel;
		this.parent = parent;
		this.scopeType = scopeType;
	}

	/**
	 * Add a symbol to the scope.
	 * @param symbol	the symbol to add.
	 */
	public void addSymbol(Symbol<?> symbol) {
		this.symbols.put(symbol.getName(), symbol);
	}

	private Symbol<?> searchSymbolInScope(String symbolName) {
		if (this.symbols.containsKey(symbolName)) {
			return this.symbols.get(symbolName);
		}

		return null;
	}

	/**
	 * Find a symbol in the scope.
	 * @param symbolName	the name of the symbol.
	 * @return	the symbol with the given name, or null if the symbol is not in the scope.
	 */
	public Symbol<?> findSymbol(String symbolName) {
		// If the node is a function or is root, just search the variable in the same scope.
		if (this.scopeType == ScopeType.FUNCTION || this.parent == null) {
			return searchSymbolInScope(symbolName);
		}

		// In any other scope, we have to check on the same scope and their parent's (until reaching root or a function).
		Symbol<?> symbol = searchSymbolInScope(symbolName);
		if (symbol != null) {
			return symbol;
		}

		// If the symbol is not found, search on the parent's scope.
		return this.parent.findSymbol(symbolName);
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


	public ScopeNode getParent() {
		return parent;
	}
}
