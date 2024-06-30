package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.MIPSRenderer;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.Map;
import java.util.Objects;


public class AssignmentOperations extends MIPSOperations {

    public AssignmentOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat, MIPSRenderer mipsRenderer) {
        super(symbolTableInterface, registerAllocatorInteger, registerAllocatorFloat, mipsRenderer);
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
        StringBuilder text = new StringBuilder(saveVariableIntoMemory(variableRegister, registerValue, destinationType));

        // Prepare placeholders for rendering
        String registerName = variableRegister.getNotNullRegister();
        Map<String, String> placeholders = Map.of(
                "destinationRegister", destination.getNotNullRegister(),
                "sourceRegister", registerName
        );

        String templatePath;
        if (destinationType == DataType.FLOAT) {
            if (registerName.startsWith("$t")) {
                templatePath = "float_register_assignment_t_template";
            } else {
                templatePath = "float_register_assignment_f_template";
            }
        } else {
            String destinationRegisterName = destination.getNotNullRegister();
            if (destinationRegisterName.startsWith(PARAM_PREFIX)) {
                String registerMemoryAddress = registerAllocator.getRegisterMemoryAddress(destinationRegisterName);
                if (!registerMemoryAddress.isEmpty()) {

                    String nonNegativeMemoryAddress = registerMemoryAddress.startsWith("-") ? registerMemoryAddress.replaceFirst("-", "") : registerMemoryAddress;
                    Map<String, String> load_var_placeholders = Map.of(
                            "variableName", nonNegativeMemoryAddress,
                            "register", destinationRegisterName
                    );

                    text.append(renderer.render("load_variable_to_memory_template", load_var_placeholders));

                    addPairIfNotPresent(nonNegativeMemoryAddress, destinationRegisterName);
                }
            }
            templatePath = "integer_register_assignment_template";
        }

        // Render the template and append to the text
        text.append(renderer.render(templatePath, placeholders));

        return text.toString();
    }

    public String literalAssignment(Register destination, Operand literal, DataType dataType) {
        StringBuilder text = new StringBuilder();

        if (dataType == DataType.INTEGER) {
            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "destinationRegister", destination.getNotNullRegister(),
                    "literalValue", literal.getValue()
            );

            // Render the template and append to the text
            text.append(renderer.render("integer_literal_assignment_template", placeholders));
        } else {
            String floatNumber = floatToHex(Float.parseFloat(literal.getValue()));
            Operand tempFloatOperand = new Operand(true, DataType.FLOAT, floatNumber, true);
            Register tempFloatRegister = registerAllocatorInteger.allocateRegister(tempFloatOperand);

            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "tempFloatRegister", tempFloatRegister.getNotNullRegister(),
                    "floatNumber", floatNumber,
                    "destinationRegister", destination.getNotNullRegister()
            );

            // Render the template and append to the text
            text.append(renderer.render("float_literal_assignment_template", placeholders));

            // Free register
            registerAllocatorInteger.freeRegister(tempFloatRegister.getRegisterName());
        }

        return text.toString();
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
        Symbol<?> variable = symbolTable.findSymbolInsideFunction(destination, functionStack.peek().getFunctionName());
        DataType destinationType = variable.getDataType(); // We have the datatype of the variable that is being assigned.
        StringBuilder text = new StringBuilder();

        // Do all the previous operations.
        for (OperandContainer operation : this.pendingOperations) {
            switch (destinationType) {
                case INTEGER, BOOLEAN ->
                        text.append(integerOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
                case FLOAT ->
                        text.append(floatOperation(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase()));
            }
        }

        // Clear all the operations.
        this.pendingOperations.clear();

        return text.append(assignValueToRegister(operand1, destination, destinationType, true)).toString();
    }

    private String floatOperation(Operand destination, Operand operand1, Operand operand2, String operator) {
        StringBuilder text = new StringBuilder();

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorFloat.allocateRegister(operand1);
        text.append(saveVariableIntoMemory(regOp1, operand1, DataType.FLOAT));

        Register regOp2 = registerAllocatorFloat.allocateRegister(operand2);
        text.append(saveVariableIntoMemory(regOp2, operand2, DataType.FLOAT));

        Register regDest = registerAllocatorFloat.allocateRegister(destination);
        text.append(saveVariableIntoMemory(regDest, destination, DataType.FLOAT));

        // Prepare placeholders for rendering
        Map<String, String> placeholders = Map.of(
                "destinationRegister", regDest.getRegisterName(),
                "operand1Register", regOp1.getNotNullRegister(),
                "operand2Register", regOp2.getNotNullRegister()
        );

        String templatePath;
        switch (operator) {
            case "sum" -> templatePath = "float_operation_sum_template";
            case "sub" -> templatePath = "float_operation_sub_template";
            case "mul" -> templatePath = "float_operation_mul_template";
            case "div" -> templatePath = "float_operation_div_template";
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        // Render the template and append to the text
        text.append(renderer.render(templatePath, placeholders));

        // Free temporary registers (temporal registers generated by TAC or literals).
        if (operand2.isTemporal()) {
            registerAllocatorFloat.freeRegister(operand2.getValue());
        }

        if (operand1.isTemporal()) {
            registerAllocatorFloat.freeRegister(operand1.getValue());
        }

        // Compare the new result with the old result
        return text.toString();
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
        String text = "";

        switch (register.getRegisterEnum()) {
            case AVAILABLE_REGISTER -> {

                // Only load variables or literals, not temporal registers.
                if (!variableOffset.isTemporal() || !variableOffset.isRegister()) {
                    return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister());
                }
                return "";
            }
            case SWAP_REGISTERS -> {
                text += loadVariableToMemory(register.getVariableName(), register.getRegisterName(), dataType); // Write the operation to save the variable into memory.
                return text + loadVariableToRegister(variableOffset.getValue(), register.getRegisterName(), dataType, !variableOffset.isRegister());
            }
            // The variable was already loaded into a register.
            default -> {
                return "";
            }
        }
    }

    private String integerOperation(Operand destination, Operand operand1, Operand operand2, String operator) {
        StringBuilder text = new StringBuilder();

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorInteger.allocateRegister(operand1);
        text.append(saveVariableIntoMemory(regOp1, operand1, DataType.INTEGER));

        Register regOp2 = registerAllocatorInteger.allocateRegister(operand2);
        text.append(saveVariableIntoMemory(regOp2, operand2, DataType.INTEGER));

        Register regDest = registerAllocatorInteger.allocateRegister(destination);
        //text.append(saveVariableIntoMemory(regDest, destination, DataType.INTEGER));

        // Prepare placeholders for rendering
        Map<String, String> placeholders = Map.of(
                "destinationRegister", regDest.getRegisterName(),
                "operand1Register", regOp1.getNotNullRegister(),
                "operand2Register", regOp2.getNotNullRegister()
        );

        String templatePath;
        switch (operator) {
            case "sum" -> templatePath = "integer_operation_sum_template";
            case "sub" -> templatePath = "integer_operation_sub_template";
            case "mul" -> templatePath = "integer_operation_mul_template";
            case "div" -> templatePath = "integer_operation_div_template";
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        // Render the template and append to the text
        text.append(renderer.render(templatePath, placeholders));

        // Free temporary registers (temporal registers generated by TAC or literals).
        if (operand2.isTemporal()) {
            registerAllocatorInteger.freeRegister(operand2.getValue());
        }

        if (operand1.isTemporal()) {
            registerAllocatorInteger.freeRegister(operand1.getValue());
        }

        // Compare the new result with the old result
        return text.toString();
    }

    public String conditionalJump(String label, String operator) {
        // When pendingOperations is empty and the last operation it wasn't "or" or "and" we can do a direct jump.
        if (this.pendingLogicalOperations.isEmpty()) {
            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of("label", label);

            // Render the direct jump template
            return renderer.render("direct_jump_template", placeholders);

        }

        DataType dataType;

        Symbol<?> variable = symbolTable.findSymbolInsideFunction(this.pendingLogicalOperations.get(0).getOperand1().getValue(), functionStack.peek().getFunctionName());
        if (variable == null) {
            dataType = this.pendingLogicalOperations.get(0).getOperand1().getType();
        } else {
            dataType = variable.getDataType();
        }
        StringBuilder text = new StringBuilder();

        for (OperandContainer operation : this.pendingLogicalOperations) {
            if (!Objects.equals(operation.getOperator(), "IfZ")) {
                switch (dataType) {
                    case INTEGER, BOOLEAN ->
                            text.append(logicOperationInteger(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase(), label));
                    case FLOAT ->
                            text.append(logicOperationFloat(operation.getDestination(), operation.getOperand1(), operation.getOperand2(), operation.getOperator().toLowerCase(), label));
                }
                break;
            } else {
                text.append(logicOperationInteger(operation.getDestination(), operation.getOperand1(), null, operation.getOperator().toLowerCase(), label));
            }

        }

        this.pendingLogicalOperations.clear();

        // Compare the new result with the old result
        return text.toString();
    }

    private String logicOperationFloat(Operand destination, Operand operand1, Operand operand2, String operator, String label) {
        StringBuilder text = new StringBuilder();

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorFloat.allocateRegister(operand1);
        text.append(saveVariableIntoMemory(regOp1, operand1, DataType.FLOAT));
        Register regOp2;
        if (operand2 != null) {
            regOp2 = registerAllocatorFloat.allocateRegister(operand2);
            text.append(saveVariableIntoMemory(regOp2, operand2, DataType.FLOAT));

            Register regDest = registerAllocatorFloat.allocateRegister(destination);
            text.append(saveVariableIntoMemory(regDest, destination, DataType.FLOAT));

            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "operand1Register", regOp1.getNotNullRegister(),
                    "operand2Register", regOp2.getNotNullRegister(),
                    "label", label
            );

            String templatePath;
            switch (operator) {
                case "eq" -> templatePath = "logic_operation_float_eq_template";
                case "neq" -> templatePath = "logic_operation_float_neq_template";
                case "lt" -> templatePath = "logic_operation_float_lt_template";
                case "gt" -> templatePath = "logic_operation_float_gt_template";
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            }

            // Render the template and append to the text
            text.append(renderer.render(templatePath, placeholders));

            // Free temporary registers (temporal registers generated by TAC or literals).
            if (operand2.isTemporal()) {
                registerAllocatorFloat.freeRegister(operand2.getValue());
            }

            if (operand1.isTemporal()) {
                registerAllocatorFloat.freeRegister(operand1.getValue());
            }
        }

        return text.toString();
    }


    private String logicOperationInteger(Operand destination, Operand operand1, Operand operand2, String operator, String label) {
        StringBuilder text = new StringBuilder();

        // Allocate temporary registers.
        Register regOp1 = registerAllocatorInteger.allocateRegister(operand1);
        text.append(saveVariableIntoMemory(regOp1, operand1, DataType.INTEGER));
        Register regOp2;

        if (operand2 != null && !operator.equals("ifz")) {
            regOp2 = registerAllocatorInteger.allocateRegister(operand2);
            text.append(saveVariableIntoMemory(regOp2, operand2, DataType.INTEGER));

            Register regDest = registerAllocatorInteger.allocateRegister(destination);
            text.append(saveVariableIntoMemory(regDest, destination, DataType.INTEGER));

            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "destinationRegister", regDest.getRegisterName(),
                    "operand1Register", regOp1.getNotNullRegister(),
                    "operand2Register", regOp2.getNotNullRegister(),
                    "label", label
            );

            String templatePath;
            switch (operator) {
                case "eq" -> templatePath = "logic_operation_integer_eq_template";
                case "neq" -> templatePath = "logic_operation_integer_neq_template";
                case "lt" -> templatePath = "logic_operation_integer_lt_template";
                case "gt" -> templatePath = "logic_operation_integer_gt_template";
                case "or" -> templatePath = "logic_operation_integer_or_template";
                case "and" -> templatePath = "logic_operation_integer_and_template";
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            }

            // Render the template and append to the text
            text.append(renderer.render(templatePath, placeholders));

            // Free temporary registers (temporal registers generated by TAC or literals).
            if (operand2.isTemporal()) {
                registerAllocatorInteger.freeRegister(operand2.getValue());
            }
        }

        if (operator.equals("ifz")) {
            // Prepare placeholders for rendering
            Map<String, String> ifzPlaceholders = Map.of(
                    "operand1Register", regOp1.getNotNullRegister(),
                    "label", label
            );

            // Render the ifz template and append to the text
            text.append(renderer.render("logic_operation_integer_ifz_template", ifzPlaceholders));
        }

        if (operand1.isTemporal()) {
            registerAllocatorInteger.freeRegister(operand1.getValue());
        }

        return text.toString();
    }

    public String createLabel(String labelName) {
        return labelName + ":" + NL;
    }

    public String notOperation(String operand1, String result) {
        OperandContainer operandContainer = new OperandContainer();
        loadOperands(operandContainer, result, operand1, null, "not", false);
        this.pendingLogicalOperations.add(operandContainer);

        return null;
    }
}
