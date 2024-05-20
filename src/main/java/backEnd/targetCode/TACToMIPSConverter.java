package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import backEnd.targetCode.operations.AssignmentOperations;
import backEnd.targetCode.operations.FunctionOperations;
import backEnd.targetCode.registers.RegisterAllocator;
import backEnd.targetCode.registers.RegisterAllocatorInteger;
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

	public TACToMIPSConverter(SymbolTableInterface symbolTable, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat) {
		assignmentOperations = new AssignmentOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat);
		functionOperations = new FunctionOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat, assignmentOperations);
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
				//targetCode.write(convertTACInstruction(instruction) ? "")
				String instructionCode = convertTACInstruction(instruction);
				if (instructionCode != null) {
					targetCode.write(instructionCode);
				}
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
			case "PushParam" -> functionOperations.assignFunctionParameter(instruction.getOperand1());
			case "EndFunc" -> functionOperations.endFunction();
			case "LCall" -> functionOperations.callFunction(instruction.getOperand1());

			// ** Assignments
			case "=" -> showOperation(instruction, assignmentOperations.assignValue(instruction.getOperand1(), instruction.getResult()));

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
			case "SUM", "SUB", "MUL", "DIV" -> showOperation(instruction, assignmentOperations.addPendingOperation(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult(), instruction.getOperator()));

			default -> null;
		};
	}

	private String showOperation(TACInstruction instruction, String codeMIPS) {
		return 	LINE_SEPARATOR + LINE_INDENTATION +
				assignmentOperations.writeComment("TAC: " + instruction.toString()) + LINE_SEPARATOR +
				((codeMIPS == null) ? (LINE_INDENTATION + "# Store the temporary variable into a pending list"): codeMIPS) + LINE_SEPARATOR;
	}
}
