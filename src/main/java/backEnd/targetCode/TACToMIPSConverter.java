package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final String LINE_INDENTATION = "\t";
	private static final String COMMENT_WORD = "#";

	private static final String FRAME_POINTER = "$fp";
	private static final String STACK_POINTER = "$sp";
	private static final String RETURN_REGISTER = "$ra";
	private static final String END_PROGRAM_INSTRUCTION = "syscall";
	private static final String FUNCTION_RESULT_REGISTER = "$v0";

	private static final String TARGET_FILE = "target/farm.asm";
	private BufferedWriter targetCode;
	private final SymbolTableInterface symbolTable;

	private final static String MAIN_FUNCTION = "ranch";
	private String currentFunctionName;

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
			// ** Functions ** //
			case "function":
				targetCode.write(funcDeclaration(instruction.getResult()));
				break;
			case "Return":
				targetCode.write(returnFunction(instruction.getOperand1()));
			case "=":
				// Assignment
				// mipsCode.append(assign(instruction.getResult(), instruction.getOperand1()));
				break;
			case "BeginFunc":
				// Begin Function
				targetCode.write(beginFunction(instruction.getOperand1()));
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

	private String writeComment(String comment) {
		return (COMMENT_WORD + " " + comment);
	}

	private String funcDeclaration(String functionLabel) {
		this.currentFunctionName = functionLabel;

		String text = writeComment("Start of function " + functionLabel) + LINE_SEPARATOR +
				(functionLabel + ":") + LINE_SEPARATOR;
		/*
			addi $sp, $sp, -8   # Allocate stack frame
			sw $ra, 4($sp)      # Save return address
			sw $fp, 0($sp)      # Save frame pointer
			move $fp, $sp       # Set frame pointer
		 */

		// Save stack
		text += LINE_INDENTATION + writeComment("Save stack, return and frame pointer (from previous call).") + LINE_SEPARATOR + LINE_INDENTATION +
				("addi " + STACK_POINTER + ", " + STACK_POINTER + ", -8") + LINE_SEPARATOR + LINE_INDENTATION +
				("sw " + RETURN_REGISTER + ", 4(" + STACK_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("sw " + FRAME_POINTER + ", 0(" + STACK_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("move " + FRAME_POINTER + ", " + STACK_POINTER) + LINE_SEPARATOR;

		return text + LINE_SEPARATOR;
	}

	private String beginFunction(String functionSize) {
		/*if (functionLabel.equals(MAIN_FUNCTION)) {
			return functionLabel + ":" + LINE_SEPARATOR +                    										// function start
					LINE_INDENTATION + "move" + FRAME_POINTER + ", " + STACK_POINTER + LINE_SEPARATOR +				// move $fp, $sp
					LINE_INDENTATION +  "sub " + STACK_POINTER + ", " + STACK_POINTER + ", " + functionSize + "\n";	// sub $sp, $sp, size
		}
		else {
			return functionLabel + ":" + LINE_SEPARATOR;
		}*/
		return 	LINE_INDENTATION + writeComment("Allocate function's memory (in Bytes)") + LINE_SEPARATOR + LINE_INDENTATION +
				("sub " + STACK_POINTER + ", " + STACK_POINTER + ", -" + functionSize) + " " + LINE_SEPARATOR + LINE_SEPARATOR;
	}

	private String returnFunction(String returnValue) {
		String text = LINE_INDENTATION + writeComment("Function return's value") + LINE_SEPARATOR + LINE_INDENTATION;

		// Check if the return value is a symbol in the scope.
		Symbol<?> returnSymbol = symbolTable.findSymbolInsideFunction(returnValue, currentFunctionName);
		if (returnSymbol != null && returnSymbol.isVariable()) {
			text += ("li " + FUNCTION_RESULT_REGISTER + ", " + returnSymbol.getOffset() + "(" + FRAME_POINTER + ")");
		}
		else {
			text += ("li " + FUNCTION_RESULT_REGISTER + ", " + returnValue);
		}

		return text + LINE_SEPARATOR + LINE_SEPARATOR;
	}

	private String endFunction() {
		/*
			move $sp, $fp       # Restore stack pointer
			lw $ra, 4($sp)      # Restore return address
			lw $fp, 0($sp)      # Restore frame pointer
			addi $sp, $sp, 8    # Deallocate stack frame
			jr $ra              # Return from function
		 */

		String text = LINE_INDENTATION + writeComment("End of function - Restore stack, return and frame pointer") + LINE_SEPARATOR + LINE_INDENTATION +
				("move " + STACK_POINTER + ", " + FRAME_POINTER) + LINE_SEPARATOR + LINE_INDENTATION +
				("lw " + RETURN_REGISTER + ", 4(" + STACK_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("lw " + FRAME_POINTER + ", 0(" + STACK_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("addi " + STACK_POINTER + ", " + STACK_POINTER + ", 8") + LINE_SEPARATOR + LINE_INDENTATION;

		// End the program if it's the main or add the return value if it's another function.
		if (currentFunctionName.equals(MAIN_FUNCTION)) {
			text += writeComment("End of the main") + LINE_SEPARATOR + LINE_INDENTATION +
					("li " + FUNCTION_RESULT_REGISTER + ", 10") + LINE_SEPARATOR + LINE_INDENTATION +
					(END_PROGRAM_INSTRUCTION);
		}
		else {
			text += ("jr " + RETURN_REGISTER);
		}

		return text + LINE_SEPARATOR + LINE_SEPARATOR;
	}
}
