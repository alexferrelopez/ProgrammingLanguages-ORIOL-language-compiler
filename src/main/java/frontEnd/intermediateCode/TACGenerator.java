package frontEnd.intermediateCode;

import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;

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
        generateCode(program);
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
                    return;  // Evita procesamiento adicional de los hijos
                case "while":
                    handleWhile(tree);
                    return;  // Evita procesamiento adicional de los hijos
                case "assignation":
                    handleAssignment(tree);
                    break;  // Asumimos que handleAssignment no procesa completamente todos los hijos
                case "func_params":
                    // Manejar los parámetros de una función
                    break;
                case "return_stmt":
                    // Manejar la sentencia 'return'
                    handleReturn(tree);
                    break;
            }
        }

        // Procesar recursivamente todos los hijos del nodo actual
        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            generateCode(child);
        }
    }

    private void handleReturn(Tree<AbstractSymbol> tree) {
        // We have RETURN, <return_stmt'>, ';'
        Tree<AbstractSymbol> return_stmt = tree.getChildren().get(1);

        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(return_stmt);

        if (leafNodes.size() == 1 && ((TerminalSymbol) leafNodes.get(0).getNode()).isEpsilon()) {
            // Return statement without a return value
            tacModule.addUnaryInstruction( null, "poop",null);
            return;
        }

        TerminalSymbol returnSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        tacModule.addUnaryInstruction(returnSymbol.getToken().getLexeme(), "poop", null);
    }


    private void handleIf(Tree<AbstractSymbol> tree) {
        // Condition
        Tree<AbstractSymbol> condition_expr = tree.getChildren().get(1).getChildren().get(1); // expr_bool
        Expression expr = generateExpressionCode(condition_expr);
        String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());

        // Labels
        String labelTrue = tacModule.createLabel();
        String labelFalse = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        // Jump instructions
        tacModule.addConditionalJump(tempVar, labelTrue);
        tacModule.addUnconditionalJump(labelFalse);

        // 'then' block
        tacModule.addLabel(labelTrue);
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
        // Similar a handleIf, pero adaptado para bucles 'while'
    }

    private void handleAssignment(Tree<AbstractSymbol> tree) {
        // Check if it's a function call or a simple assignment
        if (tree.getChildren().get(1).getChildren().get(0).getNode().getName().equals("func_call")) {
            // Handle function call
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

        TerminalSymbol operatorSymbol = (TerminalSymbol) leafNodes.get(1).getNode();
        String operator = operatorSymbol.getToken().getLexeme();

        TerminalSymbol leftOperandSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String leftOperand = leftOperandSymbol.getToken().getLexeme();

        TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
        String rightOperand = rightOperandSymbol.getToken().getLexeme();

        return new Expression(leftOperand, operator, rightOperand);
    }
}
