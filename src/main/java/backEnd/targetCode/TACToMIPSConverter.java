package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.semantics.symbolTable.SymbolTableInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final String LINE_INDENTATION = "\t";
	private static final String FRAME_POINTER = "$fp";
	private static final String STACK_POINTER = "$sp";
	private static final String RETURN_REGISTER = "$ra";
	private static final String TARGET_FILE = "target/farm.asm";
	private BufferedWriter targetCode;
	private final SymbolTableInterface symbolTable;
	private final static String MAIN_FUNCTION = "main";

	public TACToMIPSConverter(SymbolTableInterface symbolTable) {
		this.symbolTable = symbolTable;
	}

	private void createAssemblyFile() throws FailedFileCreationException {
		// Generate a "farm.asm" file as target code inside /target folder.
		// Write the MIPS code to the file.
		File code = new File(TARGET_FILE);
		try {
			// Use FileWriter and BufferedWriter to write to the file
			FileWriter writer = new FileWriter(code, false);
			targetCode = new BufferedWriter(writer);
		} catch (IOException e) {
			throw new FailedFileCreationException("Error creating file: " + TARGET_FILE);
		}
	}

	@Override
	public void generateMIPS(List<TACInstruction> instructions) throws TargetCodeException {
		createAssemblyFile();

		for (TACInstruction instruction : instructions) {
			try {
				convertTACInstruction(instruction);
			} catch (IOException e) {
				throw new FailedFileCreationException(e.getMessage());
			}
		}

		try {
			targetCode.close();
		} catch (IOException e) {
			throw new FailedFileCreationException("Error writing to file: " + TARGET_FILE, e);
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
			// *** Binary Operations ***
			case "GT":
				// Greater than
				break;
			case "LT":
				// Less than
				break;
			case "EQ":
				// Equal
				break;
			case "NEQ":
				// Not equal
				break;
			case "OR":
				// Or
				break;
			case "AND":
				// And
				break;

			// *** Conditional ***
			case "IFz":
				// If zero
				break;

			// *** Arithmetic Operations ***
			case "SUM":
				// Sum
				break;
			case "SUB":
				break;
			case "MOD":
				break;
			case "MUL":
				break;
			case "POW":
				break;
			case "DIV":
				break;
		}
	}

	private String beginFunction(String functionLabel, String functionSize) {
		if (functionLabel.equals(MAIN_FUNCTION)) {
			return functionLabel + ":" + LINE_SEPARATOR +                    										// function start
					LINE_INDENTATION + "move" + FRAME_POINTER + ", " + STACK_POINTER + LINE_SEPARATOR +				// move $fp, $sp
					LINE_INDENTATION +  "sub " + STACK_POINTER + ", " + STACK_POINTER + ", " + functionSize + "\n";	// sub $sp, $sp, size
		}
		else {
			return functionLabel + ":" + LINE_SEPARATOR;
		}
	}

	private String endFunction() {
		return LINE_INDENTATION + "move " + STACK_POINTER + ", " + FRAME_POINTER + LINE_SEPARATOR +	// move $fp, $sp
				LINE_INDENTATION + "jr " + RETURN_REGISTER + LINE_SEPARATOR;							// jr $ra
	}
}
