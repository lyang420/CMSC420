import java.util.ArrayList;

/* Implementation of a meldable heap in Java containing key-value pairs, where keys are comparable to one another. */
public class LeftistHeap<Key extends Comparable<Key>, Value> {

	/* A node of a leftist heap contains a key, a value, references to left and right "subtrees," and a variable representing its "null path length," or shortest number of edges to a null reference. */
	class LHNode {
		Key key;
		Value value;
		LHNode left, right;
		int npl;

		/* I defined a private constructor for the node inner class. This is so we can create a new node to insert into an existing heap, since the insert function only takes a key and value as input arguments. */
		private LHNode(Key x, Value v) {
			this.key = x;
			this.value = v;
			this.left = null;
			this.right = null;
			this.npl = 0;
		}
	}

	/* Private member representing this heap's root node. */
	private LHNode root;

	/* Construct a new, empty heap. */
	public LeftistHeap() {
		this.root = null;
	}

	/* A heap is empty if the root is null (i.e., nothing inside). */
	public boolean isEmpty() {
		return this.root == null;
	}

	/* "Reset" the current heap. */
	public void clear() {
		this.root = null;
	}

	/* Insert a new key-value pair into the heap. This is can be done efficiently just by merging the current heap with a node containing the key-value pair we want to insert. */
	public void insert(Key x, Value v) {
		this.root = merge(this.root, new LHNode(x, v));
	}

	/* Merge two heaps. */
	public void mergeWith(LeftistHeap<Key, Value> h2) {
		/* We go through the process of merging only if the heap to be merged with isn't empty, and isn't the same as the current heap. */
		if (h2 == null || this == h2) {
			return;
		}
		this.root = merge(this.root, h2.root);
		
		/* Discard the heap that was merged. */
		h2.root = null;
	}

	/* Private recursive helper method for merging. */
	private LHNode merge(LHNode u, LHNode v) {
		if (u == null) {
			return v;
		}
		if (v == null) {
			return u;
		}
		if (u.key.compareTo(v.key) > 0) {
			LHNode temp = u;
			u = v;
			v = temp;
		}
		if (u.left == null) {
			u.left = v;
		} else {
			/* Maintain the property of the leftsubtree being strictly "heavier" than the right subtree. */
			u.right = merge(u.right, v);
			if (u.left.npl < u.right.npl) {
				LHNode temp = u.left;
				u.left = u.right;
				u.right = temp;
			}
			u.npl = u.right.npl + 1;
		}
		return u;
	}

	/* Split the current heap by a given key. */
	public LeftistHeap<Key, Value> split(Key x) {
		/* Create an empty list of nodes. */
		ArrayList<LHNode> L = new ArrayList<LHNode>();
		
		/* Perform a left-to-right pre-order traversal of the current heap. */
		this.root = traverseAndPopulate(L, this.root, x);
		
		/* Create a new empty heap h2, and merge each of the elements of L, which we just populated, into this new heap, from left to right. */
		LeftistHeap<Key, Value> h2 = new LeftistHeap<Key, Value>();
		
		/* It's only necessary to merge the nodes from L
		   to h2 if L contains anything to begin with. */
		if (L.size() > 0) {
			/* O(k log(n))*/
			for (int i = 0; i < L.size(); i++) {
				/* Unfortunately, I have to go through the trouble of creating a new heap and populating it with the node we're currently looking at before merging with h2.
				
				This is because the merge method only accepts a heap as an argument. In addition, we also want to make sure we're merging the sub-heap we're looking at, children nodes included, instead of just its key and value. */
				LeftistHeap<Key, Value> h3 = new LeftistHeap<Key, Value>();
				h3.root = L.get(i);
				h2.mergeWith(h3);
			}
			
			/* Uphold the leftist properties of the current heap, which may have been disrupted by the split. */
			this.root = fix(this.root);
		}
		return h2;
	}

	/* Private recursive helper method which extracts all nodes in the current heap greater than a specified value. */
	private LHNode traverseAndPopulate(ArrayList<LHNode> L, LHNode u, Key x) {
		/* Base case: If we reach a node pointing to null, return. */
		if (u == null) {
			return u;
		}
		
		/* When we visit a node u, if u.key <= x, leave the node unchanged and recursively traverse to left and right subtrees... */
		if (u.key.compareTo(x) <= 0) {
			u.left = traverseAndPopulate(L, u.left, x);
			u.right = traverseAndPopulate(L, u.right, x);
		
		/* ...otherwise, unlink this node from the current heap and append it to the end of the list L. */
		} else {
			L.add(u);
			u = null;
		}
		return u;
	}
	
	/* Private recursive helper method which rearranges the structure of a heap to make sure it is leftist. */
	private LHNode fix(LHNode u) {
		/* Base case: If we reach a node pointing to null, return. */
		if (u == null) {
			return u;
		}
		
		/* Make sure to visit EVERY node in a heap, because we need to make sure they all have the correct NPL values. */
		u.left = fix(u.left);
		u.right = fix(u.right);
		if (u.left == null || u.right == null) {
			u.npl = 0;
			
			/* If a node happens to have an NPL value of 0, that means at least one of its children point to null. If it's the case that only one of them point to null and the other one doesn't, the one pointing to null MUST be the right child, in line with the leftist heap property. */
			if (u.left == null && u.right != null) {
				u.left = u.right;
				u.right = null;
			}
		}
		
		/* Update NPL values. */
		if (u.left != null && u.right != null) {
			u.npl = Math.min(u.left.npl, u.right.npl) + 1;
			
			/* Update subtree structure to uphold leftist heap property. */
			if (u.left.npl < u.right.npl) {
				LHNode temp = u.left;
				u.left = u.right;
				u.right = temp;
				
			}
		}
		return u;
	}

	/* Return the minimum key in the heap, which, based on its structure, should be located at the root node. */
	public Key getMinKey() {
		if (this.isEmpty()) {
			return null;
		}
		return this.root.key;
	}

	/* Return the minimum key in the heap, and then remove it. */
	public Value extractMin() throws Exception {
		if (this.isEmpty()) {
			throw new Exception("Empty heap");
		}
		
		/* "Remove" the root node by setting the heap equal to the merge of the root's left and right subtrees. */
		Value v = this.root.value;
		this.root = merge(this.root.left, this.root.right);
		return v;
	}

	public ArrayList<String> list() {
		ArrayList<String> list = new ArrayList<String>();
		listHelper(this.root, list);
		return list;
	}

	private void listHelper(LHNode u, ArrayList<String> list) {
		if (u == null) {
			list.add("[]");
		} else {
			list.add("(" + u.key + ", " + u.value + ") [" + u.npl + "]");
			listHelper(u.right, list);
			listHelper(u.left, list);
		}
		return;
	}
}