package frontEnd.sintaxis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Tree<T> {
    private final T node;
    private final List<Tree<T>> children;
    private Tree<T> parent;

    public Tree(T node) {
        this.node = node;
        children = new LinkedList<>();
        parent = null;
    }

    public Tree(Tree<T> t) {
        this.node = t.node;
        children = new LinkedList<>();
        parent = t.parent;
        for (var c : t.children) {
            children.add(new Tree<>(c));
        }
    }

    public Tree<T> addChild(T child) {
        var c = new Tree<T>(child);
        children.add(c);
        c.parent = this;
        return c;
    }

    public void removeParent() {
        parent = null;
    }

    public T getNode() {
        return node;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public Tree<T> getParent() {
        return parent;
    }

    /**
     * Recoge todos los nodos hoja de un árbol dado.
     *
     * @param node El nodo desde el cual comenzar la búsqueda.
     * @return Una lista de todos los nodos hoja bajo el nodo dado.
     */
    public List<Tree<T>> getLeafNodes(Tree<T> node) {
        List<Tree<T>> leaves = new ArrayList<>();
        collectLeafNodes(node, leaves);
        return leaves;
    }

    /**
     * Método auxiliar recursivo para recoger nodos hoja.
     *
     * @param node   El nodo actual en la recursión.
     * @param leaves La lista acumulativa de nodos hoja.
     */
    private void collectLeafNodes(Tree<T> node, List<Tree<T>> leaves) {
        if (node.getChildren().isEmpty()) {
            // Si no tiene hijos, es un nodo hoja
            leaves.add(node);
        } else {
            // De lo contrario, recorre recursivamente cada hijo
            for (Tree<T> child : node.getChildren()) {
                collectLeafNodes(child, leaves);
            }
        }
    }
}
