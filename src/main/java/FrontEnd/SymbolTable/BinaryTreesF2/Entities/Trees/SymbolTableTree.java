package FrontEnd.SymbolTable.BinaryTreesF2.Entities.Trees;

import BinaryTreesF2.Entities.Symbol;
import BinaryTreesF2.Entities.SymbolTableNode;

public class SymbolTableTree extends BinaryTreesF2.Entities.BinaryTree {

    @Override
    public void addCitizen(Symbol symbol) {
        add(root, symbol, null);    // The parent node of the root will always be NULL.
    }

    @Override
    public void removeCitizen(long citizenId) {
        Symbol symbol = findCitizenById(citizenId);
        root = remove(root, symbol);
    }

    private void add(SymbolTableNode currentSymbolTableNode, Symbol symbol, SymbolTableNode parentSymbolTableNode) {
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

    private SymbolTableNode remove (SymbolTableNode currentSymbolTableNode, Symbol symbol) {

        // Case where the node to remove is not found.
        if (currentSymbolTableNode == null) {
            return null;
        }

        // We go to the right child if the value that we want to delete is higher than the current node's value
        if (symbol.getWeight() > currentSymbolTableNode.getCitizenWeight()) {
            currentSymbolTableNode.right = remove(currentSymbolTableNode.right, symbol);
            currentSymbolTableNode.calculateHeight(); // Re-calculate the height of the current node.
            return currentSymbolTableNode;
        }

        //We go to the left child if the value that we want to delete is lower than the current node's value
        if (symbol.getWeight() < currentSymbolTableNode.getCitizenWeight()) {
            currentSymbolTableNode.left = remove(currentSymbolTableNode.left, symbol);
            currentSymbolTableNode.calculateHeight(); // Re-calculate the height of the current node.
            return currentSymbolTableNode;
        }

        // Node to delete found - We have to make sure the citizen is the one with the select ID.
        if (symbol.getWeight() == currentSymbolTableNode.getCitizenWeight()) {

            if (currentSymbolTableNode.getCitizens().length == 1) {
                //If the node does not have children, we return null (replacing this node with null)
                if (currentSymbolTableNode.right == null && currentSymbolTableNode.left == null) {
                    return null;
                }

                // Case in which the node deleted has a right child.
                if (currentSymbolTableNode.left == null) {

                    // Check if the currentNode is a left or right child
                    if (currentSymbolTableNode.parent.right == currentSymbolTableNode) {
                        currentSymbolTableNode.parent.right = currentSymbolTableNode.right;
                    }
                    else {
                        currentSymbolTableNode.parent.left = currentSymbolTableNode.right;
                    }

                    currentSymbolTableNode.right.parent = currentSymbolTableNode.parent;

                    currentSymbolTableNode.right.calculateHeight(); // Re-calculate the height of the current node.
                    return currentSymbolTableNode.right;
                }

                // If the node only has one child, we return the child (replacing this node with the node's child in the parent)
                if (currentSymbolTableNode.right == null) {

                    // Check if the currentNode is a left or right child
                    if (currentSymbolTableNode.parent.left == currentSymbolTableNode) {
                        currentSymbolTableNode.parent.left = currentSymbolTableNode.left;
                    }
                    else {
                        currentSymbolTableNode.parent.right = currentSymbolTableNode.left;
                    }

                    currentSymbolTableNode.left.parent = currentSymbolTableNode.parent;
                    currentSymbolTableNode.left.calculateHeight(); // Re-calculate the height of the current node.
                    return currentSymbolTableNode.left;
                }
            }
            else {
                // Just remove the citizen and update the list, but keep the node in the same place (return the same node).
                currentSymbolTableNode.removeCitizen(symbol);
                return currentSymbolTableNode;
            }
            /////////////////////////////////
        }

        //If the node has two children, we need to reorganize the tree.

        //We will need to replace the node with another node that has a suitable value.
        //Knowing the value of the node that we want to delete, we will choose the node with the
        //next biggest value as a substitute. To choose this node, we will first go to the right node
        //(which has a greater value) and then find the lowest value in the subtree. This value will
        //be the next biggest value that we were searching for.

        SymbolTableNode tempSymbolTableNode = findMinNode(currentSymbolTableNode.right); // Find the node with the lowest value in the right subtree (given an origin/root node) = successor "inordre"

        // Specific case for root
        if (currentSymbolTableNode != root) {
            // Check if the removed node was a right or a left child.
            if (currentSymbolTableNode.parent.right == currentSymbolTableNode) {
                // Assign the parent of the new node to the node to be deleted parent.
                currentSymbolTableNode.parent.right = tempSymbolTableNode;
            } else {
                currentSymbolTableNode.parent.left = tempSymbolTableNode;
            }
            tempSymbolTableNode.parent = currentSymbolTableNode.parent;

            // Keep the same children that the node to be deleted has.
            tempSymbolTableNode.left = currentSymbolTableNode.left;

            tempSymbolTableNode.calculateHeight(); // Re-calculate the height of the current node.
            return tempSymbolTableNode;
        }

        // Case when the root node is removed.
        else {
            // There is no parent.
            tempSymbolTableNode.parent.left = tempSymbolTableNode.right;  // Assign the same child of the successor node to its parent (always a left child node).
            tempSymbolTableNode.parent = null;                 // Root node has no parent.

            // Change the current root node (assign its children to the new root node).
            tempSymbolTableNode.right = root.right;
            tempSymbolTableNode.left = root.left;
            root = tempSymbolTableNode;

            root.calculateHeight(); // Re-calculate the height of the current node.
            return tempSymbolTableNode;
        }
    }

}
