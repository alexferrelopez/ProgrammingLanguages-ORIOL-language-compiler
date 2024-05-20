package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

public class AssignmentOperations extends MIPSOperations {
    public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocator) {
        super(symbolTableInterface, registerAllocator);
    }

    // Destination must always be loaded into memory when calling this function.
    public String registerToRegisterAssignment(Register destination, Operand registerValue) {
        // Get the register from the variable that is being assigned.
        Register variableRegister = registerAllocator.allocateRegister(registerValue);
        String text = saveVariableIntoMemory(variableRegister, registerValue);

        text += LINE_INDENTATION +
                ("move " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + LINE_SEPARATOR;

        if (registerValue.isTemporal()) {
            registerAllocator.freeRegister(registerValue.getValue());
        }

        return text;
    }

    public String integerLiteralAssignment(Register destination, Operand literal) {
        return  LINE_INDENTATION +
                ("li " + destination.getNotNullRegister() + ", " + literal.getValue()) + LINE_SEPARATOR;
    }


    public String assignValue(String operand1, String destination) {
        Symbol<?> variable = symbolTable.findSymbolInsideFunction(destination, currentFunctionName);
        DataType type = variable.getDataType(); // We have the datatype of the variable that is being assigned.
        StringBuilder text = new StringBuilder();

        // Do all the previous operations.
        switch (type) {
            case INTEGER -> {
                for (OperandContainer operation : this.pendingOperations) {
                    text.append(integerOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                }
            }
            case FLOAT -> {
                //floatOperation();
            }
        }


        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, null, "=");

        // Load the destination register into memory (in case it is not already).
        Register destionationRegister = registerAllocator.allocateRegister(operandContainer.getDestination());
        text.append(saveVariableIntoMemory(destionationRegister, operandContainer.getDestination()));

        // Check if it's a register to register assignment (a = b) or a direct assignment (a = 2).
        // REGISTER ASSIGNMENT
        if (operandContainer.getOperand1().isRegister()) {
            text.append(registerToRegisterAssignment(destionationRegister, operandContainer.getOperand1()));
        } else {
            // DIRECT ASSIGNMENT (check the type of the variable to know what operations do).

            return switch (operandContainer.getOperandsType()) {
                case INTEGER ->
                        text.append(integerLiteralAssignment(destionationRegister, operandContainer.getOperand1())).toString();
                //case FLOAT -> floatAssignment(destinationVariable);
                default -> null;
            };
        }

        // Ask for registers and assign the new values.

        return text.toString();
    }

    private String floatAssignment(String offset, String operand1, String operand2) {
        return LINE_INDENTATION +
                ("li.s " + offset + ", " + operand1) + LINE_SEPARATOR;
    }

    public String addPendingOperation(String operand1, String operand2, String destination, String operator) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2, operator);
        this.pendingOperations.add(operandContainer);

        return null;
    }

    private String saveVariableIntoMemory(Register register, Operand variableOffset) {
        String text = LINE_INDENTATION;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                // Only load variables or literals, not temporal registers.
                if (!variableOffset.isTemporal() || !variableOffset.isRegister()) {
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

    private String integerOperation(Operand destination, Operand operand1, Operand operand2, String operator) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocator.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1);

        Register regOp2 = registerAllocator.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2);

        Register regDest = registerAllocator.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination);

        // Make a division or a general operation (sum, subtract or multiplication).
        if (operator.equals("div")) {
            text += LINE_INDENTATION + (operator + " " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            text += LINE_INDENTATION + ("mflo " + regDest.getRegisterName()) + LINE_SEPARATOR;
        }
        else {
            text += LINE_INDENTATION + (operator + " " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
        }

        // Free temporary registers (temporal registers generated by TAC or literals).
        if (operand2.isTemporal()) {
            registerAllocator.freeRegister(operand2.getValue());
        }

        if (operand1.isTemporal()) {
            registerAllocator.freeRegister(operand1.getValue());
        }

        return text;
    }
}
