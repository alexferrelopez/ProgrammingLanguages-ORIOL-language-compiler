import errorHandlers.AbstractErrorHandler;
import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.semantics.SemanticAnalyzer;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.sintaxis.RecursiveDescentLLParser;
import frontEnd.sintaxis.SyntacticAnalyzerInterface;

import java.util.ArrayList;
import java.util.List;

public class Compiler implements CompilerInterface {
    private final LexicalAnalyzerInterface scanner;
    private final SyntacticAnalyzerInterface parser;
    private final List<AbstractErrorHandler<?, ?>> errorHandlerList;

    public Compiler(String codeFilePath) {
        // ---- FRONT END ---- //

        // Error Handlers
        LexicalErrorHandler lexicalErrorHandler = new LexicalErrorHandler();
        SyntacticErrorHandler syntacticErrorHandler = new SyntacticErrorHandler();
        SemanticErrorHandler semanticErrorHandler = new SemanticErrorHandler();
        SymbolTableTree symbolTable = new SymbolTableTree();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(semanticErrorHandler, symbolTable);

        this.errorHandlerList = new ArrayList<>();
        this.errorHandlerList.add(lexicalErrorHandler);
        this.errorHandlerList.add(syntacticErrorHandler);
        this.errorHandlerList.add(semanticErrorHandler);

        // Code Analysis
        this.scanner = new LexicalAnalyzer(codeFilePath, lexicalErrorHandler);
        this.parser = new RecursiveDescentLLParser(scanner, syntacticErrorHandler, semanticAnalyzer);

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
        for (AbstractErrorHandler<?, ?> abstractErrorHandler : errorHandlerList) {
            if (abstractErrorHandler.hasErrors()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void printErrors() {
        for (AbstractErrorHandler<?, ?> abstractErrorHandler : errorHandlerList) {
            abstractErrorHandler.printErrors();
        }
    }
}
