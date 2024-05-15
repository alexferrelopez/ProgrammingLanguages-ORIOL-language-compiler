package backEnd.targetCode;

import frontEnd.intermediateCode.TACInstruction;

import java.util.List;

public class TACToMIPSConverter implements TACToMIPSConverterInterface {
	private static final String lineSeparator = System.lineSeparator();
	private static final String framePointer = "$fp";
	private static final String stackPointer = "$sp";
	private static final String returnRegister = "$ra";

	@Override
	public void generateMIPS(List<TACInstruction> instructions) {
		for (TACInstruction instruction : instructions) {
			convertTACInstruction();
		}
	}

	private void convertTACInstruction() {

	}

	private String beginFunction(String functionLabel, String functionSize) {
		return functionLabel + ":" + lineSeparator +                    					// function start
				"move" + framePointer + ", " + stackPointer + lineSeparator +				// move $fp, $sp
				"sub" + stackPointer + ", " + stackPointer + ", " + functionSize + "\n";	// sub $sp, $sp, size
	}

	private String endFunction() {
		return "move " + stackPointer + ", " + framePointer + lineSeparator +	// move $fp, $sp
				"jr " + returnRegister + lineSeparator;							// jr $ra
	}
}
