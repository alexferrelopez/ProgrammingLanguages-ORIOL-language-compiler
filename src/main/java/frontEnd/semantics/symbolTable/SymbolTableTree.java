package frontEnd.semantics.symbolTable;

import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public class SymbolTableTree implements SymbolTableInterface {
	private int currentScopeLevel;	// Current level of the scope
	private final ScopeNode root;	// Root of the tree
	private ScopeNode currentScope;	// Current scope
	private final static ScopeType ROOT_SCOPE = ScopeType.GLOBAL;
	private final static int ROOT_LEVEL = 0;

	public SymbolTableTree() {
		this.root = new ScopeNode(ROOT_LEVEL, ROOT_SCOPE);
		this.currentScopeLevel = ROOT_LEVEL;
		this.currentScope = root;
	}

	/**
	 * Add a scope to the tree
	 */
	@Override
	public void addScope(ScopeNode scopeNode) {
		currentScope.addChild(scopeNode);
		currentScope = scopeNode;
		currentScopeLevel++;
	}

	/**
	 * Add a symbol at the current scope of the tree.
	 *
	 * @param symbol symbol (variable or function) to add in the scope.
	 */
	@Override
	public void addSymbol(Symbol<?> symbol) {
		this.currentScope.addSymbol(symbol);
	}

	/**
	 * Find a symbol in the scope.
	 *
	 * @param symbolName the name of the symbol.
	 * @return the symbol with the given name, or null if the symbol is not in the scope.
	 */
	@Override
	public Symbol<?> findSymbol(String symbolName) {
		return this.currentScope.findSymbol(symbolName);
	}

	/**
	 * Find a scope at a given level (scope level)
	 */
	@Override
	public ScopeNode findScopeAtLevel(ScopeNode scopeNode, int level) {
		if (scopeNode.getLevel() == level) {
			return scopeNode;
		} else {
			for (ScopeNode child : scopeNode.getChildren()) {
				ScopeNode found = findScopeAtLevel(child, level);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * Get the current scope level
	 */
	@Override
	public int getCurrentScopeLevel() {
		return currentScopeLevel;
	}

}
