package backEnd.targetCode.operations;

import backEnd.targetCode.MIPSOperations;
import backEnd.targetCode.MIPSRenderer;
import backEnd.targetCode.Operand;
import backEnd.targetCode.OperandContainer;
import backEnd.targetCode.registers.Register;
import backEnd.targetCode.registers.RegisterAllocator;
import frontEnd.lexic.dictionary.tokenEnums.DataType;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.scope.ScopeNode;
import frontEnd.semantics.symbolTable.symbol.FunctionSymbol;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.semantics.symbolTable.symbol.VariableSymbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FunctionOperations extends MIPSOperations {
    private final static int MAX_FUNCTION_PARAMETERS = 4;    // Only $a0 to $3 parameters are available.
    private final static String PARAMETERS_REGISTER_PREFIX = "$a";    // Only $a0 to $3 parameters are available.
    private final static String FUNCTION_PUSH_PARAMETER_OPERATOR = "PushParam";
    private final AssignmentOperations assignmentOperations;
    private int currentParameterNumber = 0;
    private final MIPSRenderer renderer;

    public FunctionOperations(SymbolTableInterface symbolTableInterface, RegisterAllocator registerAllocatorInteger, RegisterAllocator registerAllocatorFloat, AssignmentOperations assignmentOperations, MIPSRenderer renderer) {
        super(symbolTableInterface, registerAllocatorInteger, registerAllocatorFloat, renderer);
        this.assignmentOperations = assignmentOperations;
        this.renderer = renderer;
    }

    public String funcDeclaration(String functionLabel) {
        currentFunctionName.push(functionLabel);

        // Set the offset for each variable in the function (in all the nested scopes).
        long currentOffset = -8;
        String funcName = currentFunctionName.peek();
        currentOffset = assignParametersOffset(currentOffset, funcName);
        ScopeNode functionScope = symbolTable.getFunctionScope(funcName);
        currentOffset = assignOffset(functionScope, currentOffset);

        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(funcName);
        if (functionSymbol != null && !functionSymbol.isVariable()) {
            FunctionSymbol<?> function = (FunctionSymbol<?>) functionSymbol;
            function.setOffset(currentOffset);
        }

        if (functionLabel.equals(MAIN_FUNCTION)) {
            return renderer.render("main_declaration_template",
                    Map.of(
                    "stackAllocationSpace", String.valueOf(-currentOffset)
                    )
            );
        }

        return renderer.render("func_declaration_template", Map.of(
                "functionLabel", functionLabel,
                "stackAllocationSpace", String.valueOf(-currentOffset),
                "raOffset", String.valueOf(-currentOffset - 4)
                )
        );
    }

    private long assignOffset(ScopeNode scope, long currentOffset) {
        // This is the case for a non-declared ranch function.
        // A non-existing scope has no variables to assign nor children to search.
        if (scope == null) return currentOffset;

        // Search all the variables in the current scope.
        List<Symbol<?>> variables = new ArrayList<>(scope.getSymbols().values());
        for (Symbol<?> variable : variables) {
            VariableSymbol<?> variableSymbol = (VariableSymbol<?>) variable;

            // Assign an offset only if it's not a parameters (they already have an offset assigned previously).
            if (variableSymbol.isVariable() && !variableSymbol.isFunctionParameter()) {
                variable.setOffset(currentOffset);
                currentOffset -= variable.getSizeInBytes();
            }
        }

        // Do the same for all the nested scopes.
        for (ScopeNode child : scope.getChildren()) {
            currentOffset = assignOffset(child, currentOffset);
        }

        return currentOffset;
    }

    private long assignParametersOffset(long currentOffset, String functionName) {
        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(functionName);
        if (functionSymbol != null && !functionSymbol.isVariable()) {
            FunctionSymbol<?> function = (FunctionSymbol<?>) functionSymbol;

            // Loop through all the parameters (already ordered).
            for (VariableSymbol<?> parameter : function.getParameters()) {
                parameter.setOffset(currentOffset);
                currentOffset -= parameter.getSizeInBytes();
            }
        }

        return currentOffset;
    }

    public String beginFunction(String functionSize) {
        // Map the parameters passed
        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(currentFunctionName.peek());
        if (functionSymbol != null && functionSymbol.isFunction()) {
            FunctionSymbol<?> declaredFunction = (FunctionSymbol<?>) functionSymbol;

            int numParameter = 0;
            for (VariableSymbol<?> parameter : declaredFunction.getParameters()) {
                switch (parameter.getDataType()) {
                    case FLOAT ->
                            registerAllocatorFloat.customAllocateRegister(parameter.getOffset() + "(" + FP + ")", PARAMETERS_REGISTER_PREFIX + numParameter);
                    case INTEGER ->
                            registerAllocatorInteger.customAllocateRegister(parameter.getOffset() + "(" + FP + ")", PARAMETERS_REGISTER_PREFIX + numParameter);
                }
                numParameter++;
            }

        }

        Map<String, String> placeholders = Map.of(
                "functionSize", functionSize
        );

        // Render the template
        return renderer.render("begin_function_template", placeholders);
    }


    public String returnFunction(String returnValue) {
        String text = "";

        // Check if the return value is a symbol in the scope.
        Symbol<?> functionSymbol = symbolTable.findSymbolGlobally(currentFunctionName.peek());
        Symbol<?> variableSymbol = symbolTable.findSymbolInsideFunction(returnValue, currentFunctionName.peek());

        boolean isLiteral = true;
        if (variableSymbol != null && variableSymbol.isVariable()) {
            isLiteral = false;

            RegisterAllocator registerAllocator;
            if (functionSymbol.getDataType() == DataType.FLOAT) {
                registerAllocator = registerAllocatorFloat;
            } else {
                registerAllocator = registerAllocatorInteger;
            }

            Operand operand = new Operand(true, functionSymbol.getDataType(), variableSymbol.getOffset() + "(" + FP + ")", false);
            Operand destination = new Operand(true, functionSymbol.getDataType(), RETURN_VALUE_REGISTER, false);
            Register destionationRegister = registerAllocator.allocateRegister(destination);
            return text + assignmentOperations.registerToRegisterAssignment(destionationRegister, operand, functionSymbol.getDataType());
        }

        // Determine if the current function is the main function
        String funcName = currentFunctionName.peek();
        boolean isMainFunction = funcName.equals(MAIN_FUNCTION);

        long offset = 0;
        if (functionSymbol != null && !functionSymbol.isVariable()) {
            FunctionSymbol<?> function = (FunctionSymbol<?>) functionSymbol;
            offset = function.getOffset();
        }

        // Select the appropriate template based on the condition
        String templatePath = isMainFunction ? "end_main_function_template" : "end_regular_function_template";

        // Render the template
        String renderedTemplate = renderer.render(templatePath, Map.of(
                "stackAllocationSpace", String.valueOf(-offset),
                "raOffset", String.valueOf(-offset - 4)));


        return loadVariableToRegister(returnValue, RETURN_VALUE_REGISTER, functionSymbol.getDataType(), isLiteral) + renderedTemplate;
    }

    public String endFunction() {
        // Leave the current function
        currentFunctionName.pop();
        return "";
    }

    public String assignFunctionParameter(String parameterValue, String callOperator) {
        String destinationRegister = PARAMETERS_REGISTER_PREFIX + currentParameterNumber;

        if (currentParameterNumber < MAX_FUNCTION_PARAMETERS) {
            currentParameterNumber++;

            // Save the parameter to see its type when the "call" instruction is received.
            OperandContainer pushFunctionParameter = new OperandContainer();
            loadOperands(pushFunctionParameter, destinationRegister, parameterValue, null, callOperator, false);
            Operand parameter = new Operand(true, null, parameterValue, false);
            pushFunctionParameter.setOperand1(parameter);
            this.pendingOperations.add(pushFunctionParameter);
        }

        // The assignment internally checks if it's a variable or a normal value.
        return null;
    }

    public String callFunction(String functionName) {
        StringBuilder text = new StringBuilder();
        currentFunctionName.push(functionName); // Update the new current function.

        // Do all the previous operations.
        List<OperandContainer> operations = this.pendingOperations;
        for (int i = 0; i < operations.size(); i++) {
            OperandContainer operation = operations.get(i);
            // Make an assignment for the operators to be pushed into the function called.
            if (operation.getOperator().equals(FUNCTION_PUSH_PARAMETER_OPERATOR)) {
                // Get the current parameter to see the expected type.
                Symbol<?> function = symbolTable.findSymbolGlobally(currentFunctionName.peek());
                VariableSymbol<?> parameter = ((FunctionSymbol<?>) function).getParameters().get(i);

                // Prepare placeholders for rendering
                // Assign the value to an arguments' register.
                text.append(assignmentOperations.assignValueToRegister(operation.getOperand1().getValue(), operation.getDestination().getValue(), parameter.getDataType(), false));
            }
        }

        // Clear all the variables mapped and load into memory
        Iterator<Map.Entry<String, String>> iterator = registerAllocatorInteger.getVariableToRegister().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();

            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "variableName", key,
                    "register", entry.getValue()
            );

            // Render the load variable template
            text.append(renderer.render("load_variable_to_memory_template", placeholders));
            iterator.remove();
        }

        // Clear all the variables mapped for custom registers and load into memory
        iterator = registerAllocatorInteger.getVariableToCustomRegister().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();

            // Prepare placeholders for rendering
            Map<String, String> placeholders = Map.of(
                    "variableName", key,
                    "register", entry.getValue()
            );

            // Render the load variable template
            text.append(renderer.render("load_variable_to_memory_template", placeholders));
            iterator.remove();
        }

        this.currentParameterNumber = 0;
        currentFunctionName.pop(); // Leave the new current function.
        this.pendingOperations.clear();

        // Prepare placeholders for rendering the function call
        Map<String, String> functionCallPlaceholders = Map.of(
                "functionName", functionName
        );

        // Render the function call template and return the final text
        return text.append(renderer.render("function_call_template", functionCallPlaceholders)).toString();
    }

}
