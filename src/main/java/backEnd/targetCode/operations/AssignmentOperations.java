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
	public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocator) {
		super(symbolTableInterface, registerAllocator);
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
			String variableRegister = variable.getOffset() + "(" + FRAME_POINTER + ")";
			operand = new Operand(true, variable.getDataType(), variableRegister);
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
			// Check type of variable.
			return switch (operandContainer.getOperandsType()) {
				case INTEGER -> integerAssignment(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2());
				//case FLOAT -> floatAssignment(destinationVariable);
				default -> null;
			};
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
	}*/

	private String integerAssignment(Operand destination, Operand operand1, Operand operand2) {
		String text;

		// Check if it's a simple assignment.
		if (operand2 == null) {
			if (operand1.isRegister()) {
				String regOperand = registerAllocator.getRegister(operand1.getValue());
				text = 	LINE_INDENTATION +
						("sw " + destination.getValue() + ", " + regOperand) + LINE_SEPARATOR;
				registerAllocator.freeRegister(regOperand);
			}
			else {
				text = 	LINE_INDENTATION +
						("li " + destination.getValue() + ", " + operand1.getValue()) + LINE_SEPARATOR;
			}
		}
		else {
			// It's an assignment with a register (e.g. $t0 = a + 3).
		/*
		return 	LINE_INDENTATION +
				("li " + destination.getValue() + ", " + operand1.getValue()) + LINE_SEPARATOR;*/
			text = "working" + LINE_SEPARATOR;
		}
		return text;
	}

	private String floatAssignment(String offset, String operand1, String operand2) {
		return 	LINE_INDENTATION +
				("li.s " + offset + ", " + operand1) + LINE_SEPARATOR;
	}

	public String sumAssignment(String operand1, String operand2, String destination) {
		OperandContainer operandContainer = new OperandContainer();
		loadOperands(operandContainer, destination, operand1, operand2);

		// Check type of variable.
		return switch (operandContainer.getOperandsType()) {
			case INTEGER -> integerSum(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2());
			//case FLOAT -> floatAssignment(destinationVariable);
			default -> null;
		};
	}

	private String storeOperandIntoRegister(Operand operand, String destination) {
		String text;

		if (operand.isRegister()) {
			text = "lw " + destination + ", " + operand.getValue();
		}
		else {
			text = "li " + destination + ", " + operand.getValue();
		}

		return text + LINE_SEPARATOR + LINE_INDENTATION;
	}

	private String integerSum(Operand destination, Operand operand1, Operand operand2) {
		String text = LINE_INDENTATION;

		// Allocate temporary registers.
		String regOp1 = registerAllocator.allocateRegister(operand1.getValue());
		String regOp2 = registerAllocator.allocateRegister(operand2.getValue());
		String regDest = registerAllocator.allocateRegister(destination.getValue());

		text += storeOperandIntoRegister(operand1, regOp1);
		text += storeOperandIntoRegister(operand2, regOp2);

		// Destination will always be a temporal register.
		text += ("add " + regDest + ", " + regOp1 + ", " + regOp2) + LINE_SEPARATOR;

		// Free temporary registers (destination will be freed in the assignment).
		registerAllocator.freeRegister(operand1.getValue());
		registerAllocator.freeRegister(operand2.getValue());

		return text;
	}
}
