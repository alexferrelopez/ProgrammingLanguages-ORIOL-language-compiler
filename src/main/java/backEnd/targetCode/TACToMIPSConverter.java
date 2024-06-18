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
import java.util.Map;
import java.util.Optional;

public class TACToMIPSConverter implements TargetCodeGeneratorInterface {
    private static final String TARGET_FILE = "target/farm.asm";
    private final FunctionOperations functionOperations;
    private final AssignmentOperations assignmentOperations;
    private BufferedWriter targetCode;
    private final MIPSRenderer renderer;


    public TACToMIPSConverter(SymbolTableInterface symbolTable, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat, MIPSRenderer mipsRenderer) {
        assignmentOperations = new AssignmentOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat, mipsRenderer);
        functionOperations = new FunctionOperations(symbolTable, registerAllocatorInteger, registerAllocatorFloat, assignmentOperations, mipsRenderer);
        renderer = mipsRenderer;
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
            targetCode.write("j ranch" + System.lineSeparator());
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
        String operator = instruction.getOperator();
        final String result = instruction.getResult();
        final String operand1 = instruction.getOperand1();

        return switch (operator) {
            // ** Functions ** //
            case "function" -> functionOperations.funcDeclaration(result);
            case "Return" -> functionOperations.returnFunction(operand1);
            //case "BeginFunc" -> functionOperations.beginFunction(operand1);
            case "PushParam" ->
                    showOperation(instruction, functionOperations.assignFunctionParameter(operand1, operator));
            case "EndFunc" -> functionOperations.endFunction();
            case "LCall" -> functionOperations.callFunction(result);

            // ** Assignments
            case "=" ->
                    showOperation(instruction, assignmentOperations.assignmentOperation(operand1, result));

            // *** Binary Operations ***
            case "GT", "LT", "EQ", "NEQ", "OR", "AND" ->
                    showOperation(instruction, assignmentOperations.addPendingLogicalOperation(operand1, instruction.getOperand2(), result, operator));

            // *** Conditional ***
            case "IfZ" ->
                    showOperation(instruction, assignmentOperations.addPendingLogicalOperation(operand1, null, result, operator));

            // When we arrive at a conditional, we have to store the result of the previous operation in a register.
            case "Goto" ->
                    showOperation(instruction, assignmentOperations.conditionalJump(result, operator));

            // *** Labels ***
            case "label" -> showOperation(instruction, assignmentOperations.createLabel(result));

            // *** Arithmetic Operations ***
            case "SUM", "SUB", "MUL", "DIV" ->
                    showOperation(instruction, assignmentOperations.addPendingOperation(operand1, instruction.getOperand2(), result, operator));



            default -> null;
        };
    }

    private String showOperation(TACInstruction instruction, String codeMIPS) {
        String commentCode;
        String templatePath;
        Map<String, String> placeholders;

        // Change the comment if there is no result value (it's not an assignment nor operation, it's a tag).
        if (instruction.getResult() == null || instruction.getResult().isEmpty()) {
            commentCode = instruction.getOperator() + " " + instruction.getOperand1();
            templatePath = "show_operation_without_result_template";
            placeholders = Map.of("commentCode", commentCode);
        } else {
            commentCode = instruction.toString();
            templatePath = "show_operation_with_result_template";

            placeholders = Map.of("commentCode", commentCode, "codeMIPS", Optional.ofNullable(codeMIPS).orElse(""));
        }

        // Render the template
        return renderer.render(templatePath, placeholders);
    }

}
