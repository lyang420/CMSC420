package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/* @author Lucas Yang
 * CMSC420 0201
 * Fall 2022
 * 
 */

/* Implementation of MinK data structure containing key-value pairs, where
 * keys are comparable. When applied to the extended kd-tree to answer
 * the k nearest neighbors query, keys are doubles, representing squared
 * distances to a point, and values are labeled points. */
public class MinK<Key extends Comparable<Key>, Value> {
	
	/* MinK is implemented as a max heap storing key-value pairs. For
	 * abstraction purposes, its contents will be defined as Pair objects. */
	class Pair {
		Key key;
		Value value;
		
		/* Constructor for a Pair creates a new key-value pair from the
		 * given parameters. */
		private Pair(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
	}
	
	/* MinK stores its contents in a list-based heap for efficient indexing,
	 * a key representing the maximum possible key value, and the number
	 * of elements (k from the original query). */
	private ArrayList<Pair> heap;
	private Key maxKey;
	private int numElements;

	/* Constructor for MinK creates a new MinK by instantiating an empty
	 * heap with null in the first position (for 1-indexing), and setting
	 * maxKey and numElements to the provided values. */
	public MinK(int k, Key maxKey) {
		this.heap = new ArrayList<Pair>();
		this.heap.add(null);
		this.maxKey = maxKey;
		this.numElements = k;
	}
	
	/* Return the number of key-value pairs in MinK.*/
	public int size() {
		return this.heap.size() - 1;
	}
	
	/* Clear MinK by deleting all of its contents. */
	public void clear() {
		this.heap.clear();
		this.heap.add(null);
	}
	
	/* Return the kth greatest key in MinK, which should be the first element,
	 * or the maxKey value if MinK does not yet contain numElements elements. */
	public Key getKth() {
		if (this.size() == numElements) {
			return this.heap.get(1).key;
		}
		return this.maxKey;
	}
	
	/* Add a given key-value pair to MinK as in a max heap. */
	public void add(Key x, Value v) {
		/* If MinK does not yet contain numElements elements, append the
		 * key-value pair to the end of the list and sift it up to its
		 * correct position. */
		if (this.size() < this.numElements) {
			Pair newElement = new Pair(x, v);
			this.heap.add(newElement);
			int i = this.siftUp(this.size(), x);
			this.heap.set(i, newElement);
		/* If MinK already contains numElements elements, then replace the
		 * root element with the new key-value pair only if the new key
		 * is less than the root's key, and sift it down to its correct
		 * position. */
		} else {
			if (x.compareTo(this.heap.get(1).key) < 0) {
				Pair newElement = new Pair(x, v);
				this.heap.set(1, newElement);
				int i = this.siftDown(1, x);
				this.heap.set(i, newElement);
			}
		}
	}
	
	/* Returns an index i representing the position of a key x in a max heap
	 * after moving up the heap. */
	private int siftUp(int i, Key x) {
		/* In a max heap, all elements are greater than or equal to its
		 * children. */
		while (i > 1 && x.compareTo(this.heap.get(i/2).key) > 0) {
			this.heap.set(i, this.heap.get(i/2));
			i /= 2;
		}
		return i;
	}
	
	/* Returns an index i representing the position of a key x in a max heap
	 * after moving down the heap. */
	private int siftDown(int i, Key x) {
		while ((2 * i) <= this.numElements) {
			int u = (2 * i), v = ((2 * i) + 1);
			if (v <= this.numElements && this.heap.get(v).key.compareTo(this.heap.get(u).key) > 0) {
				u = v;
			}
			if (this.heap.get(u).key.compareTo(x) > 0) {
				this.heap.set(i, this.heap.get(u));
				i = u;
			} else {
				break;
			}
		}
		return i;
	}
	
	/* Return a list of values in MinK sorted by key. */
	public ArrayList<Value> list() {
		ArrayList<Pair> temp = new ArrayList<Pair>(this.heap);
		ArrayList<Value> res = new ArrayList<Value>();
		temp.remove(0);
		if (temp.size() > 0) {
			Collections.sort(temp, new ByKey());
			
			for (Pair pair : temp) {
				res.add(pair.value);
			}
		}
		return res;
	}
	
	/* Comparator to sort MinK contents based on key. */
	private class ByKey implements Comparator<Pair> {
		public int compare(Pair p1, Pair p2) {
			return p1.key.compareTo(p2.key);
		}
	}
}