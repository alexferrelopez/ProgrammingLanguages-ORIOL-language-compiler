package frontEnd.semantics.symbolTable;

import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public interface SymbolTableInterface {
    /**
     * Add a scope to the tree
     */
    void addScope(ScopeType scopeType);

    /**
     * Add a symbol at the current scope of the tree.
     * @param symbol    symbol (variable or function) to add in the scope.
     */
    void addSymbol(Symbol<?> symbol);

    /**
     * Find a symbol in the scope.
     * @param symbolName	the name of the symbol.
     * @return	the symbol with the given name, or null if the symbol is not in the scope.
     */
    Symbol<?> findSymbol(String symbolName);

    /**
     * Find a scope at a given level (scope level)
     */
    ScopeNode findScopeAtLevel(ScopeNode scopeNode, int level);

    /**
     * Get the current scope level
     */
    int getCurrentScopeLevel();

    /**
     *
     */
    void leaveCurrentScope();
}