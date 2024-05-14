package frontEnd.sintaxis;

import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.semantics.SemanticAnalyzer;
import frontEnd.semantics.SemanticAnalyzerInterface;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;

class RecursiveDescentLLParserTest {

    private final static String TEST_FILE_FOLDER = "src/test/resources/";
    private final static String ASSIGNMENTS_FILE_FOLDER = TEST_FILE_FOLDER + "assignments/";

    private RecursiveDescentLLParser parser;
    private LexicalAnalyzerInterface lexicalAnalyzer;
    private SyntacticErrorHandler errorHandler;
    private SemanticAnalyzerInterface semanticAnalyzer;
    private SemanticErrorHandler semanticErrorHandler;
    private SymbolTableInterface symbolTable;

    private void setupCompiler(String filePath) {
        errorHandler = new SyntacticErrorHandler();
        lexicalAnalyzer = new LexicalAnalyzer(filePath, new LexicalErrorHandler());
        semanticErrorHandler = new SemanticErrorHandler();
        symbolTable = new SymbolTableTree();
        semanticAnalyzer = new SemanticAnalyzer(semanticErrorHandler, symbolTable);
        parser = new RecursiveDescentLLParser(lexicalAnalyzer, errorHandler, semanticAnalyzer);
    }

    private void compileCode() {
        // Start compilation
        parser.parseProgram();

        // Asserts that there are no errors in the syntactical analysis
        Assertions.assertFalse(errorHandler.hasErrors());
    }

    // ** GENERAL CODE ** //
    @Test
    @DisplayName("Check general assignments.")
    @Description("Test that checks if the general assignments are accepted syntactically.")
    public void test_assignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacions.farm");
        compileCode();
    }

    // ** LOGICAL CODE ** //
    @Test
    @DisplayName("Check logical assignments.")
    @Description("Test that checks if the logical assignments are accepted syntactically.")
    public void test_logicalAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsLògiques.farm");
        compileCode();
    }

    // ** RELATIONAL CODE ** //
    @Test
    @DisplayName("Check relational assignments.")
    @Description("Test that checks if the relational assignments are accepted syntactically.")
    public void test_relationalAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsRelacionals.farm");
        compileCode();
    }

    // ** ARITHMETIC CODE ** //
    @Test
    @DisplayName("Check arithmetic assignments.")
    @Description("Test that checks if the arithmetic assignments are accepted syntactically.")
    public void test_arithmeticAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsAritmètiques.farm");
        compileCode();
    }

    // ** FIBONACCI CODE ** //
    @Test
    @DisplayName("Check Fibonacci code.")
    @Description("Test that checks if the fibonacci is accepted syntactically.")
    public void test_fibonacci() {
        setupCompiler(TEST_FILE_FOLDER + "ExempleFibonacci.farm");
        compileCode();
    }
}