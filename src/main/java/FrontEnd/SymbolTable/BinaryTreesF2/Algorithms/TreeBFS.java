package FrontEnd.SymbolTable.BinaryTreesF2.Algorithms;

import Auxiliar.MyQueue;
import BinaryTreesF2.Entities.Symbol;
import BinaryTreesF2.Entities.SymbolTableNode;

public class TreeBFS {
	public static Symbol findCitizenById (SymbolTableNode rootSymbolTableNode, long citizenId) {

		// Just search for nodes if there are any in the tree.
		if (rootSymbolTableNode != null) {
			MyQueue<SymbolTableNode> myQueue = new MyQueue<>();
			myQueue.add(rootSymbolTableNode);

			while (!myQueue.isEmpty()) {
				SymbolTableNode newSymbolTableNode = myQueue.poll();

				// Check if newNode has the same ID.
				for (Symbol nodeSymbol : newSymbolTableNode.getCitizens()) {
					if (nodeSymbol.sameID(citizenId)) {
						return nodeSymbol;
					}
				}

				// Add left child to the queue.
				if (newSymbolTableNode.left != null) {
					myQueue.add(newSymbolTableNode.left);
				}

				// Add right child to the queue.
				if (newSymbolTableNode.right != null) {
					myQueue.add(newSymbolTableNode.right);
				}
			}
		}

		return null;
	}
}
