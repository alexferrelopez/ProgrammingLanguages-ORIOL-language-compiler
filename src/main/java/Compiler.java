import backEnd.targetCode.TACToMIPSConverter;
import backEnd.targetCode.TACToMIPSConverterInterface;
import errorHandlers.AbstractErrorHandler;
import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import errorHandlers.errorTypes.ErrorType;
import errorHandlers.warningTypes.WarningType;
import frontEnd.exceptions.InvalidFileException;
import frontEnd.intermediateCode.TACGenerator;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.intermediateCode.TACModule;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.semantics.SemanticAnalyzer;
import frontEnd.semantics.SemanticAnalyzerInterface;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.sintaxis.RecursiveDescentLLParser;
import frontEnd.sintaxis.SyntacticAnalyzerInterface;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;


import java.util.ArrayList;
import java.util.List;

public class Compiler implements CompilerInterface {
    private final LexicalAnalyzerInterface scanner;
    private final SyntacticAnalyzerInterface parser;
    private TACGenerator tacGenerator;
    private final TACToMIPSConverterInterface mipsConverter;
    private final List<AbstractErrorHandler<? extends ErrorType, ? extends WarningType>> errorHandlerList;
    private final SymbolTableInterface symbolTable;
    private final SemanticAnalyzerInterface semanticAnalyzer;

    public Compiler(String codeFilePath) {
        // ---- FRONT END ---- //

        // *** Error Handlers ***
        LexicalErrorHandler lexicalErrorHandler = new LexicalErrorHandler();
        SyntacticErrorHandler syntacticErrorHandler = new SyntacticErrorHandler();
        SemanticErrorHandler semanticErrorHandler = new SemanticErrorHandler();

        this.errorHandlerList = new ArrayList<>();
        this.errorHandlerList.add(lexicalErrorHandler);
        this.errorHandlerList.add(syntacticErrorHandler);
        this.errorHandlerList.add(semanticErrorHandler);

        // *** Code Analysis ***
        this.scanner = new LexicalAnalyzer(codeFilePath, lexicalErrorHandler);
        this.symbolTable = new SymbolTableTree();
        this.semanticAnalyzer = new SemanticAnalyzer(semanticErrorHandler, symbolTable);
        this.parser = new RecursiveDescentLLParser(scanner, syntacticErrorHandler, semanticAnalyzer);

        // ---- BACK END ---- //
        this.mipsConverter = new TACToMIPSConverter();
    }

    /**
     * This method starts the lexical, syntactic and semantic analysis of the code. Generates the intermediate code.
     */
    @Override
    public void compileCode() {
        parser.parseProgram();

        // *** Intermediate Code *** //
        Tree<AbstractSymbol> tree = parser.getTree();    // Get tree from parser

        // Print the tree for debugging
        parser.printTree(tree);

        TACModule tacModule = new TACModule();
        tacGenerator = new TACGenerator(tacModule, symbolTable);

        // Generate the intermediate code
        List<TACInstruction> TACinstructions = tacGenerator.generateTAC(tree);

        tacGenerator.printTAC();

        // ---- BACK END ---- //
		try {
			mipsConverter.generateMIPS(TACinstructions);
		} catch (InvalidFileException e) {
            System.out.println(e.getMessage());
		}
	}

    /**
     * This method returns if the code has errors or not (checks the lexical, syntactic and semantic errors).
     *
     * @return True if the code has errors, false otherwise.
     */
    @Override
    public boolean hasErrors() {
        for (AbstractErrorHandler<? extends ErrorType, ? extends WarningType> abstractErrorHandler : errorHandlerList) {
            if (abstractErrorHandler.hasErrors()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasWarnings() {
        for (AbstractErrorHandler<? extends ErrorType, ? extends WarningType> abstractErrorHandler : errorHandlerList) {
            if (abstractErrorHandler.hasWarnings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void printErrors() {
        for (AbstractErrorHandler<? extends ErrorType, ? extends WarningType> abstractErrorHandler : errorHandlerList) {
            abstractErrorHandler.printErrors();
        }
    }

    @Override
    public void printWarnings() {
        for (AbstractErrorHandler<? extends ErrorType, ? extends WarningType> abstractErrorHandler : errorHandlerList) {
            abstractErrorHandler.printWarnings();
        }
    }
}
