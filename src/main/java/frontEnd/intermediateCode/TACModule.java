package frontEnd.intermediateCode;

import java.util.List;

public class TACModule {
    private List<TACInstruction> instructions;  // Store all the instructions
    private int labelCounter;           // Create unique labels for if, while, and for statements (L0, L1, L2, ...)
    private int tempVarCounter;         // Create unique temporary variables (t0, t1, t2, ...)


    /**
     * Method to add a general instruction to the TAC module
     * @param instruction the instruction to add (ex: x = 5)
     */
    public void addInstruction(String instruction) {
        this.instructions.add(new TACInstruction(instruction));
    }

    /**
     * Method to add a unary instruction to the TAC module
     * @param result the result of the operation
     * @param operand the operand of the operation (ex: b + a)
     */
    public void addUnaryInstruction(String result, String operand) {
        this.instructions.add(new TACInstruction(result, operand));
    }


    /**
     * Method to add a binary instruction that NOT requires a temporary variable to the TAC module (ex: x = 5 + 3)
     * @param result the result of the operation
     * @param operator the operator of the operation (ex: +, -, *, /)
     * @param operand1 the first operand of the operation
     * @param operand2 the second operand of the operation
     */
    public void addBinaryInstruction(String result, String operator, String operand1, String operand2) {
        this.instructions.add(new TACInstruction(result, operator, operand1, operand2));
    }

    /**
     * Method to add a binary instruction that requires a temporary variable to the TAC module (ex: t0 = 5 + 3)
     * @param operator the operator of the operation (ex: +, -, *, /)
     * @param operand1 the first operand of the operation
     * @param operand2 the second operand of the operation
     * @return the temporary variable that stores the result of the operation
     */
    public String addBinaryInstruction(String operator, String operand1, String operand2) {
        String tempResult = getNextTempVar();
        this.instructions.add(new TACInstruction(tempResult, operator, operand1, operand2));
        return tempResult;
    }

    /**
     * Method to create and return a unique label
     * @return the unique label
     */
    public String createLabel() {
        return "L" + (labelCounter++);
    }

    /**
     * Method to add a label to the TAC module
     * @param label the label to add
     */
    public void addLabel(String label) {
        this.instructions.add(new TACInstruction(label + ":"));
    }


    /**
     * Method to create and return a unique temporary variable
     * @return the unique temporary variable
     */
    private String getNextTempVar() {
        return "t" + (tempVarCounter++);
    }

    /**
     * Method to generate and return all the accumulated TAC code as a String
     * @return the TAC code as a String
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TACInstruction instruction : instructions) {
            builder.append(instruction).append("\n");
        }
        return builder.toString();
    }
}
