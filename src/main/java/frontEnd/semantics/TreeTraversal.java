package frontEnd.semantics;

import frontEnd.sintaxis.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TreeTraversal {
	// Utility function to check if a node has a specific child type
	public static <T> boolean hasSpecificChildType(Tree<T> node, String childType) {
		if (node.getNode().equals(childType)) {
			return true;
		}
		for (Tree<T> child : node.getChildren()) {
			if (hasSpecificChildType(child, childType)) {
				return true;
			}
		}
		return false;
	}

	// Function used to navigate through the whole tree using DFS approach and get only the leaf nodes (which are the tokens).
	public static <T> List<T> getLeafNodesIterative(Tree<T> root) {
		List<T> leaves = new ArrayList<>();
		Stack<Tree<T>> stack = new Stack<>();
		stack.push(root);

        // Continue until all nodes have been processed
        while (!stack.isEmpty()) {
            Tree<T> node = stack.pop();

            // If the node has no children, it is a leaf node.
            if (node.getChildren().isEmpty()) {
                leaves.add(node.getNode());
            } else {
                // Push all children of the current node to the stack.
                stack.addAll(node.getChildren());
            }
        }
        return leaves;
    }
}