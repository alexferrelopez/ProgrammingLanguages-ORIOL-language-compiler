package FrontEnd.SymbolTable.BinaryTreesF2.Entities;

import Auxiliar.GraphicComponents.TreePainter;
import Auxiliar.MyArrayList;
import BinaryTreesF2.Algorithms.TreeBFS;

import javax.swing.*;

// Class used as a shared tree which is implemented as BSTTree and AVLTree
public abstract class BinaryTree {

    public SymbolTableNode root = null;   // Root of the tree (has no father Node)

    // Adds a node to the tree
    public abstract void addCitizen(Symbol symbol);

    // Removes a node from the tree
    public abstract void removeCitizen (long citizenId);

    // Prints all the nodes in the tree with the corresponding branches
    public void printRepresentation() {

        // Case in which there is no root node.
        if (root == null) {
            System.out.println(System.lineSeparator() + "No hi ha cap node a l'arbre.");
        }
        else {
            //We print the right part of the tree
            if (root.left != null) {
                print("", root.left, true);
            }
            // Tab space if there is no left part
            else {
                System.out.println();
            }

            for (int i = 0; i < root.getCitizens().length; i++) {
                Symbol rootSymbol = root.getCitizens()[i];

                if (i == 0) {
                    rootSymbol.printInfo(false, true);   // Print the star in front of the citizen just if the Node is the root.
                }
                else {
                    if (root.right != null) {
                        System.out.print("| ");
                        rootSymbol.printInfo(false, false);   // Print the star in front of the citizen just if the Node is the root.
                    }
                    else {
                        System.out.print("  ");
                        rootSymbol.printInfo(false, false);   // Print the star in front of the citizen just if the Node is the root.
                    }
                }
            }

            //We print the left part of the tree
            if (root.right != null) {
                print("", root.right, false);
            }
        }

    }

    private void print(String stringIndentation, SymbolTableNode symbolTableNode, boolean leftNode) {
        String stringIndentationAux;

        if (symbolTableNode.left != null) {
            if (leftNode) {
                stringIndentationAux = stringIndentation + "      ";
            } else {
                stringIndentationAux = stringIndentation + "|     ";
            }
            print(stringIndentationAux, symbolTableNode.left, true);
        }

        // Join the nodes to the parents, just those who have both a child and a parent on their left.
        if (symbolTableNode.left == null && symbolTableNode.parent != null && symbolTableNode.parent.right == symbolTableNode && symbolTableNode.right != null) {   // (node.parent != null && node.parent.left == node) would be (node.parent.left == node)
            System.out.println(stringIndentation + "|");
        }

        System.out.print(stringIndentation);

        // Add an indentation to the last nodes of the tree (leaves)
        if (!leftNode) {
            if (symbolTableNode.isLeaf()) {
                System.out.println("|");    //The space is already contained in the prior print of the indentation
                System.out.print(stringIndentation);
            }
        }

        // Check if it is the last right node of a branch
        if (symbolTableNode.left == null && leftNode) {
            System.out.println();
            System.out.print(stringIndentation);
        }

        System.out.print("|--- ");
//        for (Citizen nodeCitizen : node.getCitizens()) {
//            nodeCitizen.printInfo(false, node.equals(root));    // Print the star in front of the citizen just if the Node is the root.
//        }

        for (int i = 0; i < symbolTableNode.getCitizens().length; i++) {
            Symbol nodeSymbol = symbolTableNode.getCitizens()[i];

            // Print "|" only when there is a left child node.
            if (i > 0 && symbolTableNode.left != null && symbolTableNode.right != null && leftNode) {
                System.out.print(stringIndentation + "|    ");
            }
            else if (i > 0 && symbolTableNode.parent.left == symbolTableNode && leftNode) {
                System.out.print(stringIndentation + "|    ");
            } else if (i > 0 && symbolTableNode.parent.right == symbolTableNode) {
                System.out.print(stringIndentation + "     ");
            }

            nodeSymbol.printInfo(false, symbolTableNode.equals(root));    // Print the star in front of the citizen just if the Node is the root.
        }

        // Check if the parent of the node is on the left.
        if (symbolTableNode.parent.left == symbolTableNode && symbolTableNode.isLeaf()) {
            System.out.println(stringIndentation + "|");
        }

        // Check if a node only has a right child.
        if (!symbolTableNode.isLeaf() && symbolTableNode.right == null) {

            // Solves a problem with a random '|' printed.
            if (leftNode) {
                System.out.println(stringIndentation + "|");
            }
            else {
                // Avoid printing a line separator at the end.
                if (!stringIndentation.equals("")) {
                    System.out.println(stringIndentation);
                }
            }
        }

        if (symbolTableNode.right != null) {
            if (!leftNode) {
                stringIndentationAux = stringIndentation + "      ";
            } else {
                stringIndentationAux = stringIndentation + "|     ";
            }
            print(stringIndentationAux, symbolTableNode.right, false);
        }

        // Adding an extra indentation when a leaf node has a parent node to its right.
        if (symbolTableNode.isLeaf() && symbolTableNode.parent.right == symbolTableNode) {
            System.out.println(stringIndentation);
        }

    }

    // Given an id, it returns the Citizen object that has that id.
    public Symbol findCitizenById(long citizenId) {
        return TreeBFS.findCitizenById(root, citizenId);
    }

    // Given a starting node, searches for the left node that has the lowest value (used as a successor "inordre").
    public SymbolTableNode findMinNode(SymbolTableNode symbolTableNode) {
        while (symbolTableNode.left != null) {
            symbolTableNode = symbolTableNode.left;
        }
        return symbolTableNode;
    }

    // Given a minimum and maximum weight, find Citizens whose weight is between that range.
    public void findCitizensInRange(float max, float min) {

        MyArrayList<Symbol> witches = new MyArrayList<>();
        findCitizensInRange(max, min, root, witches);

        // Print all the witches (in case there is any)
        if (witches.size() > 0) {

            // Take into account if there is only one witch discovered (or +1 one)
            if (witches.size() == 1) {
                System.out.println("S'ha descobert " + witches.size() + " bruixa!");
            }
            else {
                System.out.println("S'han descobert " + witches.size() + " bruixes!");
            }

            // Print all the witches information
            for (Symbol witch : witches) {
                witch.printInfo(true, true);
            }
        }
        else {
            System.out.println("No s'ha descobert cap bruixa.");
        }
    }

    private void findCitizensInRange(float max, float min, SymbolTableNode symbolTableNode, MyArrayList<Symbol> witches) {

        // Check if exploring the nodes with a lower value than the current node is interesting: the current node value is over Minimum Value.
        if (symbolTableNode.left != null && symbolTableNode.getCitizenWeight() >= min) {
            findCitizensInRange(max, min, symbolTableNode.left, witches);
        }

        // Print the node if it meets the requirements: the Citizen's weight is between the limits / bounds (it's a Witch).
        if (min <= symbolTableNode.getCitizenWeight() && max >= symbolTableNode.getCitizenWeight()) {
            for (Symbol symbol : symbolTableNode.getCitizens()) {
                witches.add(symbol);
            }
        }

        // Check if exploring the nodes with a higher value than the current node is interesting: the current node value is below Maximum Value.
        if (symbolTableNode.right != null && symbolTableNode.getCitizenWeight() <= max ) {
            findCitizensInRange(max, min, symbolTableNode.right, witches);
        }
    }


    public void visualRepresentation() {
        JFrame frame;
        frame = new JFrame("Binary Search BSTTree Visual Representation");

        JScrollPane jScrollPane = new JScrollPane(new TreePainter(root, 1920, 1080));

        // Set scroll bars to always display.
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(jScrollPane);
        frame.pack();
//        frame.setResizable(false);
        frame.setVisible(true);
    }
}
