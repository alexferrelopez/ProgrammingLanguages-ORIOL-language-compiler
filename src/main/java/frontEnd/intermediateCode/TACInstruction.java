package frontEnd.intermediateCode;

public class TACInstruction {
    private final String operator;  // Operator like +, -, *, /
    private final String operand1;  // First operand
    private final String operand2;  // Second operand (null if unary operation)
    private final String result;    // Result of the operation or variable to assign


    /**
     * Constructor for binary operations
     *
     * @param result   Result of the operation
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

    // For if, while, for, do-while, etc.
    public TACInstruction(String label, String operator) {
        this.result = label;
        this.operator = operator;
        this.operand1 = null;
        this.operand2 = null;
    }

    /**
     * Method to generate and return all the accumulated TAC code as a String
     *
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

    public String getOperator() {
        return operator;
    }

    public String getOperand1() {
        return operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public String getResult() {
        return result;
    }

}
