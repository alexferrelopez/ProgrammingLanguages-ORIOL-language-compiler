package errorHandlers;

import errorHandlers.errorTypes.ErrorType;
import errorHandlers.errorTypes.LexicalErrorType;
import errorHandlers.warningTypes.LexicalWarningType;
import errorHandlers.warningTypes.WarningType;
import org.jetbrains.annotations.Nullable;

/**
 * Error handler for lexical errors, extends error enums to give accurate error and warning messages.
 */
public class LexicalErrorHandler extends AbstractErrorHandler<LexicalErrorType, LexicalWarningType> {
    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportError(LexicalErrorType errorType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word) {
        addError();
        StringBuilder sb = new StringBuilder();
        sb.append("Lexical error no.").append(this.getErrorCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");
        sb.append("\t").append(errorType.getMessage()).append(": ").append(word);

        String message = sb.toString();

        Report<ErrorType> report = new Report<>(errorType, optionalLine, optionalColumn, word, sb.toString());
        addErrorReport(report);

        return message;
    }

    /**
     * See parent class: @{@link AbstractErrorHandler}.
     */
    @Override
    public String reportWarning(LexicalWarningType warningType, @Nullable Integer optionalLine, @Nullable Integer optionalColumn, String word) {
        addWarning();
        StringBuilder sb = new StringBuilder();
        sb.append("Lexical warning no.").append(this.getWarningCount());

        if (optionalLine != null) sb.append(" at line ").append(optionalLine);
        if (optionalColumn != null) sb.append(", column ").append(optionalColumn);

        sb.append(":\n");
        sb.append("\t").append(warningType.getMessage()).append(": ").append(word);

        String message = sb.toString();

        Report<WarningType> report = new Report<>(warningType, optionalLine, optionalColumn, word, sb.toString());
        addWarningReport(report);

        return message;
    }

}