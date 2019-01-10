import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FibonacciHeap {

	int numNode; // number of nodes in the heap
	int maxVal;
	Node maxNode;

	public FibonacciHeap() {
		numNode = 0;
		maxVal = 0;
		maxNode = null;
	}

	// insert a node into root list
	public void insertHeap(Node x) {
		if (x.val <= 0) {
			throw new IllegalArgumentException("Insert node value must be greater than 0.");
		}
		// if the root list is not empty
		if (maxVal != 0) {
			// add new node to the left of max node
			x.right = maxNode;
			maxNode.left.right = x;
			x.left = maxNode.left;
			maxNode.left = x;
		}
		// if the insert value is max or the first insert
		if (x.val > maxVal) {
			maxVal = x.val;
			maxNode = x;
		}
		numNode++;
	}

	// extract the max node, meld its children into root list, and update max
	public Node extractMax() {
		// heap is not empty
		if (maxNode != null) {
			int maxDegree = maxNode.degree;
			Node maxRight = null;
			Node maxLeft = null;

			// max node has at least one child
			if (maxNode.child != null) {
				Node child = maxNode.child;
				for (int i = 0; i < maxDegree; i++) {
					child.parent = null;
					numNode--;
					Node temp = child.right; // current right node before insert
					child.left = null;
					child.right = null;
					insertHeap(child);
					child = temp;
				}
				maxNode.child = null;
			}

			// more than one node in root list
			if (maxNode.right != maxNode) {
				maxRight = maxNode.right;
				maxLeft = maxNode.left;
				maxLeft.right = maxRight;
				maxRight.left = maxLeft;
				// maxNode.right = maxNode;
				// maxNode.left = maxNode;
			}

			Node currentMaxNode = new Node(maxNode.keyword, maxNode.val);

			// update the max node and value
			if (maxRight != null) {
				maxNode = maxRight;
				maxVal = maxRight.val;
				Node currentNode = maxRight;
				while (currentNode.right != maxRight) {
					if (currentNode.right.val > maxVal) {
						maxVal = currentNode.right.val;
						maxNode = currentNode.right;
					}
					currentNode = currentNode.right;
				}
				consolidate();
			} else {
				maxVal = 0;
				maxNode = null;
			}
			numNode--;
			return currentMaxNode;
		}
		return null;  // if heap is empty
	}

	// consolidate heap so no two roots have the same degree
	private void consolidate() {
		ArrayList<Node> rootListNodes = getRootArray();
		ArrayList<Node> sameDegreeNodes = null;
		while ((sameDegreeNodes = findSameDegreeNodes(rootListNodes)) != null) {
			heapLink(sameDegreeNodes.get(0), sameDegreeNodes.get(1));
			rootListNodes = getRootArray();
		}
	}

	// add root list nodes into an arraylist
	private ArrayList<Node> getRootArray() {
		ArrayList<Node> rootListNodes = new ArrayList<Node>();
		Node temp = maxNode;
		do {
			rootListNodes.add(temp);
			temp = temp.right;
		} while (temp != maxNode);

		return rootListNodes;
	}
	
	// in the root list, find two nodes with the same degree
	private ArrayList<Node> findSameDegreeNodes(ArrayList<Node> rootListNodes) {
		ArrayList<Node> sameDegreeNodes = new ArrayList<Node>();

		Collections.sort(rootListNodes, new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				return n1.degree - n2.degree;
			}
		});

		int currentDegree = -1;
		Node previousNode = null;
		for (Node n : rootListNodes) {
			if (n.degree == currentDegree) {
				sameDegreeNodes.add(previousNode);
				sameDegreeNodes.add(n);
				return sameDegreeNodes;
			} else {
				currentDegree = n.degree;
				previousNode = n;
			}
		}
		return null;
	}

	// combine the two trees with the same degree
	private Node heapLink(Node x, Node y) {
		Node large = null;
		Node small = null;
		
		// current maxNode cannot add as a child
		if (x.val == y.val && x.val == maxVal) {
			if (x == maxNode) {
				large = x;
				small = y;
			} else {
				large = y;
				small = x;
			}
		} else if (x.val >= y.val) {
			large = x;
			small = y;
		} else {
			large = y;
			small = x;
		}

		// remove small from root list
		Node l = small.left;
		Node r = small.right;
		l.right = r;
		r.left = l;
		// make small a child of large
		small.parent = large;
		if (large.child != null) {
			Node temp = large.child.left;
			large.child.left = small;
			temp.right = small;
			small.left = temp;
			small.right = large.child;
		} else {
			large.child = small;
			small.right = small;
			small.left = small;
		}

		large.degree++;
		small.childcut = false; // no other places to set childcut to false??
		return large;
	}

	// increase the value of a node
	public void increaseKey(Node x, int i) {
		int k = x.val + i;
		if (k < x.val) {
			throw new IllegalArgumentException("New value is smaller than old value.");
		}
		// if node is in root list
		if (x.parent == null) {
			x.val = k;
			if (k > maxVal) {
				maxVal = k;
				maxNode = x;
			}
		} else {
			// heap order not violated, just increase the value
			if (k <= x.parent.val) {
				x.val = k;
			} else {
				x.val = k;
				Node p = x.parent;
				cut(p, x);
				cascadingCut(p);
			}
		}
	}

	// cut the tree
	private void cut(Node p, Node x) {
		p.degree--;
		x.parent = null;
		// parent pointer point to this node
		if (p.child == x) {
			if (x.right != x) {
				p.child = x.right;
				x.right.left = x.left;
				x.left.right = x.right;
			} else {
				p.child = null;
			}
		} else {
			x.right.left = x.left;
			x.left.right = x.right;
		}

		x.left = x;
		x.right = x;
		x.childcut = false;
		numNode--;
		insertHeap(x);
	}

	// if the parent node's childcut is true
	private void cascadingCut(Node p) {
		Node parent = p.parent;
		if (parent != null) {
			if (p.childcut) {
				cut(parent, p);
				cascadingCut(parent);
			} else {
				p.childcut = true;
			}
		}
		// won't change childcut if p is a root
	}

	
	// Node forms the heap
	public class Node {
		String keyword;
		int degree;
		int val;
		Node parent;
		Node child;
		Node left;
		Node right;
		Boolean childcut;

		public Node(String w, int val) {
			keyword = w;
			degree = 0; // number of children
			this.val = val;
			parent = null;
			child = null;
			left = this;
			right = this;
			childcut = false;
		}
	}
}
