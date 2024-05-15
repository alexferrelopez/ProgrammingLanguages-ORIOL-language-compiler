package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import backEnd.targetCode.operations.AssignmentOperations;
import backEnd.targetCode.operations.FunctionOperations;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.lexic.dictionary.tokenEnums.ValueSymbol;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
	private static final String TARGET_FILE = "target/farm.asm";
	private BufferedWriter targetCode;
	private final FunctionOperations functionOperations;
	private final AssignmentOperations assignmentOperations;

	public TACToMIPSConverter(SymbolTableInterface symbolTable, RegisterAllocator registerAllocator) {
		functionOperations = new FunctionOperations(symbolTable);
		assignmentOperations = new AssignmentOperations(symbolTable);
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
				targetCode.write(functionOperations.funcDeclaration(instruction.getResult()));
				break;
			case "Return":
				targetCode.write(functionOperations.returnFunction(instruction.getOperand1()));
			case "=":
				// Assignment
				// mipsCode.append(assign(instruction.getResult(), instruction.getOperand1()));
				targetCode.write(assignmentOperations.assignValue(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult()));
				break;
			case "BeginFunc":
				// Begin Function
				targetCode.write(functionOperations.beginFunction(instruction.getOperand1()));
				break;
			case "EndFunc":
				// End Function
				targetCode.write(functionOperations.endFunction());
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
}
