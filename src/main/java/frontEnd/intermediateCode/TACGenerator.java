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
        // Condition expression
        Tree<AbstractSymbol> condition_expr = tree.getChildren().get(1);

        // Get expr_bool
        Tree<AbstractSymbol> expr_bool = condition_expr.getChildren().get(1);

        // Use generateExpressionCode
        Expression expr = generateExpressionCode(expr_bool);

        String tempVar = tacModule.addBinaryInstruction(expr.getOperator(), expr.getLeftOperand(), expr.getRightOperand());

        String labelTrue = tacModule.createLabel();
        String labelFalse = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        // Add conditional jump
        tacModule.addConditionalJump(tempVar, labelTrue);
        tacModule.addUnconditionalJump(labelFalse);

        // Handle 'then' block
        tacModule.addLabel(labelTrue);
        generateCode(tree.getChildren().get(2)); // Asumiendo que el bloque 'then' es el tercer hijo
        tacModule.addUnconditionalJump(labelEnd); // Salto al final después del bloque 'then'

        // Handle 'else' block
        tacModule.addLabel(labelFalse);
        if (tree.getChildren().size() > 3) {
            generateCode(tree.getChildren().get(3)); // Asumiendo que el bloque 'else' es el cuarto hijo
        }

        // Label del final del bloque 'if'
        tacModule.addLabel(labelEnd);
    }


    private void handleWhile(Tree<AbstractSymbol> tree) {
        // Similar a handleIf, pero adaptado para bucles 'while'
    }

    private void handleAssignment(Tree<AbstractSymbol> tree) {
        // Supongamos que los nodos de asignación tienen dos hijos
        TerminalSymbol assignSymbol = (TerminalSymbol) tree.getChildren().get(0).getNode();

        String lhs = assignSymbol.getToken().getLexeme(); // Variable a la izquierda

        // Para llegar a la expresión a la derecha, necesitas procesar los hijos del nodo de asignación
        Tree<AbstractSymbol> assignationNode = tree.getChildren().get(2);

        List<Tree<AbstractSymbol>> leafNodes = tree.getLeafNodes(assignationNode);

        TerminalSymbol assignationSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String rhs = assignationSymbol.getToken().getLexeme(); // Variable o número a la derecha
        tacModule.addUnaryInstruction(lhs, "=", rhs);
    }

    // Handle declaration if an assignation is present
    private void handleDeclaration(Tree<AbstractSymbol> tree) {
        Tree<AbstractSymbol> assignationNode = tree.getChildren().get(1);

        if (assignationNode != null) {
            handleAssignment(assignationNode);
        }
    }


    public void printTAC() {
        System.out.println("Generated TAC Code:");
        tacModule.printInstructions();
    }

    private Expression generateExpressionCode(Tree<AbstractSymbol> expr_bool) {
        List<Tree<AbstractSymbol>> leafNodes = expr_bool.getLeafNodes(expr_bool);

        TerminalSymbol operatorSymbol = (TerminalSymbol) leafNodes.get(1).getNode();
        String operator = operatorSymbol.getToken().getLexeme();

        TerminalSymbol leftOperandSymbol = (TerminalSymbol) leafNodes.get(0).getNode();
        String leftOperand = leftOperandSymbol.getToken().getLexeme();

        TerminalSymbol rightOperandSymbol = (TerminalSymbol) leafNodes.get(2).getNode();
        String rightOperand = rightOperandSymbol.getToken().getLexeme();

        return new Expression(leftOperand, operator, rightOperand);
    }
}
