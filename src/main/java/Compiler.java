import errorHandlers.AbstractErrorHandler;
import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.sintaxis.RecursiveDescentLLParser;
import frontEnd.sintaxis.SyntacticAnalyzerInterface;

import java.util.ArrayList;
import java.util.List;

public class Compiler implements CompilerInterface {
	private final LexicalAnalyzerInterface scanner;
	private final SyntacticAnalyzerInterface parser;
	private final List<AbstractErrorHandler<?, ?>> errorHandler;

	public Compiler(String codeFilePath) {
		// ---- FRONT END ---- //

		// Error Handlers
		LexicalErrorHandler lexicalErrorHandler = new LexicalErrorHandler();
		SyntacticErrorHandler syntacticErrorHandler = new SyntacticErrorHandler();
		SemanticErrorHandler semanticErrorHandler = new SemanticErrorHandler();

		this.errorHandler = new ArrayList<>();
		this.errorHandler.add(lexicalErrorHandler);
		this.errorHandler.add(lexicalErrorHandler);
		this.errorHandler.add(syntacticErrorHandler);
		this.errorHandler.add(semanticErrorHandler);

		// Code Analysis
		this.scanner = new LexicalAnalyzer(codeFilePath, new LexicalErrorHandler());
		this.parser = new RecursiveDescentLLParser(scanner, syntacticErrorHandler);

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
