package FrontEnd.SymbolTable.SymbolType;

import FrontEnd.Dictionary.TokenEnums.DataType;
import FrontEnd.SymbolTable.Scope;
import FrontEnd.SymbolTable.Symbol;

import java.util.List;

public class FunctionSymbol<T> extends Symbol<T> {
	private final List<VariableSymbol<?>> parameters;  // List of parameters that act as variables inside the function.

	public FunctionSymbol(String name, DataType returnType, List<VariableSymbol<?>> parameters, long lineDeclaration, Scope scope) {
		super(name, returnType, lineDeclaration, scope);
		this.parameters = parameters;
	}

	public List<VariableSymbol<?>> getParameters() {
		return parameters;
	}
}
