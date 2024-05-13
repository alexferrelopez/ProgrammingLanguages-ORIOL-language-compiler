package frontEnd.intermediateCode;

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

    public TACGenerator(TACModule tacModule) {
        this.tacModule = tacModule;
    }

    public void generateTAC(Tree<AbstractSymbol> tree) {
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
            int bytesNeeded = 0;
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
                case "assignation":
                    handleAssignment(tree);
                    break;
                case "return_stmt":
                    handleReturn(tree);
                    break;
            }
        }

        // Procesar recursivamente todos los hijos del nodo actual
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
            if (!terminalSymbol.getToken().getLexeme().equals(",") && !terminalSymbol.getToken().getLexeme().equals("(") && !terminalSymbol.getToken().getLexeme().equals(")") ){
                parameters.add(terminalSymbol.getToken().getLexeme());
            }
        }

        for (String parameter : parameters) {
            tacModule.addUnaryInstruction("", "PushParam", parameter);
        }

        int numberOfParameters = parameters.size();
        // Create a new temporary variable to store the result of the function call
        tacModule.addUnaryInstruction(functionName, "LCall","");
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
            String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());
            tacModule.addConditionalJump(tempVar, labelEnd);
        } else {
            tacModule.addConditionalJump(expr.getLeftOperand(), labelEnd);
        }

        // 'do' block
        Tree<AbstractSymbol> func_body = tree.getChildren().get(1).getChildren().get(3);
        generateCode(func_body);
        tacModule.addUnconditionalJump(labelStart);

        // End label for the while loop
        tacModule.addUnconditionalJump(labelStart);
    }

    private void handleAssignment(Tree<AbstractSymbol> tree) {
        // Check if it's a function call or a simple assignment
        if (tree.getChildren().get(1).getChildren().get(0).getNode().getName().equals("func_call'")) {
            // Handle function call
            String functionName = ((TerminalSymbol) tree.getChildren().get(0).getNode()).getToken().getLexeme();
            handleFunctionCall(tree.getChildren().get(1).getChildren().get(0), functionName);
            return;
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
            return new Expression(terminalSymbol.getToken().getLexeme(), "", "");
        }
        TerminalSymbol operatorSymbol = (TerminalSymbol) leafNodes.get(1).getNode();
        String operator = operatorSymbol.getToken().getLexeme();

        TerminalSymbol leftOperandSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String leftOperand = leftOperandSymbol.getToken().getLexeme();

        TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
        String rightOperand = rightOperandSymbol.getToken().getLexeme();

        return new Expression(leftOperand, operator, rightOperand);
    }
}
