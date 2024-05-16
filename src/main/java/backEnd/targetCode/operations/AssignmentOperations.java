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
			case INTEGER -> integerSumSub(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2(), "add");
			//case FLOAT -> floatAssignment(destinationVariable);
			default -> null;
		};
	}

	public String subtractAssignment(String operand1, String operand2, String destination) {
		OperandContainer operandContainer = new OperandContainer();
		loadOperands(operandContainer, destination, operand1, operand2);

		// Check type of variable.
		return switch (operandContainer.getOperandsType()) {
			case INTEGER -> integerSumSub(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2(), "sub");
			//case FLOAT -> floatAssignment(destinationVariable);
			default -> null;
		};
	}

	private String loadOperandIntoRegister(Operand operand, String destination, DataType dataType) {
		String text;

		// Load a value from a register into a register.
		if (operand.isRegister()) {
			text = "lw " + destination + ", " + operand.getValue();
		}
		else {
			// Load a floating point value (decimal).
			if (dataType == DataType.FLOAT) {
				text = "li.s " + destination + ", " + operand.getValue();
			}

			// Load any other type (integer).
			else {
				text = "li " + destination + ", " + operand.getValue();
			}
		}

		return text + LINE_SEPARATOR + LINE_INDENTATION;
	}

	private String integerSumSub(Operand destination, Operand operand1, Operand operand2, String operator) {
		String text = LINE_INDENTATION;

		// Allocate temporary registers.
		String regOp1 = registerAllocator.allocateRegister(operand1.getValue());
		String regOp2 = registerAllocator.allocateRegister(operand2.getValue());
		String regDest = registerAllocator.allocateRegister(destination.getValue());

		text += loadOperandIntoRegister(operand1, regOp1, DataType.INTEGER);
		text += loadOperandIntoRegister(operand2, regOp2, DataType.INTEGER);

		// Destination will always be a temporal register.
		text += (operator + " " + regDest + ", " + regOp1 + ", " + regOp2) + LINE_SEPARATOR;

		// Free temporary registers (destination will be freed in the assignment).
		registerAllocator.freeRegister(operand2.getValue());
		registerAllocator.freeRegister(operand1.getValue());

		return text;
	}
}
