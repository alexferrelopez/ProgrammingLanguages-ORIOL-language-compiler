package frontEnd.intermediateCode;

import java.util.List;

public class TACModule {
    private List<String> instructions;  // Store all the instructions
    private int labelCounter;           // Create unique labels for if, while, and for statements


    /**
     * Method to add a general instruction to the TAC module
     * @param instruction the instruction to add
     */
    public void addInstruction(String instruction) {
        this.instructions.add(instruction);
    }

    /**
     * Method to add a unary instruction to the TAC module
     * @param result the result of the operation
     * @param operand the operand of the operation (ex: x = -y)
     */
    public void addUnaryInstruction(String result, String operand) {
        this.instructions.add(result + " = " + operand);
    }


    /**
     * Method to add a binary instruction to the TAC module
     * @param result the result of the operation
     * @param operator the operator of the operation (ex: +, -, *, /)
     * @param operand1 the first operand of the operation
     * @param operand2 the second operand of the operation
     */
    public void addBinaryInstruction(String result, String operator, String operand1, String operand2) {
        this.instructions.add(result + " = " + operand1 + " " + operator + " " + operand2);
    }


    /**
     * Method to create and return a unique label
     * @return
     */
    public String createLabel() {
        return "L" + (labelCounter++);
    }

    /**
     * Method to add a label to the TAC module
     * @param label the label to add
     */
    public void addLabel(String label) {
        this.instructions.add(label + ":");
    }

    /**
     * Method to generate and return all the accumulated TAC code as a String
     * @return the TAC code as a String
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String instr : instructions) {
            builder.append(instr).append("\n");
        }
        return builder.toString();
    }

}
