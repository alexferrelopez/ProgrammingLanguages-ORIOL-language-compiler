package frontEnd.semantics.symbolTable;

import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public interface SymbolTableInterface {
    /**
     * Add a scope to the tree
     * @param scopeType the type of the scope to add (function or conditional-loop).
     */
    void addScope(ScopeType scopeType);


    /**
     * Add a function scope to the tree
     */
    void addScope(ScopeType scopeType, DataType returnType);

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
     * Find a symbol globally in all the scopes.
     * @param symbolName	the name of the symbol.
     * @return	the symbol with the given name, or null if the symbol is not in the scope.
     */
    Symbol<?> findSymbolGlobally(String symbolName);

    /**
     * Find if a symbol exists in the current scope.
     *
     * @param symbolName the name of the symbol.
     * @return true if the symbol exists; false otherwise.
     */
    boolean containsSymbol(String symbolName);

    /**
     * Find a symbol inside all the scopes of a function (for MIPS purpose).
     *
     * @param symbolName the symbol with the given name, or null if the symbol is not in the scope.
     * @return th
     */
    Symbol<?> findSymbolInsideFunction(String symbolName, String functionName);

    /**
     * Find if a symbol exists in the whole symbols table.
     *
     * @param symbolName the name of the symbol.
     * @return true if the symbol exists; false otherwise.
     */
    boolean containsSymbolGlobally(String symbolName);

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

    ScopeNode getCurrentScope();

    /**
     * Calculate all the Bytes required for a function (given its parameters and all the local variables declared).
     * @param functionName  name of the function to get its size
     * @return  number of Bytes required to allocate the whole function.
     */
    int calculateFunctionSize(String functionName);

    ScopeNode getFunctionScope(String functionName);
}