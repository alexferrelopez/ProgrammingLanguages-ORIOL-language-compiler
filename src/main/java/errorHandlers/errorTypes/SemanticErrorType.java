package errorHandlers.errorTypes;

/**
 * Enum for semantic errors
 */
public enum SemanticErrorType implements ErrorType {
    VARIABLE_NOT_DECLARED("Variable not declared"),
    DUPLICATE_SYMBOL_DECLARATION("Duplicate symbol declaration"),
    INCOMPATIBLE_TYPES("Incompatible types"),
    FUNCTION_NOT_DECLARED("Function not declared"),
    FUNCTION_NOT_CALLED_CORRECTLY("Function not called correctly"),
    FUNCTION_PARAMETERS_NOT_MATCH("Function parameters not match"),
    FUNCTION_PARAMETERS_INVALID("Function parameter cannot be a function call"),
    FUNCTION_RETURN_TYPE_NOT_CORRECT("Function return type not correct"),
    FUNCTION_RETURN_NOT_CORRECT("Function return not correct"),
    NOT_A_VARIABLE("Function is not a variable"),
    INVALID_VALUE("Invalid value"),
    INVALID_BOOLEAN_EXPRESSION("Invalid boolean expression"),
    INVALID_ARITHMETIC_EXPRESSION("Invalid arithmetic expression"),
    DIVISION_BY_ZERO("Division by zero"),;

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
