package backEnd.targetCode;

import frontEnd.semantics.symbolTable.SymbolTableInterface;

public class MIPSOperations {
	protected static final String LINE_SEPARATOR = System.lineSeparator();
	protected static final String LINE_INDENTATION = "\t";
	protected static final String COMMENT_WORD = "#";

	protected static final String FRAME_POINTER = "$fp";
	protected static final String STACK_POINTER = "$sp";
	protected static final String RETURN_REGISTER = "$ra";
	protected static final String END_PROGRAM_INSTRUCTION = "syscall";
	protected static final String FUNCTION_RESULT_REGISTER = "$v0";

	protected final SymbolTableInterface symbolTable;
	protected final static String MAIN_FUNCTION = "ranch";
	protected static String currentFunctionName;
	protected static RegisterAllocator registerAllocator;

	public MIPSOperations(SymbolTableInterface symbolTableInterface) {
		this.symbolTable = symbolTableInterface;
	}

	protected String writeComment(String comment) {
		return (COMMENT_WORD + " " + comment);
	}
}
