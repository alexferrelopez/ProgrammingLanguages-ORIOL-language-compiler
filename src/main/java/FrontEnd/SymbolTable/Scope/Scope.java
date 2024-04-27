package FrontEnd.SymbolTable.Scope;

import FrontEnd.SymbolTable.Symbol.Symbol;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope nextScope;
	private int scopeId; // Scope ID, represents the level of the scope

	private final Map<String, Symbol<?>> scopeSymbols = new HashMap<>();

	public Scope(int scopeId) {
		this.scopeId = scopeId;
	}

	/**
	 * Check if the symbol name is unique in the scope.
	 * @param name	the name of the symbol.
	 */
	public void checkUniqueSymbolName(String name) {
		for (String local_name : this.scopeSymbols.keySet()) {
			if (local_name.equals(name)) {
				// TODO -> Error: Symbol already defined
			}
		}
	}

	/**
	 * Add a symbol to the scope.
	 * @param symbol	the symbol to add.
	 */
	public void addSymbol(Symbol<?> symbol) {
		this.checkUniqueSymbolName(symbol.getName());
		this.scopeSymbols.put(symbol.getName(), symbol);
	}

	/**
	 * Find a symbol in the scope.
	 * @param name	the name of the symbol.
	 * @return	the symbol with the given name, or null if the symbol is not in the scope.
	 */
	public Symbol<?> findSymbol(String name) {
		for (Map.Entry<String, Symbol<?>> symbol_entry : this.scopeSymbols.entrySet()) {
			String symbolName = symbol_entry.getKey();
			Symbol<?> symbolValue = symbol_entry.getValue();

			if (name.equals(symbolName)) {
				return symbolValue;
			}
		}
		return null;
	}

}
