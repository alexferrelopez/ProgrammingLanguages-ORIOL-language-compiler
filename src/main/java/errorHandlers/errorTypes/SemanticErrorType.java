package errorHandlers.errorTypes;

/**
 * Enum for semantic errors
 */
public enum SemanticErrorType implements ErrorType {
    VARIABLE_NOT_DECLARED("Variable not declared"),
	VARIABLE_ALREADY_DEFINED("Variable already defined"),
    DUPLICATE_SYMBOL_DECLARATION("Duplicate symbol declaration"),
    INCOMPATIBLE_TYPES("Incompatible types"),
    FUNCTION_NOT_DECLARED("Function not declared"),
	FUNCTION_ALREADY_DEFINED("Function already defined"),
    FUNCTION_NOT_CALLED_CORRECTLY("Function not called correctly"),
    FUNCTION_PARAMETERS_NOT_MATCH("Function parameters not match"),
    FUNCTION_PARAMETERS_INVALID("Function parameter cannot be a function call"),
    FUNCTION_RETURN_TYPE_NOT_CORRECT("Function return type not correct"),
    FUNCTION_RETURN_NOT_CORRECT("Function return not correct"),
	RETURN_SECOND("Only one return statement is allowed"),
	RETURN_STATEMENT_MISSING("Function must have a return statement"),
    ALREADY_USED_IDENTIFIER("The name of the identifier is already used"),
    INVALID_VALUE("Invalid value"),
    INVALID_BOOLEAN_EXPRESSION("Invalid boolean expression"),
    INVALID_ARITHMETIC_EXPRESSION("Invalid arithmetic expression"),
    DIVISION_BY_ZERO("Division by zero"),
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
