package frontEnd.intermediateCode;

public class Expression {
    private final String leftOperand;
    private final String operator;
    private final String rightOperand;

    public Expression(String leftOperand, String operator, String rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public String getLeftOperand() {
        return leftOperand;
    }

    public String getOperator() {
        return operator;
    }

    public String getRightOperand() {
        return rightOperand;
    }
}