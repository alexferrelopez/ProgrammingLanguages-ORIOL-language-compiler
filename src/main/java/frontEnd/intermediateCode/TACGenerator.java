package frontEnd.intermediateCode;

import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TACGenerator {
    private TACModule tacModule;
    private List<Tree<AbstractSymbol>> funcTreeList;
    private final SymbolTableInterface symbolTable;

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
            int bytesNeeded = 0; //symbolTable.calculateFunctionSize(functionName);
            // Use symbolTable to get the number of bytes needed. The node of the function has a hash table with the variables declared in the function
            // TODO -> calculate bytesNeeded
            tacModule.addUnaryInstruction(null, "BeginFunc", Integer.toString(bytesNeeded));

            generateCode(funcTree);

            // End the function with result: EndFunc
            tacModule.addUnaryInstruction(null, "EndFunc", null);
        }

        tacModule.addFunctionLabel("ranch");
        tacModule.addUnaryInstruction(null, "BeginFunc", "0");
        // Generate TAC code for main program
        generateCode(program);
        tacModule.addUnaryInstruction(null, "EndFunc", null);

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

        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        // Get the parameters
        List<String> parameters = new ArrayList<>();
        for (Tree<AbstractSymbol> leafNode : leafNodes) {
            TerminalSymbol terminalSymbol = (TerminalSymbol) leafNode.getNode();
            if (!terminalSymbol.getToken().getLexeme().equals(",") && !terminalSymbol.getToken().getLexeme().equals("(") && !terminalSymbol.getToken().getLexeme().equals(")")) {
                parameters.add(terminalSymbol.getToken().getLexeme());
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
        Expression expr = generateExpressionCode(condition_expr);
        String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());

        // Labels
        String labelFalse = tacModule.createLabel();
        String labelTrue = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        // Jump instructions
        tacModule.addConditionalJump(tempVar, labelFalse);

        // True block
        Tree<AbstractSymbol> func_body = tree.getChildren().get(1).getChildren().get(3);
        generateCode(func_body);
        tacModule.addUnconditionalJump(labelEnd);

        // 'else' block
        tacModule.addLabel(labelFalse);
        Tree<AbstractSymbol> elseBlock = tree.getChildren().get(2);
        // Check if there is an else block
        if (elseBlock.getChildren().get(0).getNode().getName() != "ε") {
            generateCode(elseBlock);
        }

        // End label for the if statement
        tacModule.addLabel(labelEnd);
    }

    private void handleFor(Tree<AbstractSymbol> tree) {
        // Initialization (get the loop variable, and it's value, normally "i")
        Tree<AbstractSymbol> initialization = getNodeBySymbolName(tree, "loop_variable");

        Tree<AbstractSymbol> terminalFirstVar = getNodeBySymbolName(initialization, "VARIABLE");
        String firstVar = ((TerminalSymbol) terminalFirstVar.getNode()).getToken().getLexeme();
        generateCode(initialization);

        // Condition
        Tree<AbstractSymbol> untilNumberNode = tree.getChildren().get(4);

        // Get the number to loop until
        Tree<AbstractSymbol> terminal = getNodeBySymbolName(untilNumberNode, "VALUE_INT");

        String lastVar = ((TerminalSymbol) terminal.getNode()).getToken().getLexeme();

        Expression expr = new Expression(firstVar, ">", lastVar);

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
        List<Tree<AbstractSymbol>> leafNodes = var_assignationTree.getLeafNodes(var_assignationTree);
        // Remove "ε" nodes in leafNodes
        leafNodes.removeIf(node -> ((TerminalSymbol) node.getNode()).isEpsilon());

        // Get the last leaf node, which is the increment value
        Tree<AbstractSymbol> incrementTree = leafNodes.get(leafNodes.size() - 1);
        String increment = ((TerminalSymbol) incrementTree.getNode()).getToken().getLexeme();

        // Get the assigment operation

        String assigmentOperation = leafNodes.get(2).getNode().getName();

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

        if (!Objects.equals(expr.getOperator(), "") && !Objects.equals(expr.getRightOperand(), "")) {
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
        // Check if it's a function call or a simple assignment
        // TODO -> check if the VARIABLE is a function call with the table of symbols
        if (getNodeBySymbolName(tree, "func_call") != tree) {
            Tree<AbstractSymbol> funcCall = getNodeBySymbolName(tree, "func_call");

            if (funcCall != null) {
                String functionName = ((TerminalSymbol) getNodeBySymbolName(funcCall, "VARIABLE").getNode()).getToken().getLexeme();

                // Handle function call
                Tree<AbstractSymbol> func_call_ = getNodeBySymbolName(tree, "func_call'");
                if (func_call_ != null) {
                    handleFunctionCall(func_call_, functionName);
                }
            }
        }
        Expression expr = generateExpressionCode(tree);

        tacModule.addUnaryInstruction(expr.getLeftOperand(), "=", expr.getRightOperand());
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

        TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
        String rightOperand = rightOperandSymbol.getToken().getLexeme();

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
