package backEnd.targetCode;

import backEnd.targetCode.registers.RegisterAllocator;
import backEnd.targetCode.registers.RegisterAllocatorInteger;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.LinkedList;
import java.util.List;

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
    protected static RegisterAllocator registerAllocatorInteger;
    protected static RegisterAllocator registerAllocatorFloat;
    protected List<OperandContainer> pendingOperations = new LinkedList<>();

    public MIPSOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat) {
        this.symbolTable = symbolTableInterface;
        MIPSOperations.registerAllocatorInteger = registerAllocatorInteger;
        MIPSOperations.registerAllocatorFloat = registerAllocatorFloat;
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

        // We have to check if the variable is a float or an integer due to the different instructions.
        if (dataType == DataType.FLOAT) {
            // Also, we have to check if it's a literal or a variable because the instruction changes.
            if (isLiteral) {
                return "li.s " + oldestRegister + ", " + oldestVariable;
            }
            else {
                return "lwc1 " + oldestRegister + ", " + oldestVariable;
            }
        } else {
            if (isLiteral) {
                return "li " + oldestRegister + ", " + oldestVariable;
            }
            else {
                return "lw " + oldestRegister + ", " + oldestVariable;
            }
        }
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

    protected void loadOperands(OperandContainer operandContainer, String destinationStr, String operand1Str, String operand2Str, String operatorStr) {
        operandContainer.setDestination(loadSingleOperand(destinationStr));
        operandContainer.setOperand1(loadSingleOperand(operand1Str));
        if (operand2Str != null) {
            operandContainer.setOperand2(loadSingleOperand(operand2Str));
        }
        operandContainer.setOperator(operatorStr);
    }
}
