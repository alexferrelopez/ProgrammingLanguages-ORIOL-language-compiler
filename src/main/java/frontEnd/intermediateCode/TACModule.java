package frontEnd.intermediateCode;

import java.util.ArrayList;
import java.util.List;

public class TACModule {
    private final List<TACInstruction> instructions;
    private int labelCounter;
    private int tempVarCounter;

    public TACModule() {
        this.instructions = new ArrayList<>();
    }

    public void addUnaryInstruction(String result, String operator, String operand) {
        this.instructions.add(new TACInstruction(result, operator, operand, null));
    }

    public void addBinaryInstruction(String result, String operator, String operand1, String operand2) {
        this.instructions.add(new TACInstruction(result, operator, operand1, operand2));
    }

    public String addBinaryInstruction(String operator, String operand1, String operand2) {
        String tempResult = getNextTempVar();
        TACInstruction tacInstruction = new TACInstruction(tempResult, operator, operand1, operand2);
        this.instructions.add(tacInstruction);
        return tempResult;
    }

    public String createLabel() {
        return "$L" + (labelCounter++);
    }

    public void addLabel(String label) {
        this.instructions.add(new TACInstruction(label, "label"));
    }

    public void addConditionalJump(String condition, String targetLabel) {
        this.instructions.add(new TACInstruction(null, "IfZ", condition, null));
        this.instructions.add(new TACInstruction(targetLabel, "Goto", null, null));
    }


    public void addUnconditionalJump(String label) {
        this.instructions.add(new TACInstruction(label, "Goto", null, null));
    }

    private String getNextTempVar() {
        return "$t" + (tempVarCounter++);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TACInstruction instruction : instructions) {
            builder.append(instruction.toString()).append("\n");
        }
        return builder.toString();
    }

    public void printInstructions() {
        // Encabezados para las columnas
        System.out.printf("%-15s | %-10s | %-10s | %-10s%n", "Result", "Operator", "Operand1", "Operand2");
        System.out.println("------------------------------------------------------------");

        // Iterar sobre todas las instrucciones y formatearlas según el encabezado
        for (TACInstruction instruction : instructions) {
            String result = instruction.getResult() != null ? instruction.getResult() : "";
            String operator = instruction.getOperator() != null ? instruction.getOperator() : "";
            String operand1 = instruction.getOperand1() != null ? instruction.getOperand1() : "";
            String operand2 = instruction.getOperand2() != null ? instruction.getOperand2() : "";

            System.out.printf("%-15s | %-10s | %-10s | %-10s%n", result, operator, operand1, operand2);
        }
    }


    public void addDeclaration(String variableName, String variableType) {
        this.instructions.add(new TACInstruction(variableName, "declaration", variableType, null));
    }

    public void addFunctionLabel(String functionName) {
        this.instructions.add(new TACInstruction(functionName, "function"));
    }

    public List<TACInstruction> getInstructions() {
        return instructions;
    }
}
