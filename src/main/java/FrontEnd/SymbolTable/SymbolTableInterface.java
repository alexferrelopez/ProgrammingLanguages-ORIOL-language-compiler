package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Scope.Scope;
import FrontEnd.SymbolTable.Symbol.Symbol;

public interface SymbolTableInterface {

    void addScopeAtLevel(Scope scope, int level);

    Scope findScopeAtLevel(Scope scope, int level);

    void removeScope(int level, Scope scope);

    /**
     * Get the current scope level
     */
    int getCurrentScopeLevel();

    /**
     * Set the current scope level
     */
    void setCurrentScopeLevel(int currentScopeLevel);

}