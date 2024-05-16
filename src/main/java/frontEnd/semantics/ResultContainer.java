package frontEnd.semantics;

import errorHandlers.errorTypes.SemanticErrorType;
import org.jetbrains.annotations.Nullable;

/**
 * Result container holds the information of any function that can return a boolean or an error that should be reported by the errorHandler.
 * @param isError boolean that indicates if the result is an error.
 * @param isEmpty boolean that indicates if the result is empty, and should not be reported by the errorHandler.
 * @param errorType type of error that should be reported by the errorHandler.
 * @param optionalLine line where the error was found.
 * @param optionalColumn column where the error was found.
 * @param word word that caused the error.
 */
public record ResultContainer(boolean isError, boolean isEmpty, SemanticErrorType errorType, @Nullable Integer optionalLine,
                              @Nullable Integer optionalColumn, String word) {
}
