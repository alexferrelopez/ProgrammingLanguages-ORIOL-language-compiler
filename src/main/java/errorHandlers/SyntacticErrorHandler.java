package errorHandlers;

import errorHandlers.errorTypes.ParserErrorType;
import errorHandlers.warningTypes.ParserWarningType;

/**
 * Error handler for the parser, extends error enums to give accurate error and warning messages.
 */
public class SyntacticErrorHandler extends AbstractErrorHandler<ParserErrorType, ParserWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(ParserErrorType errorType, Integer optionalLine, Integer optionalColumn, String word) {
        addError();
        /*
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        levenshteinDistance.apply("a", "b");
        levenshteinDistance.getThreshold();
        */
        StringBuilder sb = new StringBuilder();
        sb.append("Lexical error no.").append(this.getErrorCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");

        //TODO

        return "";
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(ParserWarningType warningType, int lineNum, int colNum, String word) {
        addWarning();
        return "";
    }
}
