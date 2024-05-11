package errorHandlers.errorTypes;

/**
 * Enum for semantic errors
 */
public enum SemanticErrorType implements ErrorType {
    FUNCTION_ALREADY_DEFINED("Function already defined"),
    VARIABLE_ALREADY_DEFINED("Variable already defined"),
    VARIABLE_NOT_DECLARED("Variable not declared"),
    FUNCTION_RETURN_TYPE_NOT_CORRECT("Function return type not correct"),
    RETURN_SECOND("Only one return statement is allowed"),
    RETURN_STATEMENT_MISSING("Function must have a return statement"),
    MAIN_FUNCTION_ALREADY_DEFINED("Main function already defined"),
    MAIN_FUNCTION_MISSING("Main function missing");


    //TODO: Add different types of errors,
    final String message;

    SemanticErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
