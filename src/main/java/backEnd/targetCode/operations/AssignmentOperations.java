package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import javax.xml.crypto.Data;

public class AssignmentOperations extends MIPSOperations {
    public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat) {
        super(symbolTableInterface, registerAllocatorInteger, registerAllocatorFloat);
    }

    // Destination must always be loaded into memory when calling this function.
    public String registerToRegisterAssignment(Register destination, Operand registerValue, DataType destinationType) {
        // Get the register from the variable that is being assigned.
        Register variableRegister;
        RegisterAllocator registerAllocator;

        if (destinationType == DataType.INTEGER) {
            registerAllocator = registerAllocatorInteger;
        }
        else {
            registerAllocator = registerAllocatorFloat;
        }

        variableRegister = registerAllocator.allocateRegister(registerValue);
        String text = saveVariableIntoMemory(variableRegister, registerValue, destinationType);

        text += LINE_INDENTATION +
                ("move " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + LINE_SEPARATOR;

        if (registerValue.isTemporal()) {
            registerAllocator.freeRegister(registerValue.getValue());
        }

        return text;
    }

    public String literalAssignment(Register destination, Operand literal, DataType dataType) {
        if (dataType == DataType.INTEGER) {
            return  LINE_INDENTATION +
                    ("li " + destination.getNotNullRegister() + ", " + literal.getValue()) + LINE_SEPARATOR;
        }
        else {
            return  LINE_INDENTATION +
                    ("li.s " + destination.getNotNullRegister() + ", " + literal.getValue()) + LINE_SEPARATOR;
        }
    }


    public String assignValue(String operand1, String destination) {
        Symbol<?> variable = symbolTable.findSymbolInsideFunction(destination, currentFunctionName);
        DataType destinationType = variable.getDataType(); // We have the datatype of the variable that is being assigned.
        StringBuilder text = new StringBuilder();

        // Do all the previous operations.
        for (OperandContainer operation : this.pendingOperations) {
            switch (destinationType) {
                case INTEGER -> {
                    text.append(integerOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                }
                case FLOAT -> {
                    text.append(floatOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                }
            }
        }

        // Clear all the operations.
        this.pendingOperations.clear();

        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, null, "=");
        RegisterAllocator registerAllocator;

        // Load the destination register into memory (in case it is not already).
        if (destinationType == DataType.INTEGER) {
            registerAllocator = registerAllocatorInteger;
        }
        else {
            registerAllocator = registerAllocatorFloat;
        }

        Register destionationRegister = registerAllocator.allocateRegister(operandContainer.getDestination());
        text.append(saveVariableIntoMemory(destionationRegister, operandContainer.getDestination(), destinationType));

        // Check if it's a register to register assignment (a = b) or a direct assignment (a = 2).
        // REGISTER ASSIGNMENT
        if (operandContainer.getOperand1().isRegister()) {
            text.append(registerToRegisterAssignment(destionationRegister, operandContainer.getOperand1(), destinationType));
        } else {
            // DIRECT ASSIGNMENT (check the type of the variable to know what operations do).
            return switch (destinationType) {
                case INTEGER -> text.append(literalAssignment(destionationRegister, operandContainer.getOperand1(), DataType.INTEGER)).toString();
                case FLOAT -> text.append(literalAssignment(destionationRegister, operandContainer.getOperand1(), DataType.FLOAT)).toString();
                default -> null;
            };
        }

        // Ask for registers and assign the new values.

        return text.toString();
    }

    private String floatOperation(Operand destination, Operand operand1, Operand operand2, String operator) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorFloat.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.FLOAT);

        Register regOp2 = registerAllocatorFloat.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2, DataType.FLOAT);

        Register regDest = registerAllocatorFloat.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination, DataType.FLOAT);

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
            registerAllocatorFloat.freeRegister(operand2.getValue());
        }

        if (operand1.isTemporal()) {
            registerAllocatorFloat.freeRegister(operand1.getValue());
        }

        return text;
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

    private String saveVariableIntoMemory(Register register, Operand variableOffset, DataType dataType) {
        String text = LINE_INDENTATION;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                // Only load variables or literals, not temporal registers.
                if (!variableOffset.isTemporal() || !variableOffset.isRegister()) {
                    return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + LINE_SEPARATOR;
                }
                return "";
            }
            case SWAP_REGISTERS -> {
                text += loadVariableToMemory(register.getVariableName(), register.getRegisterName(), dataType) + LINE_SEPARATOR; // Write the operation to save the variable into memory.
                return text + LINE_INDENTATION + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + LINE_SEPARATOR;
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
        Register regOp1 = registerAllocatorInteger.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.INTEGER);

        Register regOp2 = registerAllocatorInteger.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2, DataType.INTEGER);

        Register regDest = registerAllocatorInteger.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination, DataType.INTEGER);

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
            registerAllocatorInteger.freeRegister(operand2.getValue());
        }

        if (operand1.isTemporal()) {
            registerAllocatorInteger.freeRegister(operand1.getValue());
        }

        return text;
    }
}
