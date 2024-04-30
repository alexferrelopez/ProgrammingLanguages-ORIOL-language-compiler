package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Scope.ScopeNode;

public interface SymbolTableInterface {
    /**
     * Add a scope to the tree
     */
    void addScopeAtLevel(ScopeNode scopeNode, int level);

    /**
     * Find a scope at a given level (scope level)
     */
    ScopeNode findScopeAtLevel(ScopeNode scopeNode, int level);

    /**
     * Remove the last scope from the list
     */
    void removeScope(int level, ScopeNode scopeNode);

    /**
     * Get the current scope level
     */
    int getCurrentScopeLevel();

    /**
     * Set the current scope level
     */
    void setCurrentScopeLevel(int currentScopeLevel);

}