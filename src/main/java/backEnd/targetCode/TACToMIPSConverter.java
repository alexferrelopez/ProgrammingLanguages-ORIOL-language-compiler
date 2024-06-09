package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import backEnd.exceptions.targetCode.FailedFileCreationException;
import backEnd.targetCode.operations.AssignmentOperations;
import backEnd.targetCode.operations.FunctionOperations;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.intermediateCode.TACInstruction;
import frontEnd.semantics.symbolTable.SymbolTableInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static backEnd.targetCode.MIPSOperations.NL;
import static backEnd.targetCode.MIPSOperations.TAB;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
    private static final String TARGET_FILE = "target/farm.asm";
    private final FunctionOperations functionOperations;
    private final AssignmentOperations assignmentOperations;
    private BufferedWriter targetCode;

    public TACToMIPSConverter(SymbolTableInterface symbolTable, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat) {
        assignmentOperations = new AssignmentOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat);
        functionOperations = new FunctionOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat, assignmentOperations);
    }

    private void createAssemblyFile() throws FailedFileCreationException {
        // Generate a "farm.asm" file as target code inside /target folder.
        // Write the MIPS code to the file.
        File code = new File(TARGET_FILE);
        try {
            // Use FileWriter and BufferedWriter to write to the file
            FileWriter writer = new FileWriter(code, false);
            targetCode = new BufferedWriter(writer);
        } catch (IOException e) {
            throw new FailedFileCreationException("Error creating file: " + TARGET_FILE);
        }
    }

    @Override
    public void generateMIPS(List<TACInstruction> instructions) throws TargetCodeException {
        createAssemblyFile();

        // Write the jump to the "main" function.
        try {
            targetCode.write("j ranch" + NL + NL);
        } catch (IOException e) {
            throw new FailedFileCreationException(e.getMessage());
        }

        for (TACInstruction instruction : instructions) {
            try {
                String instructionCode = convertTACInstruction(instruction);
                if (instructionCode != null) {
                    targetCode.write(instructionCode);
                }
            } catch (IOException e) {
                throw new FailedFileCreationException(e.getMessage());
            }
        }

        try {
            targetCode.close();
        } catch (IOException e) {
            throw new FailedFileCreationException("Error writing to file: " + TARGET_FILE, e);
        }
    }

    private String convertTACInstruction(TACInstruction instruction) throws IOException {
        return switch (instruction.getOperator()) {
            // ** Functions ** //
            case "function" -> functionOperations.funcDeclaration(instruction.getResult());
            case "Return" -> functionOperations.returnFunction(instruction.getOperand1());
            case "BeginFunc" -> functionOperations.beginFunction(instruction.getOperand1());
            case "PushParam" ->
                    showOperation(instruction, functionOperations.assignFunctionParameter(instruction.getOperand1(), instruction.getOperator()));
            case "EndFunc" -> functionOperations.endFunction();
            case "LCall" -> functionOperations.callFunction(instruction.getResult());

            // ** Assignments
            case "=" ->
                    showOperation(instruction, assignmentOperations.assignmentOperation(instruction.getOperand1(), instruction.getResult()));

            // *** Binary Operations ***
            case "GT", "LT", "EQ", "NEQ", "OR", "AND" ->
                    showOperation(instruction, assignmentOperations.addPendingLogicalOperation(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult(), instruction.getOperator()));

            // *** Conditional ***
            case "IfZ" ->
                    showOperation(instruction, assignmentOperations.addPendingLogicalOperation(instruction.getOperand1(), null, instruction.getResult(), instruction.getOperator()));

            // When we arrive at a conditional, we have to store the result of the previous operation in a register.
            case "Goto" ->
                    showOperation(instruction, assignmentOperations.conditionalJump(instruction.getResult(), instruction.getOperator()));

            // *** Labels ***
            case "label" -> showOperation(instruction, assignmentOperations.createLabel(instruction.getResult()));

            // *** Arithmetic Operations ***
            case "SUM", "SUB", "MUL", "DIV" ->
                    showOperation(instruction, assignmentOperations.addPendingOperation(instruction.getOperand1(), instruction.getOperand2(), instruction.getResult(), instruction.getOperator()));

            case "PopParams" -> "TODO"; //TODO;

            default -> null;
        };
    }

    private String showOperation(TACInstruction instruction, String codeMIPS) {
        String commentCode;

        // Change the comment if there is no result value (it's not an assignment nor operation, it's a tag).
        if (instruction.getResult() == null || instruction.getResult().isEmpty()) {
            commentCode = instruction.getOperator() + " " + instruction.getOperand1();
        } else {
            commentCode = instruction.toString();
        }

        return NL + TAB +
                assignmentOperations.writeComment("TAC: " + commentCode) +
                ((codeMIPS == null) ? "" : NL + codeMIPS) + NL;
    }
}
