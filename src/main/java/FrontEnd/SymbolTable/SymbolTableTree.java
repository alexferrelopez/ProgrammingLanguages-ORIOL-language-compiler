package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Scope.ScopeNode;

public class SymbolTableTree implements SymbolTableInterface {
	private int currentScopeLevel;					// Current level of the scope
	private final ScopeNode root;						// Root of the tree

	public SymbolTableTree(ScopeNode rootData) {
		this.root = rootData;
		this.currentScopeLevel = 0;
	}

	/**
	 * Add a scope to the tree
	 */
	@Override
	public void addScopeAtLevel(ScopeNode scopeNode, int level) {
		ScopeNode parentScopeNode = findScopeAtLevel(root, level);
		if (parentScopeNode != null) {
			parentScopeNode.addChild(scopeNode);
		} else {
			// TODO -> Handle error: no Scope at the given level
		}
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
	 * Remove the last scope from the list
	 */
	@Override
	public void removeScope(int level, ScopeNode scopeNode) {
		ScopeNode parentScopeNode = findScopeAtLevel(root, level);
		if (parentScopeNode != null) {
			parentScopeNode.removeChild(scopeNode);
		} else {
			// TODO -> Handle error: no Scope at the given level
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
