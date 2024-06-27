package backEnd.targetCode;

import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import backEnd.targetCode.registers.RegisterAllocatorInteger;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.*;

public class MIPSOperations {
    protected static final String NL = System.lineSeparator(); // New line
    protected static final String TAB = "\t";
    protected static final String COMMENT_WORD = "#";

    protected static final String FP = "$fp";   // Frame pointer
    protected static final String SP = "$sp";    // Stack pointer
    protected static final String RETURN_ADDRESS_REGISTER = "$ra";
    protected static final String RETURN_VALUE_REGISTER = "$v0";
    protected static final String END_PROGRAM_INSTRUCTION = "syscall";
    protected static final String FUNCTION_RESULT_REGISTER = "$v0";
    protected final static String MAIN_FUNCTION = "ranch";
    protected static Stack<FunctionContext> functionStack = new Stack<>();
    protected static RegisterAllocator registerAllocatorInteger;
    protected static RegisterAllocator registerAllocatorFloat;
    protected final SymbolTableInterface symbolTable;
    protected final MIPSRenderer renderer;
    protected List<OperandContainer> pendingOperations = new LinkedList<>();
    protected List<OperandContainer> pendingLogicalOperations = new LinkedList<>();

    public MIPSOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat, MIPSRenderer mipsRenderer) {
        this.symbolTable = symbolTableInterface;
        MIPSOperations.registerAllocatorInteger = registerAllocatorInteger;
        MIPSOperations.registerAllocatorFloat = registerAllocatorFloat;
        this.renderer = mipsRenderer;
    }

    /**
     * Convert a float value to a hexadecimal string.
     *
     * @param value The float value to convert.
     * @return The hexadecimal string.
     */
    public static String floatToHex(float value) {
        int intBits = Float.floatToIntBits(value);
        return "0x" + Integer.toHexString(intBits);
    }

    protected String writeComment(String comment) {
        return (COMMENT_WORD + " " + comment);
    }

    protected ValueSymbol getOperandType(String operandValue) {
        try {
            return Tokenizer.convertStringIntoValueSymbol(operandValue);
        } catch (InvalidTokenException e) {
            return ValueSymbol.VALUE_INT;    // This should never happen.
        }
    }

    protected String loadVariableToMemory(String oldestVariable, String oldestRegister, DataType dataType) {
        if (dataType == DataType.FLOAT) {
            return "swc1 " + oldestRegister + ", " + oldestVariable;
        } else {
            return "sw " + oldestRegister + ", " + oldestVariable;
        }
    }

    protected String loadVariableToRegister(String oldestVariable, String oldestRegister, DataType dataType, boolean isLiteral) {
        Map<String, String> placeholders = new HashMap<>();
        String templatePath;
        String strippedRelativeOffset = oldestVariable.startsWith("-") ? oldestVariable.replaceFirst("-", "") : oldestVariable;

        return switch (dataType) {
            case FLOAT -> {
                if (isLiteral) {
                    String floatNumber = floatToHex(Float.parseFloat(oldestVariable));
                    Operand tempFloatOperand = new Operand(true, DataType.FLOAT, floatNumber, true);
                    Register tempFloatRegister = registerAllocatorInteger.allocateRegister(tempFloatOperand);

                    placeholders.put("tempFloatRegister", tempFloatRegister.getNotNullRegister());
                    placeholders.put("floatNumber", floatNumber);
                    placeholders.put("oldestRegister", oldestRegister);

                    templatePath = "load_float_literal_template";
                    String renderedTemplate = renderer.render(templatePath, placeholders);
                    registerAllocatorInteger.freeRegister(tempFloatRegister.getRegisterName());
                    yield renderedTemplate;
                } else {
                    placeholders.put("oldestRegister", oldestRegister);
                    placeholders.put("oldestVariable", strippedRelativeOffset);
                    //placeholders.put("oldestVariable", oldestVariable);
                    templatePath = "load_float_variable_template";
                    yield renderer.render(templatePath, placeholders);
                }
            }
            case INTEGER -> {
                if (isLiteral) {
                    placeholders.put("oldestRegister", oldestRegister);
                    placeholders.put("oldestVariable", oldestVariable);
                    templatePath = "load_integer_literal_template";
                } else {
                    placeholders.put("oldestRegister", oldestRegister);
                    placeholders.put("oldestVariable", strippedRelativeOffset);
                    //placeholders.put("oldestVariable", oldestVariable);
                    templatePath = "load_integer_variable_template";
                }
                yield renderer.render(templatePath, placeholders);
            }
            default -> "";
        };
    }

    protected Operand loadSingleOperand(String operandValue, boolean mainReturn) {
        if (operandValue == null) {
            return null;
        }

        Operand operand;
        ValueSymbol operandValueSymbol = getOperandType(operandValue);

        // Check if it's a variable (check its type in the symbols table).
        if (operandValueSymbol == ValueSymbol.VARIABLE) {
            String currentFunction = functionStack.peek().getFunctionName();

            // Get the previous function name if trying to return value from main.
            if (mainReturn && functionStack.size() > 1) {
                currentFunction = functionStack.get(functionStack.size() - 2).getFunctionName();
            }

            // Check if it's a function (assign the value to the return register).
            Symbol<?> function = symbolTable.findSymbolGlobally(operandValue);
            if (function != null && function.isFunction()) {
                operand = new Operand(true, function.getDataType(), RETURN_VALUE_REGISTER, false);
            } else {
                // It's a variable.
                Symbol<?> variable = symbolTable.findSymbolInsideFunction(operandValue, currentFunction);
                String variableRegister = variable.getOffset() + "(" + FP + ")";
                operand = new Operand(true, variable.getDataType(), variableRegister, false);
            }

        }
        // Check if it's a register. It's treated as a temporal (it will be removed from the registers when operated).
        else if (operandValue.startsWith(RegisterAllocatorInteger.REGISTER_PREFIX_TEMP)) {
            operand = new Operand(true, null, operandValue, true);
        }
        // Any other type of data (integer, float, boolean...). It's treated as a temporal (it will be removed from the registers when operated).
        else {
            DataType operandType = operandValueSymbol.getDataType();
            operand = new Operand(false, operandType, operandValue, true);
        }

        return operand;
    }

    protected void loadOperands(OperandContainer operandContainer, String destinationStr, String operand1Str, String operand2Str, String operatorStr, boolean returnValue) {
        operandContainer.setDestination(loadSingleOperand(destinationStr, returnValue));
        operandContainer.setOperand1(loadSingleOperand(operand1Str, returnValue));
        if (operand2Str != null) {
            operandContainer.setOperand2(loadSingleOperand(operand2Str, returnValue));
        }
        operandContainer.setOperator(operatorStr);
    }
}