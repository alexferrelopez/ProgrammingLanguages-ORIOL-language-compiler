package backEnd.targetCode;

import java.util.*;

public class RegisterAllocator {
	private static final int NUM_TEMP_REGISTERS = 10;
	public static final String REGISTER_PREFIX = "$t";
	private final Stack<String> availableRegisters;
	private final Map<String, String> variableToRegister;

	public RegisterAllocator() {
		availableRegisters = new Stack<>();
		for (int i = NUM_TEMP_REGISTERS - 1; i >= 0 ; i--) {
			availableRegisters.add(REGISTER_PREFIX + i);
		}
		variableToRegister = new HashMap<>();
	}

	public String allocateRegister(String variable) {
		// Check if the variable already has a register assigned.
		if (variableToRegister.containsKey(variable)) {
			return variableToRegister.get(variable);
		}

		// Find an unused register
		if (!availableRegisters.isEmpty()) {
			String reg = availableRegisters.pop();
			variableToRegister.put(variable, reg);
			return reg;
		}

		// If no registers are available, throw an error (this should never happen with our grammar).
		return null;
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