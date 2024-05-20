package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;

import java.util.ArrayList;
import java.util.List;

public class FunctionOperations extends MIPSOperations {
	private final AssignmentOperations assignmentOperations;

	private final static int MAX_FUNCTION_PARAMETERS = 4;	// Only $a0 to $3 parameters are available.
	private final static String PARAMETERS_REGISTER_PREFIX = "$a";	// Only $a0 to $3 parameters are available.
	private int currentParameterNumber = 0;
	private final static String FUNCTION_PUSH_PARAMETER_OPERATOR = "PushParam";

	public FunctionOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat, AssignmentOperations assignmentOperations) {
		super(symbolTableInterface, registerAllocatorInteger, registerAllocatorFloat);
		this.assignmentOperations = assignmentOperations;
	}

	public String funcDeclaration(String functionLabel) {
		currentFunctionName.push(functionLabel);

		String text = writeComment("Start of function " + functionLabel) + LINE_SEPARATOR +
				(functionLabel + ":") + LINE_SEPARATOR;
		/*
			sw $fp, 0($sp)      # Save previous (called function) frame pointer
			move $fp, $sp       # Set frame pointer ($fp = $sp)
			sw $ra, -4($fp)     # Save return address
			subi $sp, $sp, 8   	# Allocate stack frame
		 */

		// Save stack
		text += LINE_INDENTATION + writeComment("Save stack, return and frame pointer (from previous call).") + LINE_SEPARATOR + LINE_INDENTATION +
				("sw " + FRAME_POINTER + ", 0(" + STACK_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("move " + FRAME_POINTER + ", " + STACK_POINTER) + LINE_SEPARATOR + LINE_INDENTATION +
				("sw " + RETURN_VALUE_REGISTER + ", -4(" + FRAME_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("subi " + STACK_POINTER + ", " + STACK_POINTER + ", 8") + LINE_SEPARATOR + LINE_INDENTATION;

		return text + LINE_SEPARATOR;
	}

	private long assignOffset(ScopeNode scope, long currentOffset) {
		// Search all the variables in the current scope.
		List<Symbol<?>> variables = new ArrayList<>(scope.getSymbols().values());
		for (Symbol<?> variable : variables) {
			VariableSymbol<?> variableSymbol = (VariableSymbol<?>) variable;

			// Assign an offset only if it's not a parameters (they already have an offset assigned previously).
			if (variableSymbol.isVariable() && !variableSymbol.isFunctionParameter()) {
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

	private long assignParametersOffset(long currentOffset, String functionName) {
		Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(functionName);
		if (functionSymbol != null && !functionSymbol.isVariable()) {
			FunctionSymbol<?> function = (FunctionSymbol<?>) functionSymbol;

			// Loop through all the parameters (already ordered).
			for (VariableSymbol<?> parameter : function.getParameters()) {
				parameter.setOffset(currentOffset);
				currentOffset -= parameter.getSizeInBytes();
			}
		}

		return currentOffset;
	}

	public String beginFunction(String functionSize) {

		// Set the offset for each variable in the function (in all the nested scopes).
		long currentOffset = 0;
		currentOffset = assignParametersOffset(currentOffset, currentFunctionName.peek());
		ScopeNode function = symbolTable.getFunctionScope(currentFunctionName.peek());
		assignOffset(function, currentOffset);

		return 	LINE_INDENTATION + writeComment("Allocate function's memory (in Bytes)") + LINE_SEPARATOR + LINE_INDENTATION +
				("sub " + STACK_POINTER + ", " + STACK_POINTER + ", -" + functionSize) + " " + LINE_SEPARATOR + LINE_SEPARATOR +
				LINE_INDENTATION + writeComment("-- Variables code --") + LINE_SEPARATOR;
	}

	public String returnFunction(String returnValue) {
		String text = LINE_SEPARATOR + LINE_INDENTATION + writeComment("Function return's value") + LINE_SEPARATOR + LINE_INDENTATION;

		// Check if the return value is a symbol in the scope.
		Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(currentFunctionName.peek());
		Symbol<?> variableSymbol = symbolTable.findSymbolInsideFunction(returnValue, currentFunctionName.peek());

		String destinationRegister = returnValue;
		boolean isLiteral = true;
		if (variableSymbol != null && variableSymbol.isVariable()) {
			isLiteral = false;

			RegisterAllocator registerAllocator;
			if (functionSymbol.getDataType() == DataType.FLOAT) {
				registerAllocator = registerAllocatorFloat;
			}
			else {
				registerAllocator = registerAllocatorInteger;
			}

			Operand operand = new Operand(true, functionSymbol.getDataType(), variableSymbol.getOffset() + "(" + FRAME_POINTER + ")", false);
			Register variableRegister = registerAllocator.allocateRegister(operand);
			destinationRegister = variableRegister.getNotNullRegister();
		}

		return text + loadVariableToRegister(destinationRegister, RETURN_VALUE_REGISTER, functionSymbol.getDataType(), isLiteral) + LINE_SEPARATOR;
	}

	public String endFunction() {
		/*
			move $sp, $fp       # Restore stack pointer ($sp = $fp) - All current function's memory is overwritten
			lw $ra, -4($fp)     # Restore return address ($ra = -4($fp))
			lw $fp, 0($fp)      # Restore frame pointer ($fp = previos $fp).
			jr $ra              # Return from function
		 */

		registerAllocatorInteger.freeRegister("test");

		String text = LINE_SEPARATOR + LINE_INDENTATION + writeComment("End of function - Restore stack, return and frame pointer") + LINE_SEPARATOR + LINE_INDENTATION +
				("move " + STACK_POINTER + ", " + FRAME_POINTER) + LINE_SEPARATOR + LINE_INDENTATION +
				("lw " + RETURN_VALUE_REGISTER + ", -4(" + FRAME_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION +
				("lw " + FRAME_POINTER + ", 0(" + FRAME_POINTER + ")") + LINE_SEPARATOR + LINE_INDENTATION;

		// End the program if it's the main or add the return value if it's another function.
		if (currentFunctionName.peek().equals(MAIN_FUNCTION)) {
			text += writeComment("End of the main") + LINE_SEPARATOR + LINE_INDENTATION +
					("li " + FUNCTION_RESULT_REGISTER + ", 10") + LINE_SEPARATOR + LINE_INDENTATION +
					(END_PROGRAM_INSTRUCTION);
		}
		else {
			text += ("jr " + RETURN_ADDRESS_REGISTER);
		}

		// Leave the current function.
		currentFunctionName.pop();

		return text + LINE_SEPARATOR + LINE_SEPARATOR;
	}

	public String assignFunctionParameter(String parameterValue, String callOperator) {
		String destinationRegister = PARAMETERS_REGISTER_PREFIX + currentParameterNumber;
		currentParameterNumber++;

//		if (currentParameterNumber > MAX_FUNCTION_PARAMETERS) {
//
//		}

		// Save the parameter to see its type when the "call" instruction is received.
		OperandContainer pushFunctionParameter = new OperandContainer();
		loadOperands(pushFunctionParameter, destinationRegister, parameterValue, null, callOperator, false);
		Operand parameter = new Operand(true, null, parameterValue, false);
		pushFunctionParameter.setOperand1(parameter);
		this.pendingOperations.add(pushFunctionParameter);

		// The assignment internally checks if it's a variable or a normal value.
		return null;
	}

	public String callFunction(String functionName) {
		StringBuilder text = new StringBuilder();
		currentFunctionName.push(functionName);	// Update the new current function.

		int numParameter = 0;
		// Do all the previous operations.
		for (OperandContainer operation : this.pendingOperations) {

			// Make an assignment for the operators to be pushed into the function called.
			if (operation.getOperator().equals(FUNCTION_PUSH_PARAMETER_OPERATOR)) {

				// Get the current parameter to see the expected type.
				Symbol<?> function = symbolTable.findSymbolGlobally(currentFunctionName.peek());
				VariableSymbol<?> parameter = ((FunctionSymbol<?>) function).getParameters().get(numParameter);

				// Assign the value to an arguments' register.
				text.append(assignmentOperations.assignValueToRegister(operation.getOperand1().getValue(), operation.getDestination().getValue(), parameter.getDataType(), false));
				numParameter++;
			}
		}

		this.currentParameterNumber = 0;
		currentFunctionName.pop();	// Leave the new current function.
		this.pendingOperations.clear();

		return text.append(LINE_INDENTATION).append("jal ").append(functionName).append(LINE_SEPARATOR).toString();
	}
}
