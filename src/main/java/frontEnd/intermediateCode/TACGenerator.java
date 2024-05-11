package frontEnd.intermediateCode;

import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;

public class TACGenerator {
    private TACModule tacModule;

    public TACGenerator(TACModule tacModule) {
        this.tacModule = tacModule;
    }

    public void generateCode(Tree<AbstractSymbol> tree) {
        if (tree == null || tree.getNode() == null) return;

        AbstractSymbol symbol = tree.getNode();

        // Si es un nodo no terminal, procesar basado en el tipo
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
                // otros casos...
            }
        }

        // Procesar recursivamente todos los hijos del nodo actual
        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            generateCode(child);
        }
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
        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(tree);

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
