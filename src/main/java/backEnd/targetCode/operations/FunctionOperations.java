package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.RegisterAllocator;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class FunctionOperations extends MIPSOperations {
	public FunctionOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocator) {
		super(symbolTableInterface, registerAllocator);
	}

	public String funcDeclaration(String functionLabel) {
		currentFunctionName = functionLabel;

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

	private long assignOffset(ScopeNode scope, long currentOffset) {
		// Search all the variables in the current scope.
		List<Symbol<?>> variables = new ArrayList<>(scope.getSymbols().values());
		for (Symbol<?> variable : variables) {
			if (variable.isVariable()) {
				variable.setOffset(currentOffset);
				currentOffset -= variable.getSizeInBytes();
			}
		}

		// Do the same for all the nested scopes.
		for (ScopeNode child : scope.getChildren()) {
			currentOffset = assignOffset(child, currentOffset);
		}

		return currentOffset;
	}

	public String beginFunction(String functionSize) {
		/*if (functionLabel.equals(MAIN_FUNCTION)) {
			return functionLabel + ":" + LINE_SEPARATOR +                    										// function start
					LINE_INDENTATION + "move" + FRAME_POINTER + ", " + STACK_POINTER + LINE_SEPARATOR +				// move $fp, $sp
					LINE_INDENTATION +  "sub " + STACK_POINTER + ", " + STACK_POINTER + ", " + functionSize + "\n";	// sub $sp, $sp, size
		}
		else {
			return functionLabel + ":" + LINE_SEPARATOR;
		}*/

		// Set the offset for each variable in the function (in all the nested scopes).
		long currentOffset = 0;
		ScopeNode function = symbolTable.getFunctionScope(currentFunctionName);
		assignOffset(function, currentOffset);

		return 	LINE_INDENTATION + writeComment("Allocate function's memory (in Bytes)") + LINE_SEPARATOR + LINE_INDENTATION +
				("sub " + STACK_POINTER + ", " + STACK_POINTER + ", -" + functionSize) + " " + LINE_SEPARATOR + LINE_SEPARATOR +
				LINE_INDENTATION + writeComment("-- Variables code --") + LINE_SEPARATOR;
	}

	public String returnFunction(String returnValue) {
		String text = LINE_SEPARATOR + LINE_INDENTATION + writeComment("Function return's value") + LINE_SEPARATOR + LINE_INDENTATION;

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

	public String endFunction() {
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
