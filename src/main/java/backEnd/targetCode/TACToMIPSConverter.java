package backEnd.targetCode;

import frontEnd.exceptions.InvalidFileException;
import frontEnd.intermediateCode.TACInstruction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TACToMIPSConverter implements TACToMIPSConverterInterface {
	private static final String lineSeparator = System.lineSeparator();
	private static final String framePointer = "$fp";
	private static final String stackPointer = "$sp";
	private static final String returnRegister = "$ra";
	private static final String targetFile = "target/farm.asm";
	private BufferedWriter targetCode;

	private void createAssemblyFile() throws InvalidFileException {
		// Generate a "farm.asm" file as target code inside /target folder.
		// Write the MIPS code to the file.
		File code = new File(targetFile);
		try {
			if (code.createNewFile()) {
				// Use FileWriter and BufferedWriter to write to the file
				FileWriter writer = new FileWriter(code);
				targetCode = new BufferedWriter(writer);
			}
		} catch (IOException e) {
			throw new InvalidFileException("Error creating file: " + targetFile);
		}
	}

	@Override
	public void generateMIPS(List<TACInstruction> instructions) throws InvalidFileException {
		createAssemblyFile();

		for (TACInstruction instruction : instructions) {
			try {
				convertTACInstruction(instruction);
			} catch (IOException e) {
				throw new InvalidFileException(e.getMessage());
			}
		}
	}

	private void convertTACInstruction(TACInstruction instruction) throws IOException {
		switch (instruction.getOperator()) {
			case "=":
				// Assignment
				// mipsCode.append(assign(instruction.getResult(), instruction.getOperand1()));
				break;
			case "BeginFunc":
				// Begin Function
				targetCode.write(beginFunction(instruction.getResult(), instruction.getOperand1()));
				break;
			case "EndFunc":
				// End Function
				targetCode.write(endFunction());
				break;
			case "SUM":
				// Sum
				break;

		}
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
