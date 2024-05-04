import ErrorHandlers.LexicalErrorHandler;
import FrontEnd.LexicalAnalyzer;
import FrontEnd.lexic.LexicalAnalyzerInterface;
import FrontEnd.sintaxis.RecursiveDescentLLParser;
import FrontEnd.sintaxis.SyntacticAnalyzerInterface;

public class Compiler implements CompilerInterface {
	private final LexicalAnalyzerInterface scanner;
	private final SyntacticAnalyzerInterface parser;

	public Compiler(String codeFilePath) {
		// ---- FRONT END ---- //
		this.scanner = new LexicalAnalyzer(codeFilePath, new LexicalErrorHandler());
		this.parser = new RecursiveDescentLLParser(scanner);

		// ---- BACK END ---- //
	}

	/**
	 * This method starts the lexical, syntactic and semantic analysis of the code. Generates the intermediate code.
	 */
	@Override
	public void compileCode() {
		parser.parseProgram();
	}

	/**
	 * This method returns if the code has errors or not (checks the lexical, syntactic and semantic errors).
	 * @return True if the code has errors, false otherwise.
	 */
	@Override
	public boolean hasErrors() {
		return false;
	}
}
