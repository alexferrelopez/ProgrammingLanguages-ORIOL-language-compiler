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

        // Assign the registers' values using different operations.
        if (destinationType == DataType.FLOAT) {
            // Check if the operand is $t or $f
            if (variableRegister.getNotNullRegister().startsWith("$t")) {
                text += LINE_INDENTATION +
                        ("mtc1 " + variableRegister.getNotNullRegister() + ", " + destination.getNotNullRegister()) + LINE_SEPARATOR;
            }
            else {
                text += LINE_INDENTATION +
                        ("mov.s " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + LINE_SEPARATOR;
            }
        }
        else {
            text += LINE_INDENTATION +
                    ("move " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + LINE_SEPARATOR;
        }

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
            String floatNumber = floatToHex(Float.parseFloat(literal.getValue()));
            Operand tempFloatOperand = new Operand(true, DataType.FLOAT, floatNumber, true);
            Register tempFloatRegister = registerAllocatorInteger.allocateRegister(tempFloatOperand);

            String text = literalAssignment(tempFloatRegister, tempFloatOperand, DataType.INTEGER);

            // String text = LINE_INDENTATION + "li" + tempFloatRegister.getNotNullRegister() + ", " + floatNumber + LINE_SEPARATOR;

            text += LINE_INDENTATION +
                    ("mtc1 " + tempFloatRegister.getNotNullRegister() + ", " + destination.getNotNullRegister()) + LINE_SEPARATOR;

            // Free register
            registerAllocatorInteger.freeRegister(tempFloatRegister.getRegisterName());

            return text;
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
        switch (operator) {
            case "sum" -> text += LINE_INDENTATION + ("add.s " + regDest.getRegisterName() +  ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "sub" -> text += LINE_INDENTATION + ("sub.s " + regDest.getRegisterName() +  ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "mul" -> text += LINE_INDENTATION + ("mul.s " + regDest.getRegisterName() +  ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "div" -> text += LINE_INDENTATION + ("div.s " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
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

    public String conditionalJump(String label, String operator) {
        // When pendingOperations is empty and the last operation it wasn't "or" or "and" we can do a direct jump.
        if (this.pendingOperations.isEmpty()) {
            // Do a direct jump.
            return LINE_INDENTATION + "j " + label + LINE_SEPARATOR;
        }

        DataType dataType;

        Symbol<?> variable = symbolTable.findSymbolInsideFunction(this.pendingOperations.get(0).getOperand1().getValue(), currentFunctionName);
        if (variable == null) {
            dataType = this.pendingOperations.get(0).getOperand1().getType();
        }
        else {
            dataType = variable.getDataType();
        }
        String text = "";
        for (OperandContainer operation : this.pendingOperations) {
            switch (dataType) {
                case INTEGER -> {
                    text =  logicOperationInteger(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase(), label);
                }
                case FLOAT -> {
                    text =  logicOperationFloat(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase());
                }
            }
        }

        this.pendingOperations.clear();

        return text;
    }

    private String logicOperationFloat(Operand destination, Operand operand1, Operand operand2, String operator) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorFloat.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.FLOAT);

        Register regOp2 = registerAllocatorFloat.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2, DataType.FLOAT);

        Register regDest = registerAllocatorFloat.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination, DataType.FLOAT);

        // Make a division or a general operation (sum, subtract or multiplication).
        switch (operator) {
            case "eq" -> text += LINE_INDENTATION + ("c.eq.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "ne" -> text += LINE_INDENTATION + ("c.ne.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "lt" -> text += LINE_INDENTATION + ("c.lt.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "gt" -> text += LINE_INDENTATION + ("c.gt.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "le" -> text += LINE_INDENTATION + ("c.le.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "ge" -> text += LINE_INDENTATION + ("c.ge.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
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

    private String logicOperationInteger(Operand destination, Operand operand1, Operand operand2, String operator, String label) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorInteger.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.INTEGER);

        Register regOp2 = registerAllocatorInteger.allocateRegister(operand2);
        text += saveVariableIntoMemory(regOp2, operand2, DataType.INTEGER);

        Register regDest = registerAllocatorInteger.allocateRegister(destination);
        text += saveVariableIntoMemory(regDest, destination, DataType.INTEGER);

        // Make a division or a general operation (sum, subtract or multiplication).
        switch (operator) {
            case "eq" -> text += LINE_INDENTATION + ("bne " + regOp2.getNotNullRegister() +  ", " + regOp1.getNotNullRegister() + ", " + label) + LINE_SEPARATOR;
            case "neq" -> text += LINE_INDENTATION + ("beq "+ regOp2.getNotNullRegister() +  ", " + regOp1.getNotNullRegister() + ", " + label) + LINE_SEPARATOR;
            case "lt" -> text += LINE_INDENTATION + ("blt " + regOp2.getNotNullRegister() +  ", " + regOp1.getNotNullRegister() + ", " + label) + LINE_SEPARATOR;
            case "gt" -> text += LINE_INDENTATION + ("bgt " + regOp2.getNotNullRegister() +  ", " + regOp1.getNotNullRegister() + ", " + label) + LINE_SEPARATOR;
            case "or" -> text += LINE_INDENTATION + ("or "  + regDest.getRegisterName() +  ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
            case "and" -> text += LINE_INDENTATION + ("and "+ regDest.getRegisterName() +  ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + LINE_SEPARATOR;
        }

        // If the operator is "or" or "and" we have to save the result in the destination register and do a neq comparison with 0
        if (operator.equals("or") || operator.equals("and")) {
            text += LINE_INDENTATION + ("beq " + regDest.getRegisterName() + ", $zero, " + label) + LINE_SEPARATOR;
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

    private DataType getDataType(String operand1) {
        return isInteger(operand1) ? DataType.INTEGER : DataType.FLOAT;
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String saveTemporalVariableIntoMemory(Register register, Operand variableOffset, DataType dataType) {
        String text = LINE_INDENTATION;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                    return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + LINE_SEPARATOR;

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

    public String createLabel(String labelName) {
        return labelName + ":" + LINE_SEPARATOR;
    }
}
