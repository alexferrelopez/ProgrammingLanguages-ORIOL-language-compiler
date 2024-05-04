package frontEnd.semantics;

import errorHandlers.SemanticErrorHandler;

public class SemanticAnalyzer {
    private final SemanticErrorHandler errorHandler;

    public SemanticAnalyzer(SemanticErrorHandler semanticErrorHandler) {
        errorHandler = semanticErrorHandler;
    }
}
