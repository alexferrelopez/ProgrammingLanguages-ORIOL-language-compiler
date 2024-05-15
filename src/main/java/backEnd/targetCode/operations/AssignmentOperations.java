package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.RegisterAllocator;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public class AssignmentOperations extends MIPSOperations {
	public AssignmentOperations(SymbolTableInterface symbolTableInterface) {
		super(symbolTableInterface);
	}

	private ValueSymbol getOperandType(String operandValue) {
		try {
			return Tokenizer.convertStringIntoValueSymbol(operandValue);
		} catch (InvalidTokenException e) {
			return ValueSymbol.VALUE_INT;	// This should never happen.
		}
	}


	private Operand loadSingleOperand(String operandValue) {
		if (operandValue == null) {
			return null;
		}

		Operand operand;
		ValueSymbol operandValueSymbol = getOperandType(operandValue);

		// Check if it's a variable (check its type in the symbols table).
		if (operandValueSymbol == ValueSymbol.VARIABLE) {
			Symbol<?> variable = symbolTable.findSymbolInsideFunction(operandValue, currentFunctionName);
			operand = new Operand(true, variable.getDataType(), operandValue);
		}
		// Check if it's a register
		else if (operandValue.startsWith(RegisterAllocator.REGISTER_PREFIX)) {
			operand = new Operand(true, null, operandValue);
		}
		// Any other type of data (integer, float, boolean...)
		else {
			DataType operandType = operandValueSymbol.getDataType();
			operand = new Operand(false, operandType, operandValue);
		}

		return operand;
	}

	private void loadOperands(OperandContainer operandContainer, String destinationStr, String operand1Str, String Operand2Str) {
		operandContainer.setDestination(loadSingleOperand(destinationStr));
		operandContainer.setOperand1(loadSingleOperand(operand1Str));
		operandContainer.setOperand2(loadSingleOperand(Operand2Str));
	}


	public String assignValue(String operand1, String operand2, String destination) {
		OperandContainer operandContainer = new OperandContainer();
		loadOperands(operandContainer, destination, operand1, operand2);

		// Check if it's a simple assignment (e.g. a = 2).
		if (operand2 == null) {
			//return directAssignment(operand1, destination);
		}

		// Ask for registers and assign the new values.

		return "";
	}


	/*private String directAssignment(String operand, String destination) {
		// Check if the destination is a variable (it should).
		Symbol<?> variable = symbolTable.findSymbolInsideFunction(destination, currentFunctionName);
		if (variable != null && variable.isVariable()) {
			long offset = variable.getOffset();
			String variableOffset = offset + "(" + FRAME_POINTER + ")";

			destinationOperand = new Operand(true, variable.getDataType(), variableOffset);
			resultOperand = new Operand(false, variable.getDataType(), operand);

			// Check type of variable.
			return switch (variable.getDataType()) {
				case INTEGER -> integerAssignment(destinationOperand, resultOperand, null );
				//case FLOAT -> floatAssignment(destinationVariable);
				default -> null;
			};
		}

		return "";
	}

	private String integerAssignment(Operand destination, Operand operand1, Operand operand2) {
		// Check if it's a simple assignment.
		if (operand2 == null) {
			return 	LINE_INDENTATION +
					("li " + destination.getValue() + ", " + operand1.getValue()) + LINE_SEPARATOR;
		}

		// It's an assignment with a register (e.g. $t0 = a + 3).
		return 	LINE_INDENTATION +
				("li " + destination.getValue() + ", " + operand1.getValue()) + LINE_SEPARATOR;
	}

	private String floatAssignment(String offset, String operand1, String operand2) {
		return 	LINE_INDENTATION +
				("li.s " + offset + ", " + operand1) + LINE_SEPARATOR;
	}

	 */

}
