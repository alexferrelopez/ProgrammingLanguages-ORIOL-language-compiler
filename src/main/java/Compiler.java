import errorHandlers.AbstractErrorHandler;
import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import errorHandlers.errorTypes.ErrorType;
import errorHandlers.warningTypes.WarningType;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.sintaxis.RecursiveDescentLLParser;
import frontEnd.sintaxis.SyntacticAnalyzerInterface;

import java.util.ArrayList;
import java.util.List;

public class Compiler implements CompilerInterface {
    private final LexicalAnalyzerInterface scanner;
    private final SyntacticAnalyzerInterface parser;
    private final List<AbstractErrorHandler<? extends ErrorType, ? extends WarningType>> errorHandlerList;

    public Compiler(String codeFilePath) {
        // ---- FRONT END ---- //

        // Error Handlers
        LexicalErrorHandler lexicalErrorHandler = new LexicalErrorHandler();
        SyntacticErrorHandler syntacticErrorHandler = new SyntacticErrorHandler();
        SemanticErrorHandler semanticErrorHandler = new SemanticErrorHandler();

        this.errorHandlerList = new ArrayList<>();
        this.errorHandlerList.add(lexicalErrorHandler);
        this.errorHandlerList.add(syntacticErrorHandler);
        this.errorHandlerList.add(semanticErrorHandler);

        // Code Analysis
        this.scanner = new LexicalAnalyzer(codeFilePath, lexicalErrorHandler);
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
