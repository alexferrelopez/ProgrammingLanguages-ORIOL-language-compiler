package frontEnd.sintaxis;

import errorHandlers.LexicalErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.exceptions.InvalidFileException;
import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.tokenEnums.*;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

class RecursiveDescentLLParserTest {

    private final static String TEST_FILE_FOLDER = "src/test/resources/";

    private RecursiveDescentLLParser parser;
    private LexicalAnalyzerInterface lexicalAnalyzer;
    private SyntacticErrorHandler errorHandler;

    private void setupCompiler(String filePath) {
        errorHandler = new SyntacticErrorHandler();
        lexicalAnalyzer = new LexicalAnalyzer(filePath, new LexicalErrorHandler());
        parser = new RecursiveDescentLLParser(lexicalAnalyzer, errorHandler);
    }

    @Test
    @DisplayName("Check file non-existence handling.")
    @Description("Test that checks if a file that does not exist is handled properly.")
    public void test_assignments() {
        setupCompiler(TEST_FILE_FOLDER + "ExempleAssignacions.farm");

        // Start compilation
        parser.parseProgram();

        // Asserts that the operation throws the specified exception
        Assertions.assertThrows(InvalidFileException.class, lexicalAnalyzer::startLexicalAnalysis, "LexicalAnalyzer should throw InvalidFileException for non-existent files.");
    }
}