package backEnd.targetCode.operations;

public enum OptionRegisterEnum {
    VARIABLE_ALREADY_IN_REGISTER,   // The variable is already stored in a register.
    AVAILABLE_REGISTER,  // The variable is stored in a new register.
    SWAP_REGISTERS;  // The variable is stored in a register that was already in use.
}
