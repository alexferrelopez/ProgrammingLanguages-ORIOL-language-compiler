package frontEnd.semantics.symbolTable;

import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.scope.ScopeType;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public class SymbolTableTree implements SymbolTableInterface {
    private final static ScopeType ROOT_SCOPE = ScopeType.GLOBAL;
    private final static int ROOT_LEVEL = 0;
    private final ScopeNode root;    // Root of the tree
    private int currentScopeLevel;    // Current level of the scope
    private ScopeNode currentScope;    // Current scope

    public SymbolTableTree() {
        this.root = new ScopeNode(ROOT_LEVEL, ROOT_SCOPE, null);
        this.currentScopeLevel = ROOT_LEVEL;
        this.currentScope = root;
    }

    /**
     * Add a scope to the tree
     */
    @Override
    public void addScope(ScopeType scopeType) {
        currentScopeLevel++;
        ScopeNode scopeNode = new ScopeNode(currentScopeLevel, scopeType, currentScope);
        currentScope.addChild(scopeNode);
        currentScope = scopeNode;
    }

    /**
     * Add a function scope to the tree with its return type.
     */
    @Override
    public void addScope(ScopeType scopeType, DataType returnType) {
        currentScopeLevel++;
        ScopeNode scopeNode = new ScopeNode(currentScopeLevel, scopeType, currentScope, returnType);
        currentScope.addChild(scopeNode);
        currentScope = scopeNode;
    }

    /**
     * Add a symbol at the current scope of the tree.
     *
     * @param symbol symbol (variable or function) to add in the scope.
     */
    @Override
    public void addSymbol(Symbol<?> symbol) {
        // Set the index of the root's children for functions.
        if (!symbol.isVariable()) {
            FunctionSymbol<?> functionSymbol = (FunctionSymbol<?>) symbol;
            functionSymbol.setRootChildIndex(this.currentScope.getChildren().size());    // N's function = N's + 1 (current size) root children (index.)
            this.currentScope.addSymbol(functionSymbol);
        } else {
            // Just add a normal variable
            this.currentScope.addSymbol(symbol);
        }
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
     * Find a symbol globally in all the scopes.
     *
     * @param symbolName the name of the symbol.
     * @return the symbol with the given name, or null if the symbol is not in the scope.
     */
    @Override
    public Symbol<?> findSymbolGlobally(String symbolName) {
        return this.currentScope.findSymbolGlobally(symbolName);
    }

    /**
     * Find if a symbol exists in the whole symbols table.
     *
     * @param symbolName the name of the symbol.
     * @return true if the symbol exists; false otherwise.
     */
    @Override
    public boolean containsSymbol(String symbolName) {
        return (this.currentScope.findSymbol(symbolName) != null);
    }

    /**
     * Find a symbol inside all the scopes of a function (for MIPS purpose).
     *
     * @param symbolName the symbol with the given name, or null if the symbol is not in the scope.
     * @return th
     */
    @Override
    public Symbol<?> findSymbolInsideFunction(String symbolName, String functionName) {
        ScopeNode functionNode = this.root.findFunctionByName(functionName);
        return functionNode.findSymbolInsideFunction(symbolName);
    }

    public ScopeNode getFunctionScope(String functionName) {
        return this.root.findFunctionByName(functionName);
    }

    /**
     * Find if a symbol exists in the whole symbols table.
     *
     * @param symbolName the name of the symbol.
     * @return true if the symbol exists; false otherwise.
     */
    public boolean containsSymbolGlobally(String symbolName) {
        return (this.currentScope.findSymbolGlobally(symbolName) != null);
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

    @Override
    public void leaveCurrentScope() {
        currentScopeLevel--;
        this.currentScope = this.currentScope.getParent();
    }

    public ScopeNode getCurrentScope() {
        return currentScope;
    }

    /**
     * Calculate all the Bytes required for a function (given its parameters and all the local variables declared).
     *
     * @param functionName name of the function to get its size
     * @return number of Bytes required to allocate the whole function.
     */
    @Override
    public int calculateFunctionSize(String functionName) {
        // Search children with name "functionName"
        ScopeNode functionNode = this.root.findFunctionByName(functionName);    // It will always exist.

        // The main function may not exist, so return 0 if it's not found, but still generate an entry point for MIPS.
        if (functionNode == null) {
            return 0;
        }

        int currentNodeSize = functionNode.calculateScopeSize();            // Get the current node size (parameters size) + all the nested scopes variables.
        return currentNodeSize + functionNode.calculateNestedScopesSize();
    }

}
