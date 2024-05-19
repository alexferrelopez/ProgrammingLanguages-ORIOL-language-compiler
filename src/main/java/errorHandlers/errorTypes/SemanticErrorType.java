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
    FUNCTION_PARAMETERS_DONT_MATCH("Function parameters don't match"),
    FUNCTION_PARAMETERS_NUMBER_INCORRECT("The number of parameters is doesn't match the function definition"),
    FUNCTION_PARAMETERS_INVALID("Function parameter cannot be a function call"),
    FUNCTION_RETURN_TYPE_NOT_CORRECT("Function return type not correct"),
	RETURN_STATEMENT_MISSING("Function must have a return statement"),
    ALREADY_USED_IDENTIFIER("The name of the identifier is already used"),
    OPERATOR_NOT_SUPPORTED("Operator not supported in arithmetic or logic expression"),
    INVALID_ARITHMETIC_EXPRESSION("Invalid arithmetic expression"),
	MAIN_FUNCTION_ALREADY_DEFINED("Main function already defined"),
	MAIN_FUNCTION_MISSING("Main function missing"),
    MAIN_FUNCTION_PARAMETERS("Main function cannot have parameters"),
    MAIN_FUNCTION_RETURN_TYPE("Main function must have miau return type"),
    UNEXPECTED_TOKEN_ASSIGNATION("An unexpected token is being assigned a value");

    final String message;

    SemanticErrorType(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
