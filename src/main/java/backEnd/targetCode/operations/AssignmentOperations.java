package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.Objects;


public class AssignmentOperations extends MIPSOperations {

    public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat) {
        super(symbolTableInterface, registerAllocatorInteger, registerAllocatorFloat);
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Destination must always be loaded into memory when calling this function.
    public String registerToRegisterAssignment(Register destination, Operand registerValue, DataType destinationType) {
        // Get the register from the variable that is being assigned.
        Register variableRegister;
        RegisterAllocator registerAllocator;

        if (destinationType == DataType.INTEGER) {
            registerAllocator = registerAllocatorInteger;
        } else {
            registerAllocator = registerAllocatorFloat;
        }

        variableRegister = registerAllocator.allocateRegister(registerValue);
        String text = saveVariableIntoMemory(variableRegister, registerValue, destinationType);

        // Assign the registers' values using different operations.
        if (destinationType == DataType.FLOAT) {
            // Check if the operand is $t or $f
            if (variableRegister.getNotNullRegister().startsWith("$t")) {
                text += TAB +
                        ("mtc1 " + variableRegister.getNotNullRegister() + ", " + destination.getNotNullRegister()) + NL;
            } else {
                text += TAB +
                        ("mov.s " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + NL;
            }
        } else {
            text += TAB +
                    ("move " + destination.getNotNullRegister() + ", " + variableRegister.getNotNullRegister()) + NL;
        }

        return text;
    }

    public String literalAssignment(Register destination, Operand literal, DataType dataType) {
        if (dataType == DataType.INTEGER) {
            return TAB +
                    ("li " + destination.getNotNullRegister() + ", " + literal.getValue()) + NL;
        } else {
            String floatNumber = floatToHex(Float.parseFloat(literal.getValue()));
            Operand tempFloatOperand = new Operand(true, DataType.FLOAT, floatNumber, true);
            Register tempFloatRegister = registerAllocatorInteger.allocateRegister(tempFloatOperand);

            String text = literalAssignment(tempFloatRegister, tempFloatOperand, DataType.INTEGER);

            // String text = LINE_INDENTATION + "li" + tempFloatRegister.getNotNullRegister() + ", " + floatNumber + LINE_SEPARATOR;

            text += TAB +
                    ("mtc1 " + tempFloatRegister.getNotNullRegister() + ", " + destination.getNotNullRegister()) + NL;

            // Free register
            registerAllocatorInteger.freeRegister(tempFloatRegister.getRegisterName());

            return text;
        }
    }

    public String assignValueToRegister(String operand1, String destination, DataType destinationType, boolean freeTemporalsUsed) {
        StringBuilder text = new StringBuilder();
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, null, "=", !freeTemporalsUsed);
        RegisterAllocator registerAllocator;

        // Load the destination register into memory (in case it is not already).
        if (destinationType == DataType.INTEGER) {
            registerAllocator = registerAllocatorInteger;
        } else {
            registerAllocator = registerAllocatorFloat;
        }

        Register destionationRegister = registerAllocator.allocateRegister(operandContainer.getDestination());
        text.append(saveVariableIntoMemory(destionationRegister, operandContainer.getDestination(), destinationType));

        // Check if it's a register to register assignment (a = b) or a direct assignment (a = 2).
        // REGISTER ASSIGNMENT
        if (operandContainer.getOperand1().isRegister()) {
            text.append(registerToRegisterAssignment(destionationRegister, operandContainer.getOperand1(), destinationType));
            if (freeTemporalsUsed) {
                if (operandContainer.getOperand1().isTemporal()) {
                    registerAllocator.freeRegister(operandContainer.getOperand1().getValue());
                }
            }
        } else {
            // DIRECT ASSIGNMENT (check the type of the variable to know what operations do).
            return switch (destinationType) {
                case INTEGER, BOOLEAN ->
                        text.append(literalAssignment(destionationRegister, operandContainer.getOperand1(), DataType.INTEGER)).toString();
                case FLOAT ->
                        text.append(literalAssignment(destionationRegister, operandContainer.getOperand1(), DataType.FLOAT)).toString();
                default -> "";
            };
        }

        // Ask for registers and assign the new values.

        return text.toString();
    }

    public String assignmentOperation(String operand1, String destination) {
        Symbol<?> variable = symbolTable.findSymbolInsideFunction(destination, currentFunctionName.peek());
        DataType destinationType = variable.getDataType(); // We have the datatype of the variable that is being assigned.
        StringBuilder text = new StringBuilder();

        // Do all the previous operations.
        for (OperandContainer operation : this.pendingOperations) {
            switch (destinationType) {
                case INTEGER, BOOLEAN -> {
                    text.append(integerOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                }
                case FLOAT -> {
                    text.append(floatOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                }
            }
        }

        // Clear all the operations.
        this.pendingOperations.clear();

        return text.append(assignValueToRegister(operand1, destination, destinationType, true)).toString();
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
            case "sum" ->
                    text += TAB + ("add.s " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
            case "sub" ->
                    text += TAB + ("sub.s " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
            case "mul" ->
                    text += TAB + ("mul.s " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
            case "div" ->
                    text += TAB + ("div.s " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
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
        return TAB +
                ("li.s " + offset + ", " + operand1) + NL;
    }

    public String addPendingOperation(String operand1, String operand2, String destination, String operator) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2, operator, false);
        this.pendingOperations.add(operandContainer);

        return null;
    }

    public String addPendingLogicalOperation(String operand1, String operand2, String destination, String operator) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, destination, operand1, operand2, operator, false);
        this.pendingLogicalOperations.add(operandContainer);

        return null;
    }

    private String saveVariableIntoMemory(Register register, Operand variableOffset, DataType dataType) {
        String text = TAB;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                // Only load variables or literals, not temporal registers.
                if (!variableOffset.isTemporal() || !variableOffset.isRegister()) {
                    return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + NL;
                }
                return "";
            }
            case SWAP_REGISTERS -> {
                text += loadVariableToMemory(register.getVariableName(), register.getRegisterName(), dataType) + NL; // Write the operation to save the variable into memory.
                return text + TAB + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + NL;
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
            text += TAB + (operator + " " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
            text += TAB + ("mflo " + regDest.getRegisterName()) + NL;
        } else {
            if (operator.equals("sum")) {
                operator = "add";
            }
            text += TAB + (operator + " " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
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
        if (this.pendingLogicalOperations.isEmpty()) {
            // Do a direct jump.
            return TAB + "j " + label + NL;
        }

        DataType dataType;

        Symbol<?> variable = symbolTable.findSymbolInsideFunction(this.pendingLogicalOperations.get(0).getOperand1().getValue(), currentFunctionName.peek());
        if (variable == null) {
            dataType = this.pendingLogicalOperations.get(0).getOperand1().getType();
        } else {
            dataType = variable.getDataType();
        }
        String text = "";
        for (OperandContainer operation : this.pendingLogicalOperations) {
            if (!Objects.equals(operation.getOperator(), "IfZ")) {
                switch (dataType) {
                    case INTEGER, BOOLEAN -> {
                        text += logicOperationInteger(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase(), label);
                    }
                    case FLOAT -> {
                        text += logicOperationFloat(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase(), label);
                    }
                }
                break;
            } else {
                text += logicOperationInteger(operation.getDestination(), operation.getOperand1(), null, operation.getOperator().toLowerCase(), label);
            }

        }

        this.pendingLogicalOperations.clear();

        return text;
    }

    private String logicOperationFloat(Operand destination, Operand operand1, Operand operand2, String operator, String label) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorFloat.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.FLOAT);
        Register regOp2;
        if (operand2 != null) {
            regOp2 = registerAllocatorFloat.allocateRegister(operand2);
            text += saveVariableIntoMemory(regOp2, operand2, DataType.FLOAT);

            Register regDest = registerAllocatorFloat.allocateRegister(destination);
            text += saveVariableIntoMemory(regDest, destination, DataType.FLOAT);

            // Make a division or a general operation (sum, subtract or multiplication).
            switch (operator) {
                case "eq" ->
                        text += TAB + ("c.eq.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL
                                + TAB + ("bc1f " + label) + NL;
                case "neq" ->
                        text += TAB + ("c.eq.s " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL
                                + TAB + ("bc1t " + label) + NL;
                case "lt" ->
                        text += TAB + ("c.lt.s " + regOp2.getNotNullRegister() + ", " + regOp1.getNotNullRegister()) + NL
                                + TAB + ("bc1t " + label) + NL;
                case "gt" ->
                        text += TAB + ("c.lt.s " + regOp2.getNotNullRegister() + ", " + regOp1.getNotNullRegister()) + NL
                                + TAB + ("bc1f " + label) + NL;
            }


            // Free temporary registers (temporal registers generated by TAC or literals).
            if (operand2.isTemporal()) {
                registerAllocatorFloat.freeRegister(operand2.getValue());
            }

            if (operand1.isTemporal()) {
                registerAllocatorFloat.freeRegister(operand1.getValue());
            }
        }

        return text;
    }

    private String logicOperationInteger(Operand destination, Operand operand1, Operand operand2, String operator, String label) {
        String text;

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorInteger.allocateRegister(operand1);
        text = saveVariableIntoMemory(regOp1, operand1, DataType.INTEGER);
        Register regOp2;


        if (operand2 != null && !operator.equals("ifz")) {

            regOp2 = registerAllocatorInteger.allocateRegister(operand2);
            text += saveVariableIntoMemory(regOp2, operand2, DataType.INTEGER);

            Register regDest = registerAllocatorInteger.allocateRegister(destination);
            text += saveVariableIntoMemory(regDest, destination, DataType.INTEGER);


            // Make a division or a general operation (sum, subtract or multiplication).
            switch (operator) {
                case "eq" ->
                        text += TAB + ("bne " + regOp2.getNotNullRegister() + ", " + regOp1.getNotNullRegister() + ", " + label) + NL;
                case "neq" ->
                        text += TAB + ("beq " + regOp2.getNotNullRegister() + ", " + regOp1.getNotNullRegister() + ", " + label) + NL;
                case "lt" ->
                        text += TAB + ("bge " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister() + ", " + label) + NL;
                case "gt" ->
                        text += TAB + ("ble " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister() + ", " + label) + NL;
                case "or" ->
                        text += TAB + ("or " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;
                case "and" ->
                        text += TAB + ("and " + regDest.getRegisterName() + ", " + regOp1.getNotNullRegister() + ", " + regOp2.getNotNullRegister()) + NL;

            }

            // If the operator is "or" or "and" we have to save the result in the destination register and do a neq comparison with 0
            if (operator.equals("or") || operator.equals("and")) {
                text += TAB + ("beq " + regDest.getRegisterName() + ", $zero, " + label) + NL;
            }

            // Free temporary registers (temporal registers generated by TAC or literals).
            if (operand2.isTemporal()) {
                registerAllocatorInteger.freeRegister(operand2.getValue());
            }
        }

        if (operator.equals("ifz")) {
            text += TAB + ("beq " + regOp1.getNotNullRegister() + ", $zero, " + label) + NL;

        }


        if (operand1.isTemporal()) {
            registerAllocatorInteger.freeRegister(operand1.getValue());
        }

        return text;
    }

    private DataType getDataType(String operand1) {
        return isInteger(operand1) ? DataType.INTEGER : DataType.FLOAT;
    }

    private String saveTemporalVariableIntoMemory(Register register, Operand variableOffset, DataType dataType) {
        String text = TAB;

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + NL;

            }
            case SWAP_REGISTERS -> {
                text += loadVariableToMemory(register.getVariableName(), register.getRegisterName(), dataType) + NL; // Write the operation to save the variable into memory.
                return text + TAB + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister()) + NL;
            }
            // The variable was already loaded into a register.
            default -> {
                return "";
            }
        }
    }

    public String createLabel(String labelName) {
        return labelName + ":" + NL;
    }
}
