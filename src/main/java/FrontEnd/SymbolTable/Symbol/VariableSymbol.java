package FrontEnd.SymbolTable.Symbol;

import FrontEnd.Dictionary.TokenEnums.DataType;
import FrontEnd.SymbolTable.Scope.ScopeType;

public class VariableSymbol<T> extends Symbol<T> {
	private final boolean isFunctionParameter;

	public VariableSymbol(String name, DataType dataType, long lineDeclaration, ScopeType scopeType, boolean isFunctionParameter) {
		super(name, dataType, lineDeclaration, scopeType);
		this.isFunctionParameter = isFunctionParameter;
	}

	public boolean isFunctionParameter() {
		return this.isFunctionParameter;
	}
}
