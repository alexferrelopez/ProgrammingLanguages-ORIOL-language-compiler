package frontEnd.intermediateCode;

import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

public class TACGenerator {
    private TACModule tacModule; // The TACModule to store the generated code

    /**
     * Constructor of the TACGenerator class
     * @param tacModule the TACModule to store the generated code
     */
    public TACGenerator(TACModule tacModule) {
        this.tacModule = tacModule;
    }

    /**
     * Method to generate the TAC code from a part of a tree
     * @param rootNode the root node of the tree
     */
    public void generateCode(Tree<AbstractSymbol> rootNode) {
        traverse(rootNode);
    }

    /**
     * Method to traverse the tree and generate the TAC code
     * @param node the current node to process
     */
    private void traverse(Tree<AbstractSymbol> node) {
        if (node == null) return;

        // If the node is a leaf, process it
        if (node.getChildren().isEmpty()) {
            handleLeaf(node);
        } else {
            // If the node is not a leaf, traverse its children
            for (Tree<AbstractSymbol> child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    /**
     * Method to handle the leaf nodes of the tree
     * @param node the leaf node to process
     */
    private void handleLeaf(Tree<AbstractSymbol> node) {
        TerminalSymbol symbol = (TerminalSymbol) node.getNode();
        // Generate code for the leaf node

    }

    private void handleAssignment(Tree<AbstractSymbol> node) {
        // Asumiendo que el nodo de asignación tiene exactamente dos hijos: un identificador y una expresión
        Tree<AbstractSymbol> identifierNode = node.getChildren().get(0);
        Tree<AbstractSymbol> expressionNode = node.getChildren().get(1);
        String resultVar = identifierNode.getNode().getName();  // Obtener el identificador a asignar
        String expressionCode = generateExpressionCode(expressionNode);  // Generar código para la expresión
        tacModule.addInstruction(resultVar + " = " + expressionCode);
    }

    private void handleIfStatement(Tree<AbstractSymbol> node) {
        // Asumiendo que el nodo de if tiene tres partes: condición, then y else
        String labelTrue = tacModule.createLabel();
        String labelFalse = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        String conditionCode = generateExpressionCode(node.getChildren().get(0));
        tacModule.addInstruction("if " + conditionCode + " goto " + labelTrue);
        tacModule.addInstruction("goto " + labelFalse);

        tacModule.addLabel(labelTrue);
        traverse(node.getChildren().get(1));
        tacModule.addInstruction("goto " + labelEnd);

        tacModule.addLabel(labelFalse);
        if (node.getChildren().size() > 2) {
            traverse(node.getChildren().get(2));  // Procesar el bloque else si existe
        }
        tacModule.addLabel(labelEnd);
    }

    private String generateExpressionCode(Tree<AbstractSymbol> node) {
        // Este método debería manejar la generación de código para expresiones,
        // incluyendo operaciones aritméticas, literales, y variables.
        AbstractSymbol symbol = node.getNode();
        if (symbol.isOperator()) {
            // Operaciones binarias
            String left = generateExpressionCode(node.getChildren().get(0));
            String right = generateExpressionCode(node.getChildren().get(1));
            String tempVar = tacModule.addBinaryInstruction(symbol.getOperator(), left, right);
            return tempVar;
        } else if (symbol.isIdentifier()) {
            // Identificadores simplemente retornan su nombre
            return symbol.getName();
        } else if (symbol.isLiteral()) {
            // Literales simplemente retornan su valor
            return symbol.getValue();
        }
        return "";
    }
}
