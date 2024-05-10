package frontEnd.intermediateCode;

import java.util.ArrayList;
import java.util.List;

public class TACModule {
    private List<TACInstruction> instructions;
    private int labelCounter;
    private int tempVarCounter;

    public TACModule() {
        this.instructions = new ArrayList<>();
    }

    public void addUnaryInstruction(String operand1, String operator) {
        // Uniary operations like negations or direct copies
        this.instructions.add(new TACInstruction(getNextTempVar(), operator, operand1, null));
    }

    public void addBinaryInstruction(String result, String operator, String operand1, String operand2) {
        this.instructions.add(new TACInstruction(result, operator, operand1, operand2));
    }

    public String addBinaryInstruction(String operator, String operand1, String operand2) {
        String tempResult = getNextTempVar();
        this.instructions.add(new TACInstruction(tempResult, operator, operand1, operand2));
        return tempResult;
    }

    public String createLabel() {
        return "L" + (labelCounter++);
    }

    public void addLabel(String label) {
        this.instructions.add(new TACInstruction(label, "label"));
    }

    public void addConditionalJump(String condition, String label) {
        this.instructions.add(new TACInstruction(label, "if", condition, null));
    }

    public void addUnconditionalJump(String label) {
        this.instructions.add(new TACInstruction(label, "goto"));
    }

    private String getNextTempVar() {
        return "t" + (tempVarCounter++);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TACInstruction instruction : instructions) {
            builder.append(instruction.toString()).append("\n");
        }
        return builder.toString();
    }
}
