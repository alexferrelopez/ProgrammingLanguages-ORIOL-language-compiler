package backEnd.targetCode.registers;

import backEnd.targetCode.Operand;

import java.util.*;

import static backEnd.targetCode.registers.OptionRegisterEnum.*;

public class RegisterAllocatorInteger implements RegisterAllocator {
	private static final int NUM_TEMP_REGISTERS = 10;
	private static final int NUM_SAVE_REGISTERS = 8;
	public static final String REGISTER_PREFIX_TEMP = "$t";
	public static final String REGISTER_PREFIX_SAVE = "$s";
	private final Stack<String> availableRegisters;
	private final Map<String, String> variableToRegister; // <variableOffset, register>

	public RegisterAllocatorInteger() {
		availableRegisters = new Stack<>();

		// Add the temporary registers to the list.

		// For $s registers
		for (int i = NUM_SAVE_REGISTERS - 1; i >= 0 ; i--) {
			availableRegisters.add(REGISTER_PREFIX_SAVE + i);
		}

		// For $t registers
		for (int i = NUM_TEMP_REGISTERS - 1; i >= 0 ; i--) {
			availableRegisters.add(REGISTER_PREFIX_TEMP + i);
		}

		variableToRegister = new LinkedHashMap<>();
	}

	// First position is the available register for the variable.
	// Second position is the variable that was removed from the register (in case it was needed).
	public Register allocateRegister(Operand variableOperand) {
		String variable = variableOperand.getValue();

		// Check if the variable already has a register assigned.
		if (variableToRegister.containsKey(variable)) {
			// If the variable is already in a register, move it to the top of the list.

			// Do the swap between the register and the last element of the list.
			return getRegister(variable);
		}

		// Find an unused register (if there is one at least).
		if (!availableRegisters.isEmpty()) {
			String reg = availableRegisters.pop();
			variableToRegister.put(variable, reg);
			return new Register (AVAILABLE_REGISTER, reg, null);
		}
		else {
			// There are no available registers.

			// Get the oldest (first) variable in a register.
			String oldestVariable = variableToRegister.keySet().iterator().next();

			// Get the register of the oldest variable.
			String oldestRegister = variableToRegister.get(oldestVariable);

			// Remove the oldest variable from the register.
			variableToRegister.remove(oldestVariable);

			// Add the new variable to the register.
			variableToRegister.put(variable, oldestRegister);

			// Check if it's a variable (not a temporal).
			return new Register (SWAP_REGISTERS, oldestRegister, oldestVariable);
		}
	}

	public Register getRegister(String variable) {
		String variableRegister = variableToRegister.get(variable);
		variableToRegister.remove(variable);
		variableToRegister.put(variable, variableRegister);	// Set the register to be the most recent one.

		return new Register (VARIABLE_ALREADY_IN_REGISTER, null, variableRegister);
	}

	public void freeRegister(String variable) {
		if (variableToRegister.containsKey(variable)) {
			availableRegisters.add(variableToRegister.remove(variable));
			variableToRegister.remove(variable);
		}
	}
}