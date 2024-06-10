package frontEnd.intermediateCode;

import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.symbol.Symbol;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TACGenerator {
    private final SymbolTableInterface symbolTable;
    private final TACModule tacModule;
    private List<Tree<AbstractSymbol>> funcTreeList;

    public TACGenerator(TACModule tacModule, SymbolTableInterface symbolTable) {
        this.tacModule = tacModule;
        this.symbolTable = symbolTable;
    }

    public List<TACInstruction> generateTAC(Tree<AbstractSymbol> tree) {
        // Get the program node
        Tree<AbstractSymbol> program = getProgramNode(tree);
        this.funcTreeList = getFunctionNodes(tree);
        // Remove the last element of the list, which is the program node
        this.funcTreeList.remove(this.funcTreeList.size() - 1);

        // Generate TAC code for each function
        for (Tree<AbstractSymbol> funcTree : this.funcTreeList) {
            // Add a label for the function, result: name:
            List<Tree<AbstractSymbol>> leafNodes = funcTree.getLeafNodes(tree);
            String functionName = ((TerminalSymbol) funcTree.getChildren().get(2).getChildren().get(0).getChildren().get(0).getNode()).getToken().getLexeme();
            tacModule.addFunctionLabel(functionName);

            // Start the function with result: BeginFunc, operand1: bytes_needed
            // The bytes_needed are calculated by the number of variables declared in the function
            int bytesNeeded = symbolTable.calculateFunctionSize(functionName);
            // Use symbolTable to get the number of bytes needed. The node of the function has a hash table with the variables declared in the function
            tacModule.addUnaryInstruction(null, "BeginFunc", Integer.toString(bytesNeeded));

            generateCode(funcTree);

            // End the function with result: EndFunc
            tacModule.addUnaryInstruction(null, "EndFunc", null);
        }

        String mainFunction = "ranch";
        tacModule.addFunctionLabel(mainFunction);
        int bytesNeeded = symbolTable.calculateFunctionSize(mainFunction);
        tacModule.addUnaryInstruction(null, "BeginFunc", String.valueOf(bytesNeeded));
        // Generate TAC code for main program
        generateCode(program);
        tacModule.addUnaryInstruction(null, "EndFunc", null);

        tacModule.printInstructions();

        return tacModule.getInstructions();
    }

    private List<Tree<AbstractSymbol>> getFunctionNodes(Tree<AbstractSymbol> tree) {
        List<Tree<AbstractSymbol>> functionNodes = new ArrayList<>();

        if (tree.getNode() instanceof NonTerminalSymbol && tree.getNode().getName().equals("func_type")) {
            functionNodes.add(tree);
        }

        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            functionNodes.addAll(getFunctionNodes(child));
        }

        return functionNodes;
    }

    private Tree<AbstractSymbol> getProgramNode(Tree<AbstractSymbol> tree) {
        if (tree.getNode() instanceof NonTerminalSymbol && tree.getNode().getName().equals("program")) {
            return tree;
        }

        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            Tree<AbstractSymbol> programNode = getProgramNode(child);
            if (programNode != null) {
                return programNode;
            }
        }

        return null;
    }

    private void generateCode(Tree<AbstractSymbol> tree) {
        if (tree == null || tree.getNode() == null) return;

        AbstractSymbol symbol = tree.getNode();

        if (!symbol.isTerminal()) {
            switch (symbol.getName()) {
                case "condition":
                    handleIf(tree);
                    return;  // Skip processing of children
                case "loop_while":
                    handleWhile(tree);
                    return; // Skip processing of children
                case "loop_for":
                    handleFor(tree);
                    return; // Skip processing of children
                case "assignation":
                    handleAssignment(tree);
                    break;
                case "return_stmt":
                    handleReturn(tree);
                    break;
            }
        }

        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            generateCode(child);
        }
    }


    private void handleFunctionCall(Tree<AbstractSymbol> tree, String functionName) {
        // We have '(', <func_params>, ')'
        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(tree);

        // Remove "ε" and "COMMA" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon() || node.getNode().getName().equals("COMMA"));

        // Get the parameters
        List<String> parameters = new ArrayList<>();
        for (int i = 0; i < leafNodes.size() - 1; i++) {
            TerminalSymbol terminalSymbol = (TerminalSymbol) leafNodes.get(i).getNode();
            // The parameters are between 'PO' and 'PC'
            if (terminalSymbol.getName().equals("PO")) {
                for (int j = i + 1; j < leafNodes.size(); j++) {
                    TerminalSymbol parameterSymbol = (TerminalSymbol) leafNodes.get(j).getNode();
                    if (parameterSymbol.getName().equals("PT")) {
                        break;
                    }
                    parameters.add(parameterSymbol.getToken().getLexeme());
                }
            } else if (terminalSymbol.getName().equals("PT")) {
                break;
            }
        }

        for (String parameter : parameters) {
            tacModule.addUnaryInstruction("", "PushParam", parameter);
        }

        int numberOfParameters = parameters.size();
        // Create a new temporary variable to store the result of the function call
        tacModule.addUnaryInstruction(functionName, "LCall", "");
        tacModule.addUnaryInstruction("", "PopParams", Integer.toString(numberOfParameters));

    }

    private void handleReturn(Tree<AbstractSymbol> tree) {
        // We have RETURN, <return_stmt'>, ';'
        Tree<AbstractSymbol> return_stmt = tree.getChildren().get(1);

        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(return_stmt);

        if (leafNodes.size() == 1 && ((TerminalSymbol) leafNodes.get(0).getNode()).isEpsilon()) {
            // Return statement without a return value
            tacModule.addUnaryInstruction("", "Return", "");
            return;
        }

        TerminalSymbol returnSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        tacModule.addUnaryInstruction("", "Return", returnSymbol.getToken().getLexeme());
    }


    private void handleIf(Tree<AbstractSymbol> tree) {
        // Condition
        Tree<AbstractSymbol> condition_expr = tree.getChildren().get(1).getChildren().get(1); // expr_bool

        // Teh condition can be: alive, dead, comparison or logic operation (AND, OR, NOT)
        // Get the leaf nodes of the condition expression
        List<Tree<AbstractSymbol>> leafNodes = condition_expr.getLeafNodes(condition_expr);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        String tempVar;

        // Check if the condition is NOT alive or dead
        if (leafNodes.get(0).getNode().getName().equals("NOT")) {
            leafNodes.remove(0);
            tempVar = handleNotAliveDead(leafNodes);
        } else if (leafNodes.size() == 1) {
            // We have a variable or a true / false value
            TerminalSymbol terminalSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
            tempVar = terminalSymbol.getToken().getLexeme();

            // Convert the operand to a number
            tempVar = convertLogicOperand(tempVar);

        } else {
            Expression expr = generateExpressionCode(condition_expr);
            tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());
        }


        // Labels
        String labelFalse = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        // Jump instructions
        tacModule.addConditionalJump(tempVar, labelFalse);

        // True block
        Tree<AbstractSymbol> func_body = tree.getChildren().get(1).getChildren().get(3);
        generateCode(func_body);

        // 'else' block
        Tree<AbstractSymbol> elseBlock = tree.getChildren().get(2);
        // Check if there is an else block
        if (!Objects.equals(elseBlock.getChildren().get(0).getNode().getName(), "ε")) {
            tacModule.addUnconditionalJump(labelEnd);

            tacModule.addLabel(labelFalse);
            generateCode(elseBlock);
            // End label for the if statement
            tacModule.addLabel(labelEnd);
        }
        else {
            tacModule.addLabel(labelFalse);
        }
    }

    private String handleNotAliveDead(List<Tree<AbstractSymbol>> leafNodes) {
        // Get the operand
        TerminalSymbol operandSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String operand = operandSymbol.getToken().getLexeme();

        // Convert the operand to a number
        String convertedOperand = convertLogicOperand(operand);

        // Store the result in a temporary variable
        return tacModule.addBinaryInstruction("NOT", convertedOperand, "");
    }

    private void handleFor(Tree<AbstractSymbol> tree) {
        // Initialization (get the loop variable, and it's value, normally "i")
        Tree<AbstractSymbol> initialization = getNodeBySymbolName(tree, "loop_variable");

        // Get leaf nodes of the initialization node
        assert initialization != null;
        List<Tree<AbstractSymbol>> leafNodes = initialization.getLeafNodes(initialization);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        String firstVar;
        if (leafNodes.get(0).getNode().getName().equals("VARIABLE")) {
            firstVar = ((TerminalSymbol) leafNodes.get(0).getNode()).getToken().getLexeme();
        } else {
            firstVar = ((TerminalSymbol) leafNodes.get(1).getNode()).getToken().getLexeme();
        }

        generateCode(initialization);

        // Condition
        Tree<AbstractSymbol> untilNumberNode = tree.getChildren().get(4);

        // Get the number to loop until
        Tree<AbstractSymbol> terminal = untilNumberNode.getLeafNodes(untilNumberNode).get(0);
        // Remove "ε" nodes in leafNodes
        String lastVar = ((TerminalSymbol) terminal.getNode()).getToken().getLexeme();

        Expression expr = new Expression(firstVar, "LT", lastVar);

        // Create a label
        String labelStart = tacModule.createLabel();
        tacModule.addLabel(labelStart);


        String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());

        // Labels
        String labelEnd = tacModule.createLabel();

        tacModule.addConditionalJump(tempVar, labelEnd);

        // 'do' block
        Tree<AbstractSymbol> func_body = getNodeBySymbolName(tree, "func_body");
        generateCode(func_body);


        Tree<AbstractSymbol> var_assignationTree = getNodeBySymbolName(tree.getChildren().get(6), "var_assignation");
        // Get all the leaf nodes of the var_assignation node
        List<Tree<AbstractSymbol>> leafNodes2 = var_assignationTree.getLeafNodes(var_assignationTree);
        // Remove "ε" nodes in leafNodes
        leafNodes2.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        // Get the last leaf node, which is the increment value
        Tree<AbstractSymbol> incrementTree = leafNodes2.get(leafNodes2.size() - 1);
        String increment = ((TerminalSymbol) incrementTree.getNode()).getToken().getLexeme();

        // Get the assigment operation

        String assigmentOperation = leafNodes2.get(2).getNode().getName();

        // Modify the value of the loop variable with a temporary variable
        String tempVar2 = tacModule.addBinaryInstruction(assigmentOperation, firstVar, increment);
        tacModule.addUnaryInstruction(firstVar, "=", tempVar2);

        tacModule.addUnconditionalJump(labelStart);

        // End label for the for loop
        tacModule.addLabel(labelEnd);
    }

    private Tree<AbstractSymbol> getNodeBySymbolName(Tree<AbstractSymbol> tree, String assignation) {
        if (tree.getNode().getName().equals(assignation)) {
            return tree;
        }

        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            Tree<AbstractSymbol> node = getNodeBySymbolName(child, assignation);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

    private void handleWhile(Tree<AbstractSymbol> tree) {
        // Condition
        Tree<AbstractSymbol> condition_expr = tree.getChildren().get(1).getChildren().get(1); // expr_bool
        Expression expr = generateExpressionCode(condition_expr);

        // Labels
        String labelStart = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        // Add label for the start of the while loop
        tacModule.addLabel(labelStart);


        // Get leafnodes
        List<Tree<AbstractSymbol>> leafNodes = condition_expr.getLeafNodes(condition_expr);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        if (leafNodes.size() == 1) {
            TerminalSymbol terminalSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
            String operand = convertLogicOperand(terminalSymbol.getToken().getLexeme());
            tacModule.addConditionalJump(operand, labelEnd);
        } else if (!Objects.equals(expr.getOperator(), "") && !Objects.equals(expr.getRightOperand(), "")) {
            String leftOperand = expr.getLeftOperand();
            leftOperand = convertLogicOperand(leftOperand);

            String rightOperand = expr.getRightOperand();
            rightOperand = convertLogicOperand(rightOperand);

            String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), leftOperand, rightOperand);
            tacModule.addConditionalJump(tempVar, labelEnd);
        } else {
            String operand = expr.getLeftOperand();
            operand = convertLogicOperand(operand);
            tacModule.addConditionalJump(operand, labelEnd);
        }

        // 'do' block
        Tree<AbstractSymbol> func_body = tree.getChildren().get(1).getChildren().get(3);
        generateCode(func_body);
        tacModule.addUnconditionalJump(labelStart);

        // End label for the while loop
        tacModule.addLabel(labelEnd);
    }

    private void handleAssignment(Tree<AbstractSymbol> tree) {
        // Get leaf nodes of the tree
        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(tree);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        if (leafNodes.size() == 3) {
            // We have a simple assignment
            Expression expr = generateExpressionCode(tree);

            // Check if the right operand is a logic operand
            if (expr.getRightOperand().equals("alive") || expr.getRightOperand().equals("dead")) {
                // Convert the logic operand to a number
                String rightOperand = convertLogicOperand(expr.getRightOperand());
                tacModule.addUnaryInstruction(expr.getLeftOperand(), "=", rightOperand);
                return;
            }
            tacModule.addUnaryInstruction(expr.getLeftOperand(), "=", expr.getRightOperand());

        } else if (containOperation(leafNodes) != null) {
            // We have an operation
            handleOperation(tree, containOperation(leafNodes));
        } else {
            // We have a function call

            // We have to know if the return value of the function is stored in a variable
            Symbol<?> symbol = symbolTable.findSymbolGlobally(((TerminalSymbol) leafNodes.get(0).getNode()).getToken().getLexeme());
            if (symbol != null) {
                if (symbol.isFunction()) {
                    handleFunctionCall(tree, ((TerminalSymbol) leafNodes.get(0).getNode()).getToken().getLexeme());
                }
            } else {
                String functionName = ((TerminalSymbol) leafNodes.get(2).getNode()).getToken().getLexeme();
                handleFunctionCall(tree, functionName);
                // Add the result of the function call to a temporary variable
                // Modify the value of the left operand with a temporary variable
                tacModule.addUnaryInstruction(((TerminalSymbol) leafNodes.get(0).getNode()).getToken().getLexeme(), "=", functionName);
            }
        }
    }

    private String containOperation(List<Tree<AbstractSymbol>> leafNodes) {
        for (Tree<AbstractSymbol> leafNode : leafNodes) {
            if (leafNode.getNode().getName().equals("SUM") || leafNode.getNode().getName().equals("SUB") || leafNode.getNode().getName().equals("MUL") || leafNode.getNode().getName().equals("DIV") || leafNode.getNode().getName().equals("MOD") || leafNode.getNode().getName().equals("POW") || leafNode.getNode().getName().equals("AND") || leafNode.getNode().getName().equals("OR") || leafNode.getNode().getName().equals("NOT") || leafNode.getNode().getName().equals("EQ") || leafNode.getNode().getName().equals("NEQ") || leafNode.getNode().getName().equals("GT") || leafNode.getNode().getName().equals("LT") || leafNode.getNode().getName().equals("GTE") || leafNode.getNode().getName().equals("LTE")) {
                return leafNode.getNode().getName();
            }
        }

        return null;
    }

    private void handleOperation(Tree<AbstractSymbol> tree, String operation) {
        // Get the leaf nodes of the tree
        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(tree);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        String tempVar = "";

        String leftOperand = "";
        String rightOperand = "";

        // Check if the right operand is "NOT"
        if (leafNodes.get(2).getNode().getName().equals("NOT")) {
            // Create a temporary variable to store a leafNode
            List<Tree<AbstractSymbol>> tempLeafNodes = new ArrayList<>(leafNodes);

            // Remove the variable name
            tempLeafNodes.remove(0);

            // Remove "is" and "NOT" nodes
            tempLeafNodes.removeIf(node -> node.getNode().getName().equals("IS") || node.getNode().getName().equals("NOT"));


            tempVar = handleNotAliveDead(tempLeafNodes);
        } else {
            // Get the left operand
            TerminalSymbol leftOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
            leftOperand = leftOperandSymbol.getToken().getLexeme();

            // Get the right operand
            TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(4).getNode();
            rightOperand = rightOperandSymbol.getToken().getLexeme();

            // Check if operands are logic operands
            leftOperand = convertLogicOperand(leftOperand);
            rightOperand = convertLogicOperand(rightOperand);

            // Create a temporary variable to store the result of the operation
            tempVar = tacModule.addBinaryInstruction(operation, leftOperand, rightOperand);
        }

        String variableName = ((TerminalSymbol) leafNodes.get(0).getNode()).getToken().getLexeme();

        // Modify the value of the left operand with a temporary variable
        tacModule.addUnaryInstruction(variableName, "=", tempVar);
    }

    public void printTAC() {
        System.out.println("Generated TAC Code:");
        tacModule.printInstructions();
    }

    private Expression generateExpressionCode(Tree<AbstractSymbol> expr_bool) {
        List<Tree<AbstractSymbol>> leafNodes = expr_bool.getLeafNodes(expr_bool);

        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());
        if (leafNodes.size() == 1) {
            TerminalSymbol terminalSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
            return new Expression(terminalSymbol.getName(), "", "");
        }
        TerminalSymbol operatorSymbol = (TerminalSymbol) leafNodes.get(1).getNode();
        String operator = operatorSymbol.getName();

        TerminalSymbol leftOperandSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String leftOperand = leftOperandSymbol.getToken().getLexeme();
        leftOperand = convertLogicOperand(leftOperand);

        TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
        String rightOperand = rightOperandSymbol.getToken().getLexeme();
        rightOperand = convertLogicOperand(rightOperand);

        return new Expression(leftOperand, operator, rightOperand);
    }

    private String convertLogicOperand(String operand) {
        return switch (operand) {
            case "alive" -> "1";
            case "dead" -> "0";
            default -> operand;
        };
    }
}
