package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;

public class AssignmentOperations extends MIPSOperations {
    public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocator) {
        super(symbolTableInterface, registerAllocator);
    }

    // Destination must always be loaded into memory when calling this function.
    public String registerToRegisterAssignment(Register destination, Operand registerValue) {
        // Get the register from the variable that is being assigned.
        Register variableRegister = registerAllocator.allocateRegister(registerValue);
        String text = saveVariableIntoMemory(variableRegister, registerValue);

        return  text += LINE_INDENTATION +
                ("move " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister());
    }

    public String integerLiteralAssignment(Register destination, Operand literal) {
        return  LINE_INDENTATION +
                ("li " + destination.getNotNullRegister() + ", " + literal.getValue()) + LINE_SEPARATOR;
    }


    public String assignValue(String operand1, String destination) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, null);

        // Load the destination register into memory (in case it is not already).
        Register destionationRegister = registerAllocator.allocateRegister(operandContainer.getDestination());
        String text = saveVariableIntoMemory(destionationRegister, operandContainer.getDestination());

        // Check if it's a register to register assignment (a = b) or a direct assignment (a = 2).
        // REGISTER ASSIGNMENT
        if (operandContainer.getOperand1().isRegister()) {
            text += registerToRegisterAssignment(destionationRegister, operandContainer.getOperand1());
        } else {
            // DIRECT ASSIGNMENT (check the type of the variable to know what operations do).

            return switch (operandContainer.getOperandsType()) {
                case INTEGER ->
                        text += integerLiteralAssignment(destionationRegister, operandContainer.getOperand1());
                //case FLOAT -> floatAssignment(destinationVariable);
                default -> null;
            };
        }

        // Ask for registers and assign the new values.

        return text;
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

    private String assignIntegerValue(Operand destination, Operand operand1) {
        String text;

        // Load the variable into a register.
        Register variableRegister = registerAllocator.allocateRegister(destination);
        text = saveVariableIntoMemory(variableRegister, destination);

        switch (variableRegister.getRegisterEnum()) {
            case VARIABLE_ALREADY_IN_REGISTER -> {
                if (destination.isRegister()) {
                    text += LINE_INDENTATION +
                            ("li " + variableRegister.getVariableName() + ", " + operand1.getValue()) + LINE_SEPARATOR;
                } else {
                    text += LINE_INDENTATION +
                            ("li " + variableRegister.getRegisterName() + ", " + operand1.getValue()) + LINE_SEPARATOR;
                }
            }
            default -> text += LINE_INDENTATION +
                    ("li " + variableRegister.getRegisterName() + ", " + operand1.getValue()) + LINE_SEPARATOR;
        }

        return text;
    }

    private String integerAssignment(Operand destination, Operand operand1, Operand operand2) {
        String text = "";

        // Check if it's a simple assignment.
        if (operand2 == null) {
            if (operand1.isRegister()) {
                //String regOperand = registerAllocator.getRegister(operand1.getValue());

                Register regOperand = registerAllocator.allocateRegister(destination);
                text += saveVariableIntoMemory(regOperand, operand1);

                Register regDestination = registerAllocator.allocateRegister(destination);
                text += saveVariableIntoMemory(regDestination, operand1);
                text += LINE_INDENTATION +
                        ("lw " + regDestination.getRegisterName() + ", " + destination.getValue()) + LINE_SEPARATOR;
                text += LINE_INDENTATION + ("move " + regDestination.getNotNullRegister() + ", " + regOperand.getNotNullRegister()) + LINE_SEPARATOR;

                if (operand1.isTemporal()) {
                    registerAllocator.freeRegister(operand1.getValue());
                }
            } else {
                text += assignIntegerValue(destination, operand1);
            }
        } else {
            // It's an assignment with a register (e.g. $t0 = a + 3).
		/*
		return 	LINE_INDENTATION +
				("li " + destination.getValue() + ", " + operand1.getValue()) + LINE_SEPARATOR;*/
            text = "working" + LINE_SEPARATOR;
        }
        return text;
    }

    private String floatAssignment(String offset, String operand1, String operand2) {
        return LINE_INDENTATION +
                ("li.s " + offset + ", " + operand1) + LINE_SEPARATOR;
    }

    public String sumAssignment(String operand1, String operand2, String destination) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2);

        // Check type of variable.
        return switch (operandContainer.getOperandsType()) {
            case INTEGER ->
                    integerSumSub(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2(), "add");
            //case FLOAT -> floatAssignment(destinationVariable);
            default -> null;
        };
    }

    public String subtractAssignment(String operand1, String operand2, String destination) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2);

        // Check type of variable.
        return switch (operandContainer.getOperandsType()) {
            case INTEGER ->
                    integerSumSub(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2(), "sub");
            //case FLOAT -> floatAssignment(destinationVariable);
            default -> null;
        };
    }

    public String multiplicationAssignment(String operand1, String operand2, String destination) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2);

        // Check type of variable.
        return switch (operandContainer.getOperandsType()) {
            case INTEGER ->
                    integerSumSub(operandContainer.getDestination(), operandContainer.getOperand1(), operandContainer.getOperand2(), "mul");
            //case FLOAT -> floatAssignment(destinationVariable);
            default -> null;
        };
    }

    private String loadOperandIntoRegister(Operand operand, String destination, DataType dataType) {
        String text;

        // Load a value from a register into a register.
        if (operand.isRegister()) {
            text = "lw " + destination + ", " + operand.getValue();
        } else {
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

    private String saveVariableIntoMemory(Register register, Operand variableOffset) {
        String text = LINE_INDENTATION;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {
                if (!variableOffset.isTemporal()) {
                    return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName()) + LINE_SEPARATOR;
                }
                return "";
            }
            case SWAP_REGISTERS -> {
                text += loadVariableToMemory(register.getVariableName(), register.getRegisterName()) + LINE_SEPARATOR; // Write the operation to save the variable into memory.
                return text + LINE_INDENTATION + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName()) + LINE_SEPARATOR;
            }
            // The variable was already loaded into a register.
            default -> {
                return "";
            }
        }
    }

    private String integerSumSub(Operand destination, Operand operand1, Operand operand2, String operator) {
        String text = LINE_INDENTATION;

        // Allocate temporary registers.
        Register regOp1 = registerAllocator.allocateRegister(operand1);
        text += saveVariableIntoMemory(regOp1, operand1);

        Register regOp2 = registerAllocator.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2);

        Register regDest = registerAllocator.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination);

        //text += loadOperandIntoRegister(operand1, regOp1[0], DataType.INTEGER);
        //text += loadOperandIntoRegister(operand2, regOp2[0], DataType.INTEGER);

        // Destination will always be a temporal register.
        text += (operator + " " + regDest.getRegisterName() + ", " + regOp1.getVariableName() + ", " + regOp2.getVariableName()) + LINE_SEPARATOR;

        // Free temporary registers (destination will be freed in the assignment).
        //registerAllocator.freeRegister(operand2.getValue());
        //registerAllocator.freeRegister(operand1.getValue());

        return text;
    }
}
