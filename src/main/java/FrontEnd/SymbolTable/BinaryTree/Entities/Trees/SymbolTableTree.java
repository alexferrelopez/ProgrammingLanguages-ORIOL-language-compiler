package FrontEnd.SymbolTable.BinaryTree.Entities.Trees;

import FrontEnd.SymbolTable.BinaryTree.Entities.BinaryTree;
import FrontEnd.SymbolTable.Symbol;
import FrontEnd.SymbolTable.SymbolTableNode;

public class SymbolTableTree extends BinaryTree {

    @Override
    public void addSymbolTable(Symbol<?> symbol) {
        add(root, symbol, null);    // The parent node of the root will always be NULL.
    }

    private void add(SymbolTableNode currentSymbolTableNode, Symbol<?> symbol, SymbolTableNode parentSymbolTableNode) {
        float valueToInsert;
        float currentNodeValue;

        // When the current node is null, a new node can be inserted into the position
        // (we've reached a leaf node, or it is the first node of the tree: the root)
        if (currentSymbolTableNode == null) {
            root = new SymbolTableNode(symbol, parentSymbolTableNode);
            return;
        }

        valueToInsert = symbol.getWeight();
        currentNodeValue = currentSymbolTableNode.getCitizenWeight();

        if (valueToInsert < currentNodeValue) {     // We go to the left child if the value that we want to insert is lower than the current node's value
            if (currentSymbolTableNode.left != null) {
                add(currentSymbolTableNode.left, symbol, currentSymbolTableNode);
            }
            else {
                currentSymbolTableNode.left = new SymbolTableNode(symbol, currentSymbolTableNode);
            }
        } else if (valueToInsert > currentNodeValue) {      // We go to the right child if the value that we want to insert is higher than or equal to the current node's value
            if (currentSymbolTableNode.right != null) {
                add(currentSymbolTableNode.right, symbol, currentSymbolTableNode);
            }
            else {
                currentSymbolTableNode.right = new SymbolTableNode(symbol, currentSymbolTableNode);
            }
        } else {
            // If value of the node has the same weight means there already is a node with that weight, so a citizen is added to the node's citize list.
            currentSymbolTableNode.addCitizen(symbol);
        }

        // Case where the node is added
        currentSymbolTableNode.calculateHeight();
    }

}
