package backEnd.targetCode;

import java.util.*;

public class RegisterAllocator {
	private static final int NUM_TEMP_REGISTERS = 10;
	private static final int NUM_SAVE_REGISTERS = 8;
	public static final String REGISTER_PREFIX_TEMP = "$t";
	public static final String REGISTER_PREFIX_SAVE = "$s";
	private final Stack<String> availableRegisters;
	private final Map<String, String> variableToRegister; // <variableOffset, register>

	public RegisterAllocator() {
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
	public String[] allocateRegister(String variable) {
		// Check if the variable already has a register assigned.
		if (variableToRegister.containsKey(variable)) {
			// If the variable is already in a register, move it to the top of the list.

			// Do the swap between the register and the last element of the list.
			String variableRegister = variableToRegister.get(variable);
			availableRegisters.remove(variableRegister);
			availableRegisters.add(variableRegister);	// Set the register to be the most recent one.

			return new String[] { variableRegister, null };
		}

		// Find an unused register (if there is one at least).
		if (!availableRegisters.isEmpty()) {
			String reg = availableRegisters.pop();
			variableToRegister.put(variable, reg);
			return new String[] { reg };
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

			return new String[] { oldestRegister, oldestVariable };
		}
	}

	public String getRegister(String variable) {
		return variableToRegister.get(variable);
	}

	public void freeRegister(String variable) {
		if (variableToRegister.containsKey(variable)) {
			availableRegisters.add(variableToRegister.remove(variable));
			variableToRegister.remove(variable);
		}
	}
}