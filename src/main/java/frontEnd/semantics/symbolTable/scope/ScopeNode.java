package frontEnd.semantics.symbolTable.scope;

import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
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
	private final DataType returnType;

	public ScopeNode(int scopeLevel, ScopeType scopeType, ScopeNode parent) {
		this.symbols = new HashMap<>();
		this.scopeLevel = scopeLevel;
		this.parent = parent;
		this.scopeType = scopeType;
		this.returnType = null;
	}

	public ScopeNode(int scopeLevel, ScopeType scopeType, ScopeNode parent, DataType returnType) {
		this.symbols = new HashMap<>();
		this.scopeLevel = scopeLevel;
		this.parent = parent;
		this.scopeType = scopeType;
		this.returnType = scopeType == ScopeType.FUNCTION ? returnType : null;

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
	 * Find a symbol globally.
	 * @param symbolName	the name of the symbol.
	 * @return	the symbol with the given name, or null if the symbol is not in the scope.
	 */
	public Symbol<?> findSymbolGlobally(String symbolName) {
		// If the node is root, just search the variable in the same scope (top-level).
		if (this.scopeType == ScopeType.GLOBAL || this.parent == null) {
			return searchSymbolInScope(symbolName);
		}

		// In any other scope, we have to check on the same scope and their parent's (until reaching root).
		Symbol<?> symbol = searchSymbolInScope(symbolName);
		if (symbol != null) {
			return symbol;
		}

		// If the symbol is not found, search on the parent's scope.
		return this.parent.findSymbolGlobally(symbolName);
	}

	public ScopeNode getParent() {
		return parent;
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

	public Map<String, Symbol<?>> getSymbols() {
		return symbols;
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public DataType getReturnType() {
		// If the node is a function or is root, just search the type in the same scope.
		if (this.scopeType == ScopeType.FUNCTION || this.parent == null) {
			return returnType;
		}
		return this.parent.getReturnType();
	}

	public Symbol<?> findSymbolInsideFunction(String symbolName) {
		Symbol<?> symbol = searchSymbolInScope(symbolName);
		if (symbol != null) {
			return symbol;
		}

		// Check if it's a function.
		for (ScopeNode scope : this.children) {
			symbol = scope.findSymbolInsideFunction(symbolName);
			if (symbol != null) {
				return symbol;
			}
		}

		return null;
	}

	/**
	 * Find a function symbol by the function name. ONLY VALID FOR FUNCTIONS!
	 * @param functionName	the name of the function.
	 * @return	the function symbol with the given name, or null if the function is not declared.
	 */
	public ScopeNode findFunctionByName(String functionName) {
		Symbol<?> symbol = this.findSymbol(functionName);

		// Check if it's a function.
		if (!symbol.isVariable()) {
			FunctionSymbol<?> functionSymbol = (FunctionSymbol<?>) symbol;
			return this.children.get(functionSymbol.getRootChildIndex());
		}

		return null;
	}

	public int calculateNestedScopesSize() {

		// Calculate the maximum size (to guarantee the worst case) of all the variables from the node's children.
		List<Integer> childrenSizes = new ArrayList<>();
		for (ScopeNode child : this.children) {
			childrenSizes.add(child.calculateScopeSize());
		}

		return childrenSizes.stream().max(Integer::compareTo).orElse(0);
	}

	public int calculateScopeSize() {
		int size = 0;

		// Calculate the size of all the variables in the scope.
		for (Symbol<?> symbol : this.symbols.values()) {
			if (symbol.isVariable()) {
				size += symbol.getDataType().getSize();
			}
		}

		return size;
	}
}
