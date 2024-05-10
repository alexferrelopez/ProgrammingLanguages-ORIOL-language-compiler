package frontEnd.intermediateCode;

import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.Tree;

import static frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol.IF;

public class TACGenerator {
    private TACModule tacModule;

    public TACGenerator(TACModule tacModule) {
        this.tacModule = tacModule;
    }

    public void generateCode(Tree<Token> tree) {
        if (tree.getNode().getType().equals(IF)) {
            handleIf(tree);
            // TODO -> Add more cases for other types of nodes
        }
    }

    private void handleIf(Tree<Token> tree) {
        String condition = generateExpressionCode(tree.getChildren().get(0)); // Genera el código para la condición
        String labelTrue = tacModule.createLabel();
        String labelEnd = tacModule.createLabel();

        tacModule.addConditionalJump(condition, labelTrue);
        generateCode(tree.getChildren().get(1)); // Código para el bloque 'then'
        tacModule.addUnconditionalJump(labelEnd);

        tacModule.addLabel(labelTrue);
        generateCode(tree.getChildren().get(2)); // Código para el bloque 'else'

        tacModule.addLabel(labelEnd);
    }

    // Este método debe ser implementado para manejar la generación de expresiones
    private String generateExpressionCode(Tree<Token> expressionNode) {
        // Implementar la lógica para generar código de expresiones
        return ""; // Retorna una representación de la expresión
    }
}
