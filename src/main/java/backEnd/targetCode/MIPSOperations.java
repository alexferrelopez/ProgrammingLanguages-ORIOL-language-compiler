package backEnd.targetCode;

import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public class MIPSOperations {
	protected static final String LINE_SEPARATOR = System.lineSeparator();
	protected static final String LINE_INDENTATION = "\t";
	protected static final String COMMENT_WORD = "#";

	protected static final String FRAME_POINTER = "$fp";
	protected static final String STACK_POINTER = "$sp";
	protected static final String RETURN_REGISTER = "$ra";
	protected static final String END_PROGRAM_INSTRUCTION = "syscall";
	protected static final String FUNCTION_RESULT_REGISTER = "$v0";

	protected final SymbolTableInterface symbolTable;
	protected final static String MAIN_FUNCTION = "ranch";
	protected static String currentFunctionName;
	protected static RegisterAllocator registerAllocator;

	public MIPSOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocator) {
		this.symbolTable = symbolTableInterface;
		MIPSOperations.registerAllocator = registerAllocator;
	}

	protected String writeComment(String comment) {
		return (COMMENT_WORD + " " + comment);
	}

	protected ValueSymbol getOperandType(String operandValue) {
		try {
			return Tokenizer.convertStringIntoValueSymbol(operandValue);
		} catch (InvalidTokenException e) {
			return ValueSymbol.VALUE_INT;	// This should never happen.
		}
	}

	protected String loadVariableToMemory(String oldestVariable, String oldestRegister) {
		return "sw " + oldestVariable + ", " + oldestRegister;
	}

	protected String loadVariableToRegister(String oldestVariable, String oldestRegister) {
		return "lw " + oldestRegister + ", " + oldestVariable;
	}

	protected Operand loadSingleOperand(String operandValue) {
		if (operandValue == null) {
			return null;
		}

		Operand operand;
		ValueSymbol operandValueSymbol = getOperandType(operandValue);

		// Check if it's a variable (check its type in the symbols table).
		if (operandValueSymbol == ValueSymbol.VARIABLE) {
			Symbol<?> variable = symbolTable.findSymbolInsideFunction(operandValue, currentFunctionName);
			String variableRegister = variable.getOffset() + "(" + FRAME_POINTER + ")";
			operand = new Operand(true, variable.getDataType(), variableRegister, false);
		}
		// Check if it's a register
		else if (operandValue.startsWith(RegisterAllocator.REGISTER_PREFIX_TEMP)) {
			operand = new Operand(true, null, operandValue, true);
		}
		// Any other type of data (integer, float, boolean...)
		else {
			DataType operandType = operandValueSymbol.getDataType();
			operand = new Operand(false, operandType, operandValue, false);
		}

		return operand;
	}

	protected void loadOperands(OperandContainer operandContainer, String destinationStr, String operand1Str, String Operand2Str) {
		operandContainer.setDestination(loadSingleOperand(destinationStr));
		operandContainer.setOperand1(loadSingleOperand(operand1Str));
		operandContainer.setOperand2(loadSingleOperand(Operand2Str));
	}
}
