package backEnd.targetCode.registers;

import backEnd.targetCode.Operand;

public interface RegisterAllocator {
    Register allocateRegister(Operand variableOperand);
    Register getRegister(String variable);
    void freeRegister(String variable);
}
