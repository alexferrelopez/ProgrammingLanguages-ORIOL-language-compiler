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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecursiveDescentLLParserTest {

    private final static String TEST_FILE_FOLDER = "src/test/resources/";
    private final static String ASSIGNMENTS_FILE_FOLDER = TEST_FILE_FOLDER + "assignments/";
    private final static String DECLARATIONS_FILE_FOLDER = TEST_FILE_FOLDER + "declarations/";
    private final static String FOR_LOOPS_FILE_FOLDER = TEST_FILE_FOLDER + "loops/";

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

    // ** DECLARATIONS ** //
    @Test
    @DisplayName("Check general declarations.")
    @Description("Test that checks if the general declarations are accepted syntactically.")
    public void test_declarations() {
        setupCompiler(DECLARATIONS_FILE_FOLDER + "ExempleDeclaracions.farm");
        compileCode();
    }

    // Logical code
    @Test
    @DisplayName("Check logical declarations.")
    @Description("Test that checks if the logical declarations are accepted syntactically.")
    public void test_logicalDeclarations() {
        setupCompiler(DECLARATIONS_FILE_FOLDER + "ExempleDeclaracionsLògiques.farm");
        compileCode();
    }

    // Relational code
    @Test
    @DisplayName("Check relational declarations.")
    @Description("Test that checks if the relational declarations are accepted syntactically.")
    public void test_relationalDeclarations() {
        setupCompiler(DECLARATIONS_FILE_FOLDER + "ExempleDeclaracionsRelacionals.farm");
        compileCode();
    }

    // Arithmetic code
    @Test
    @DisplayName("Check arithmetic declarations.")
    @Description("Test that checks if the arithmetic declarations are accepted syntactically.")
    public void test_arithmeticDeclarations() {
        setupCompiler(DECLARATIONS_FILE_FOLDER + "ExempleDeclaracionsAritmètiques.farm");
        compileCode();
    }


    // ** ASSIGNMENTS ** //
    @Test
    @DisplayName("Check general assignments.")
    @Description("Test that checks if the general assignments are accepted syntactically.")
    public void test_assignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacions.farm");
        compileCode();
    }

    // Logical code
    @Test
    @DisplayName("Check logical assignments.")
    @Description("Test that checks if the logical assignments are accepted syntactically.")
    public void test_logicalAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsLògiques.farm");
        compileCode();
    }

    // Relational code
    @Test
    @DisplayName("Check relational assignments.")
    @Description("Test that checks if the relational assignments are accepted syntactically.")
    public void test_relationalAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsRelacionals.farm");
        compileCode();
    }

    // Arithmetic code
    @Test
    @DisplayName("Check arithmetic assignments.")
    @Description("Test that checks if the arithmetic assignments are accepted syntactically.")
    public void test_arithmeticAssignments() {
        setupCompiler(ASSIGNMENTS_FILE_FOLDER + "ExempleAssignacionsAritmètiques.farm");
        compileCode();
    }

    // ** CONDITIONALS ** //
    @Test
    @DisplayName("Check conditionals.")
    @Description("Test that checks if the for loops are accepted syntactically.")
    public void test_conditionals() {
        setupCompiler(FOR_LOOPS_FILE_FOLDER + "loops/ExempleFor.farm");
        compileCode();
    }

    // ** FOR LOOP ** //
    @Test
    @DisplayName("Check for loops.")
    @Description("Test that checks if the for loops are accepted syntactically.")
    public void test_forLoops() {
        setupCompiler(FOR_LOOPS_FILE_FOLDER + "loops/ExempleFor.farm");
        compileCode();
    }

    // ** WHILE LOOP ** //
    @Test
    @DisplayName("Check while loops.")
    @Description("Test that checks if the for loops are accepted syntactically.")
    public void test_whileLoops() {
        setupCompiler(FOR_LOOPS_FILE_FOLDER + "ExempleWhile.farm");
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