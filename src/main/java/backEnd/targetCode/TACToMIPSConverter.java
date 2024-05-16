package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import backEnd.targetCode.operations.AssignmentOperations;
import backEnd.targetCode.operations.FunctionOperations;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.semantics.symbolTable.SymbolTableInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static backEnd.targetCode.MIPSOperations.LINE_INDENTATION;
import static backEnd.targetCode.MIPSOperations.LINE_SEPARATOR;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
	private static final String TARGET_FILE = "target/farm.asm";
	private BufferedWriter targetCode;
	private final FunctionOperations functionOperations;
	private final AssignmentOperations assignmentOperations;

	public TACToMIPSConverter(SymbolTableInterface symbolTable, RegisterAllocator registerAllocator) {
		functionOperations = new FunctionOperations(symbolTable, registerAllocator);
		assignmentOperations = new AssignmentOperations(symbolTable, registerAllocator);
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
				targetCode.write(convertTACInstruction(instruction));
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

	private String convertTACInstruction(TACInstruction instruction) throws IOException {
		return switch (instruction.getOperator()) {
			// ** Functions ** //
			case "function" -> functionOperations.funcDeclaration(instruction.getResult());
			case "Return" -> functionOperations.returnFunction(instruction.getOperand1());
			case "BeginFunc" -> functionOperations.beginFunction(instruction.getOperand1());
			case "EndFunc" -> functionOperations.endFunction();

			// ** Assignments
			case "=" -> showOperation(instruction, assignmentOperations.assignValue(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult()));

			// *** Binary Operations ***
			case "GT" -> null;
				// Greater than
			case "LT" -> null;
				// Less than
			case "EQ" -> null;
				// Equal
			case "NEQ" -> null;
				// Not equal
			case "OR" -> null;
				// Or
			case "AND" -> null;
				// And

			// *** Conditional ***
			case "IFz" -> null;
				// If zero

			// *** Arithmetic Operations ***

			case "SUM" -> showOperation(instruction, assignmentOperations.sumAssignment(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult()));
			case "SUB" -> showOperation(instruction, assignmentOperations.subtractAssignment(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult()));
			case "MOD" -> null;
			case "MUL" -> null;
			case "POW" -> null;
			case "DIV" -> null;
			default -> throw new IllegalStateException("Unexpected value: " + instruction.getOperator());
		};
	}

	private String showOperation(TACInstruction instruction, String codeMIPS) {
		return 	LINE_SEPARATOR + LINE_INDENTATION +
				assignmentOperations.writeComment(instruction.toString()) + LINE_SEPARATOR +
				codeMIPS;
	}
}
