package FrontEnd.SymbolTable.BinaryTreesF2.Entities;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableNode {

    private List<Symbol> symbolList;   // Citizens array of a node to put all the citizens with same weight together
    public SymbolTableNode left;
    public SymbolTableNode right;
    public SymbolTableNode parent;
    public int height;

    public SymbolTableNode(Symbol symbolList) {
        this.symbolList = new ArrayList<>(List.of(new Symbol[]{symbolList}));
        this.left = null;
        this.right = null;
        this.parent = null;
        this.height = 0;
    }

    public SymbolTableNode(Symbol symbolList, SymbolTableNode parentSymbolTableNode) {
        this.symbolList = new ArrayList<>(List.of(new Symbol[]{symbolList}));
        this.left = null;
        this.right = null;
        this.parent = parentSymbolTableNode;
        this.height = 0;    // Initialize height value for null nodes.
    }

    public Symbol[] getCitizens() {
        return symbolList.toArray(new Symbol[0]);
    }

    public float getCitizenWeight() {
        return symbolList.get(0).getWeight();
    }

    public boolean isLeaf() {
        return (right == null && left == null);
    }

    public int getHeight() {
        return height;
    }

    // Get the height of the current node: maximum between right and left node + 1 (the node itself).
    public void calculateHeight() {

        // Case were there is no children (leaf node)
        if (right == null && left == null) {
            height = 0;
            return;
        }

        // Case where there is no right child
        if (right == null) {
            height = left.height + 1;
            return;
        }

        // Case where is no left child
        if (left == null) {
            height = right.height + 1;
            return;
        }

        // Case where the node has both children
        height = Math.max(right.height, left.height) + 1;
    }

    public void addCitizen(Symbol symbol) {
        this.symbolList.add(symbol);
    }

    public void removeCitizen(Symbol symbol) {
        this.symbolList.remove(symbol);
    }

}
