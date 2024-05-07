package frontEnd.semantics.symbolTable.symbol;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

import java.util.List;

public class FunctionSymbol<T> extends Symbol<T> {
	private final List<VariableSymbol<?>> parameters;  // List of parameters that act as variables inside the function.

	public FunctionSymbol(String name, DataType returnType, List<VariableSymbol<?>> parameters, long lineDeclaration) {
		super(name, returnType, lineDeclaration);
		this.parameters = parameters;
	}

	public List<VariableSymbol<?>> getParameters() {
		return parameters;
	}
}
