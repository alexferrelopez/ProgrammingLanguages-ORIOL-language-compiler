package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;
import frontEnd.Tree;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler) {
        errorHandler = semanticErrorHandler;
    }

    public static void sendTree(Tree tree) {
        //TODO implement this method
    }
}
