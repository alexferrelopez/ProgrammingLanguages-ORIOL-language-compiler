package FrontEnd;

import FrontEnd.Exceptions.InvalidFileException;

public class RecursiveDescentLLParser {
	private final LexicalAnalyzer lexicalAnalyzer;

	public RecursiveDescentLLParser(LexicalAnalyzer lexicalAnalyzer) {
		this.lexicalAnalyzer = lexicalAnalyzer;
	}

	public void startCodeAnalysis() {
		try {
			lexicalAnalyzer.readCodeFile();
		} catch (InvalidFileException e) {
			System.out.println(e.getMessage());
		}
	}
}
