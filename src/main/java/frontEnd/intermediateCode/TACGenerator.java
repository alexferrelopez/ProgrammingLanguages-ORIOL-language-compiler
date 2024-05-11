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
                case "if":
                    handleIf(tree);
                    break;
                case "while":
                    handleWhile(tree);
                    break;
                case "assignation":
                    handleAssignment(tree);
                    break;
                // Agregar más casos según sea necesario
            }
        } else {
            // Si es un nodo terminal y necesita procesamiento especial (como 'if', 'while')
            handleTerminal(symbol);
        }

        // Procesar recursivamente todos los hijos del nodo actual
        for (Tree<AbstractSymbol> child : tree.getChildren()) {
            generateCode(child);
        }
    }

    private void handleTerminal(AbstractSymbol symbol) {
        // Aquí manejas los terminales que puedan influir en el control de flujo o similares
        if (symbol.getName().equals("FOR") || symbol.getName().equals("IF") || symbol.getName().equals("WHILE")) {
            // Si son parte de estructuras de control, podrías querer manejar los saltos o inicios de bucles aquí
        }
    }


    private void handleIf(Tree<AbstractSymbol> tree) {
        // Suposición: El primer hijo es la condición, el segundo es el bloque 'then', el tercero (opcional) el 'else'
        String conditionResult = generateExpressionCode(tree.getChildren().get(0));
        String labelTrue = tacModule.createLabel();
        String labelFalse = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        tacModule.addConditionalJump(conditionResult, labelTrue);
        tacModule.addUnconditionalJump(labelFalse);

        tacModule.addLabel(labelTrue);
        generateCode(tree.getChildren().get(1)); // Código del bloque 'then'
        tacModule.addUnconditionalJump(labelEnd);

        tacModule.addLabel(labelFalse);
        if (tree.getChildren().size() > 2) {
            generateCode(tree.getChildren().get(2)); // Código del bloque 'else'
        }
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

    private String generateExpressionCode(Tree<AbstractSymbol> node) {
        // Aquí manejas la generación de código para las expresiones aritméticas
        if (node.getChildren().isEmpty()) { // Es un nodo hoja, posiblemente un número o variable directa
            return node.getNode().getName();
        } else {
            // Recursivamente maneja los operadores y operandos
            String leftOperand = generateExpressionCode(node.getChildren().get(0));
            String operator = node.getNode().getName(); // El operador está en el nodo actual
            String rightOperand = generateExpressionCode(node.getChildren().get(1));
            String tempVar = tacModule.addBinaryInstruction(operator, leftOperand, rightOperand);
            return tempVar;
        }
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
}
