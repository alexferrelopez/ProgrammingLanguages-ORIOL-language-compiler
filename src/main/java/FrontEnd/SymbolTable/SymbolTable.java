package FrontEnd.SymbolTable;

import FrontEnd.SymbolTable.Scope.Scope;
import FrontEnd.SymbolTable.Symbol.Symbol;

import java.util.Stack;

public class SymbolTable implements SymbolTableInterface {
	private int currentScopeLevel;			// Current level of the scope
	private final Stack<Scope> scopeStack;	// Stack of scopes

	public SymbolTable() {
		this.currentScopeLevel = 0;
		this.scopeStack = new Stack<>();
	}

	/**
	 * Open a new scope and add it to the stack.
	 */
	public void openScope() {
		Scope new_scope = new Scope(this.currentScopeLevel++);
		scopeStack.push(new_scope);
	}

	/**
	 * Close the top scope from the stack.
	 */
	public void closeScope() {
		// Remove the top scope from the stack
		if (!scopeStack.isEmpty()) {
			scopeStack.pop();
			this.currentScopeLevel--;
		} else {
			// TODO: Handle error when trying to close a scope that does not exist. This should never happen (throw exception).
		}
	}

	/**
	 * Check if the symbol table contains a symbol with the given name and scope.
	 *
	 * @param name the name of the symbol.
	 * @return true if the symbol table contains the symbol, false otherwise.
	 */
	@Override
	public boolean contains(String name) {
		return getSymbol(name) != null;
	}

	/**
	 * Get the symbol with the given name and scope.
	 *
	 * @param name the name of the symbol.
	 * @return the symbol with the given name and scope, or null if the symbol table does not contain the symbol.
	 */
	@Override
	public Symbol<?> getSymbol(String name) {
		// Iterate through the stack of scopes
		for (int i = scopeStack.size() - 1; i >= 0; i--) {
			Symbol<?> symbol = scopeStack.get(i).findSymbol(name);
			if (symbol != null) {
				return symbol;
			}
		}

		// Handle error when the symbol does not exist.
		return null;
	}

	/**
	 * Insert a symbol into the symbol table.
	 *
	 * @param symbol the symbol to insert.
	 */
	@Override
	public void insertSymbol(Symbol<?> symbol) {
		// Insert the symbol in the top scope (peek) of the stack.
		if (!scopeStack.isEmpty()) {
			scopeStack.peek().addSymbol(symbol);
		} else {
			// TODO: Handle error when trying to insert a symbol in a scope that does not exist. This should never happen (throw exception).
		}
	}
}
