import frontend.lexic.LexicalAnalyzer;
import frontend.lexic.LexicalAnalyzerInterface;
import frontend.sintaxis.RecursiveDescentLLParser;
import frontend.sintaxis.SyntacticAnalyzerInterface;

public class Compiler implements CompilerInterface {
	private final LexicalAnalyzerInterface scanner;
	private final SyntacticAnalyzerInterface parser;

	public Compiler(String codeFilePath) {
		// ---- FRONT END ---- //
		this.scanner = new LexicalAnalyzer(codeFilePath);
		this.parser = new RecursiveDescentLLParser(scanner);

		// ---- BACK END ---- //
	}

	@Override
	public void compileCode() {
		parser.parseProgram();
	}

	@Override
	public boolean hasErrors() {
		return false;
	}
}
