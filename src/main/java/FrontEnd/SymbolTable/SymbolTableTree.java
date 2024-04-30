package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Scope.Scope;

public class SymbolTableTree implements SymbolTableInterface {
	private int currentScopeLevel;					// Current level of the scope
	private final Scope root;						// Root of the tree

	public SymbolTableTree(Scope rootData) {
		this.root = rootData;
		this.currentScopeLevel = 0;
	}

	/**
	 * Add a scope to the tree
	 */
	@Override
	public void addScopeAtLevel(Scope scope, int level) {
		Scope parentScope = findScopeAtLevel(root, level);
		if (parentScope != null) {
			parentScope.addChild(scope);
		} else {
			// Handle error: no Scope at the given level
		}
	}

	@Override
	public Scope findScopeAtLevel(Scope scope, int level) {
		if (scope.getID() == level) {
			return scope;
		} else {
			for (Scope child : scope.getChildren()) {
				Scope found = findScopeAtLevel(child, level);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * Remove the last scope from the list
	 */
	@Override
	public void removeScope(int level, Scope scope) {
		Scope parentScope = findScopeAtLevel(root, level);
		if (parentScope != null) {
			parentScope.removeChild(scope);
		} else {
			// Handle error: no Scope at the given level
		}
	}

	/**
	 * Get the current scope level
	 */
	@Override
	public int getCurrentScopeLevel() {
		return currentScopeLevel;
	}

	/**
	 * Set the current scope level
	 */
	@Override
	public void setCurrentScopeLevel(int currentScopeLevel) {
		this.currentScopeLevel = currentScopeLevel;
	}

}
