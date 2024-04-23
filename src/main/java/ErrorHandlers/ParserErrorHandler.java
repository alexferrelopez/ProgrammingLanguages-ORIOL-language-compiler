package ErrorHandlers;

import ErrorHandlers.ErrorTypes.ParserErrorType;
import ErrorHandlers.WarningTypes.ParserWarningType;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Error handler for the parser, extends error enums to give accurate error and warning messages.
 */
public class ParserErrorHandler extends AbstractErrorHandler<ParserErrorType, ParserWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(ParserErrorType errorType) {
        addError();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        levenshteinDistance.apply("a", "b");
        levenshteinDistance.getThreshold();
        return "";
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(ParserWarningType warningType) {
        addWarning();
        return "";
    }
}
