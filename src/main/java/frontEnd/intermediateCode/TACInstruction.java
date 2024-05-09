package frontEnd.intermediateCode;

public class TACInstruction {
    private String operator;  // Operator like +, -, *, /
    private String operand1;  // First operand
    private String operand2;  // Second operand (null if unary operation)
    private String result;    // Result of the operation or variable to assign


    /**
     * Constructor for binary operations
     * @param result Result of the operation
     * @param operator Operator of the operation
     * @param operand1 First operand
     * @param operand2 Second operand
     */
    public TACInstruction(String result, String operator, String operand1, String operand2) {
        this.result = result;
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    /**
     * Constructor for unary operations
     * @param result Result of the operation
     * @param operand1 Operand of the operation
     */
    public TACInstruction(String result, String operand1) {
        this.result = result;
        this.operand1 = operand1;
        this.operator = "=";  // Asignación por defecto
        this.operand2 = null;
    }

    public TACInstruction(String result) {
        this.result = result;
        this.operator = "=";  // Asignación por defecto
        this.operand1 = null;
        this.operand2 = null;
    }

    /**
     * Method to generate and return all the accumulated TAC code as a String
     * @return the operator
     */
    @Override
    public String toString() {
        if (operand2 != null) {
            return result + " = " + operand1 + " " + operator + " " + operand2;
        } else {
            return result + " = " + operand1;
        }
    }

}
