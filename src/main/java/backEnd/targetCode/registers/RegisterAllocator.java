package backEnd.targetCode.registers;

import backEnd.targetCode.Operand;

import java.util.Map;

public interface RegisterAllocator {
    Register allocateRegister(Operand variableOperand);
    Register getRegister(String variable);
    void freeRegister(String variable);
    Map<String, String> getVariableToRegister();
    Register customAllocateRegister(String variable, String destination);
    Map<String, String> getVariableToCustomRegister();
}
