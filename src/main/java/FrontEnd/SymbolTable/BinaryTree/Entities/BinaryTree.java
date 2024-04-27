package FrontEnd.SymbolTable.BinaryTree.Entities;

import FrontEnd.SymbolTable.BinaryTree.Algorithm.TreeBFS;
import FrontEnd.SymbolTable.Symbol;
import FrontEnd.SymbolTable.SymbolTableNode;

// Class used as a shared tree which is implemented as BSTTree and AVLTree
public abstract class BinaryTree {

    public SymbolTableNode root = null;   // Root of the tree (has no father Node)

    // Adds a node to the tree
    public abstract void addSymbolTable(Symbol<?> symbol);

    // Given an id, it returns the Citizen object that has that id.
    public Symbol<?> findCitizenById(long citizenId) {
        return TreeBFS.findCitizenById(root, citizenId);
    }

    // Given a starting node, searches for the left node that has the lowest value (used as a successor "inordre").
    public SymbolTableNode findMinNode(SymbolTableNode symbolTableNode) {
        while (symbolTableNode.left != null) {
            symbolTableNode = symbolTableNode.left;
        }
        return symbolTableNode;
    }
}
