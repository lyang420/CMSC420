package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/* @author Lucas Yang
 * CMSC420 0201
 * Fall 2022
 * 
 */

/* Implementation of extended kd-tree containing points of two dimensions. */
public class XkdTree<LPoint extends LabeledPoint2D> {
	
	/* An extended kd-tree's nodes may be internal, which splits values
	 * based on cutting dimension, or external, which contains points. */
	private abstract class Node {
		/* Constructor for a node. */
		private Node() {
			
		}
		
		/* Operations the kd-tree supports includes finding a point, inserting
		 * one or many points at once, returning a list of its contents,
		 * locating one or several of a points nearest neighbors, and deleting
		 * a point from the tree. */
		abstract LPoint find(Point2D pt);
		abstract Node bulkInsert(ArrayList<LPoint> pts, Rectangle2D bbox, int bucketSize) throws Exception;
		abstract ArrayList<String> list(ArrayList<String> lst);
		abstract LPoint nearestNeighbor(Point2D center, Rectangle2D cell, LPoint best);
		abstract Node deleteHelper(Point2D pt);
		abstract void kNNHelper(Point2D q, Rectangle2D cell, MinK<Double, LPoint> minK);
	}
	
	/* Implementation for an internal node of the extended kd-tree. */
	private class InternalNode extends Node {
		/* An internal node contains cutting dimension, which can be 0 or 1
		 * (X or Y coordinate, respectively), cutting value, and references
		 * to its left and right subtrees. */
		int cutDim;
		double cutVal;
		Node left, right;
		
		/* Constructor to create a new internal node with provided arguments. */
		InternalNode(int cutDim, double cutVal, Node left, Node right) {
			super();
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.left = left;
			this.right = right;
		}
		
		/* When finding a point, an internal node looks at the point's cutting
		 * value, and makes a recursive call to either the left or right
		 * subtree based on the cutting dimension. */
		LPoint find(Point2D q) {
			/* If the point's value is less than the current node's
			 * cutting value, call to the left subtree, and otherwise, to the
			 * right subtree. However, if it is equal, the point may live
			 * in either the left OR right subtree, so we must handle that case
			 * as well. */
			if ((this.cutDim == 0 && q.getX() < this.cutVal) || (this.cutDim == 1 && q.getY() < this.cutVal)) {
				return this.left.find(q);
			} else if ((this.cutDim == 0 && q.getX() > this.cutVal) || (this.cutDim == 1 && q.getY() > this.cutVal)) {
				return this.right.find(q);
			} else {
				LPoint res = this.left.find(q);
				if (res == null) {
					res = this.right.find(q);
				}
				return res;
			}
		}
		
		/* When inserting many points at once, we must keep in mind that the
		 * kd-tree has a bucket size, or maximum number of points that can
		 * be contained in a single external node. With this in mind, we have
		 * to call the insertion operation on the left and right subtree by
		 * splitting the list of points to be inserted such that each subtree
		 * receives points either less than or equal to, or greater than the
		 * cutting value. */
		Node bulkInsert(ArrayList<LPoint> pts, Rectangle2D bbox, int bucketSize) throws Exception {
			int splitIndex = 0;
			if (this.cutDim == 0) {
				Collections.sort(pts, new ByXThenY());
				for (LPoint point : pts) {
					if (point.getPoint2D().getX() >= this.cutVal) {
						break;
					}
					splitIndex++;
				}
			} else {
				Collections.sort(pts, new ByYThenX());
				for (LPoint point : pts) {
					if (point.getPoint2D().getY() >= this.cutVal) {
						break;
					}
					splitIndex++;
				}
			}
			this.left = left.bulkInsert(new ArrayList<LPoint>(pts.subList(0, splitIndex)), bbox, bucketSize);
			this.right = right.bulkInsert(new ArrayList<LPoint>(pts.subList(splitIndex, pts.size())), bbox, bucketSize);
			return this;
		}
		
		/* Return a list of the contents of the kd-tree in accordance with
		 * instruction specifications. */
		ArrayList<String> list(ArrayList<String> lst) {
			if (this.cutDim == 0) {
				lst.add("(x=" + this.cutVal + ")");
			} else {
				lst.add("(y=" + this.cutVal + ")");
			}
			this.right.list(lst);
			this.left.list(lst);
			return lst;
		}
		
		/* Return a point's "nearest neighbor" by partitioning the current
		 * "rectangle" into two parts, and calling the function recursively
		 * on either the left or right subtree based on whether it is possible
		 * for it to contain the nearest neighbor. This operation is efficient
		 * because we keep track of a "best" point, or the point closest
		 * to our target so far. */
		LPoint nearestNeighbor(Point2D center, Rectangle2D cell, LPoint best) {
			Rectangle2D leftCell = cell.leftPart(this.cutDim, this.cutVal);
			Rectangle2D rightCell = cell.rightPart(this.cutDim, this.cutVal);
			
			if (this.cutDim == 0) {
				if (center.getX() < this.cutVal) {
					best = this.left.nearestNeighbor(center, leftCell, best);
					if (rightCell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
						best = this.right.nearestNeighbor(center, rightCell, best);
					}
				} else {
					best = this.right.nearestNeighbor(center, rightCell, best);
					if (leftCell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
						best = this.left.nearestNeighbor(center, leftCell, best);
					}
				}
			} else {
				if (center.getY() < this.cutVal) {
					best = this.left.nearestNeighbor(center, leftCell, best);
					if (rightCell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
						best = this.right.nearestNeighbor(center, rightCell, best);
					}
				} else {
					best = this.right.nearestNeighbor(center, rightCell, best);
					if (leftCell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
						best = this.left.nearestNeighbor(center, leftCell, best);
					}
				}
			}
			
			return best;
		}
		
		/* When deleting a point from a kd-tree, we need to modify nodes in case
		 * deletion results in an external node being empty. If this is the case,
		 * then that empty node is unlinked from the tree, with its grandparent
		 * pointing to its sibling. */
		Node deleteHelper(Point2D pt) {
			if (this.left.find(pt) != null) {
				this.left = this.left.deleteHelper(pt);
				if (this.left == null) {
					return this.right;
				}
			}
			if (this.right.find(pt) != null) {
				this.right = this.right.deleteHelper(pt);
				if (this.right == null) {
					return this.left;
				}
			}
			return this;
		}
		
		/* Calculating k nearest neighbors is conceptually similar to the
		 * single neighbor operation above, except that we must keep track
		 * of several "best" or "nearest" points in a max heap, which we
		 * implement using the MinK data structure. */
		void kNNHelper(Point2D q, Rectangle2D cell, MinK<Double, LPoint> minK) {
			if (cell.distanceSq(q) > minK.getKth()) {
				return;
			}
			
			Rectangle2D leftCell = cell.leftPart(this.cutDim, this.cutVal);
			Rectangle2D rightCell = cell.rightPart(this.cutDim, this.cutVal);
			
			if (q.get(this.cutDim) < this.cutVal) {
				this.left.kNNHelper(q, leftCell, minK);
				this.right.kNNHelper(q, rightCell, minK);
			} else {
				this.right.kNNHelper(q, rightCell, minK);
				this.left.kNNHelper(q, leftCell, minK);
			}
		}
	}
	
	/* Implementation for an external node of the extended kd-tree. */
	private class ExternalNode extends Node {
		/* An external node contains a list of points. */
		ArrayList<LPoint> points;
		
		/* Constructor to create a new external node. */
		ExternalNode() {
			super();
			this.points = new ArrayList<LPoint>();
		}
		
		/* Returns the point if it is contained within the current node, or null
		 * if it is not found. */
		LPoint find(Point2D pt) {
			LPoint res = null;
			for (LPoint point : points) {
				if (point.getPoint2D().equals(pt)) {
					res = point;
					break;
				}
			}
			return res;
		}
		
		/* Inserts several points into the external node. If there are more
		 * nodes than allowed as specified by the kd-tree's bucket size, split
		 * the list of points into two halves based on the width of their
		 * distribution, set the current node to be an internal node, with its
		 * left and right subtrees being new external nodes, and recursively
		 * call the insertion operation on the left and right subtrees. */
		Node bulkInsert(ArrayList<LPoint> pts, Rectangle2D bbox, int bucketSize) throws Exception {
			this.points.addAll(pts);
			if (this.points.size() > bucketSize) {
				Collections.sort(this.points, new ByXThenY());
				Rectangle2D rect = new Rectangle2D();
				for (LPoint point : this.points) {
					rect.expand(point.getPoint2D());
				}
				int cutDim = 0, m = this.points.size() / 2;
				double cutValue = 0.0;
				if (rect.getWidth(0) < rect.getWidth(1)) {
					cutDim = 1;
				}
				if (cutDim == 0) {
					cutValue = this.points.get(m).getX();
					if (this.points.size() % 2 == 0) {
						cutValue = (this.points.get(m - 1).getX() + this.points.get(m).getX()) / 2;
					}
				} else {
					Collections.sort(this.points, new ByYThenX());
					cutValue = this.points.get(m).getY();
					if (this.points.size() % 2 == 0) {
						cutValue = (this.points.get(m - 1).getY() + this.points.get(m).getY()) / 2;
					}
				}
				ArrayList<LPoint> L = new ArrayList<LPoint>(this.points.subList(0, m));
				ArrayList<LPoint> R = new ArrayList<LPoint>(this.points.subList(m, this.points.size()));
				Node left = new ExternalNode(), right = new ExternalNode();
				Node newNode = new InternalNode(cutDim, cutValue, left.bulkInsert(L, bbox, bucketSize), right.bulkInsert(R, bbox, bucketSize));
				return newNode;
			} else {
				return this;
			}
		}
	
		/* Return a list of the contents of the kd-tree in accordance with
		 * instruction specifications. */
		ArrayList<String> list(ArrayList<String> lst) {
			Collections.sort(this.points, new ByLabel());
			String res = "[";
			for (LPoint point : this.points) {
				res += " {" + point.toString() + "}";
			}
			if (res.equals("[ ")) {
				res += "]";
			} else {
				res += " ]";
			}
			lst.add(res);
			return lst;
		}
		
		/* Return a point's "nearest neighbor" by looking at all points in
		 * the current external node and checking if any are closer than
		 * the current "best" point. */
		LPoint nearestNeighbor(Point2D center, Rectangle2D cell, LPoint best) {
			double distanceToBest = Double.MAX_VALUE;
			if (best != null) {
				distanceToBest = center.distanceSq(best.getPoint2D());
			}
			for (LPoint point : this.points) {
				if (center.distanceSq(point.getPoint2D()) < distanceToBest) {
					best = point;
					distanceToBest = center.distanceSq(best.getPoint2D());
				}
			}
			return best;
		}
		
		/* Delete a point from an external node by removing it from the list
		 * of points. If the current external node is emptied, return a null
		 * reference so that its parent, the internal node, can be restructured
		 * accordingly. */
		Node deleteHelper(Point2D pt) {
			for (LPoint point : this.points) {
				if (point.getPoint2D().equals(pt)) {
					this.points.remove(point);
					break;
				}
			}
			if (this.points.size() == 0) {
				return null;
			}
			return this;
		}
		
		/* When calculating k nearest neighbors of a point, an external node
		 * adds all of its points to the minK data structure. MinK will handle
		 * the operation of calculating whether any of the new points are
		 * eligible to be considered k nearest. */
		void kNNHelper(Point2D q, Rectangle2D cell, MinK<Double, LPoint> minK) {
			for (LPoint point : this.points) {
				minK.add(point.getPoint2D().distanceSq(q), point);
			}
		}
	}
	
	/* Comparator to sort a list of points based on its X-coordinate. */
	private class ByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if (pt1.getX() > pt2.getX()) {
				return 1;
			} else if (pt1.getX() < pt2.getX()) {
				return -1;
			} else {
				if (pt1.getY() > pt2.getY()) {
					return 1;
				} else if (pt1.getY() < pt2.getY()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	/* Comparator to sort a list of points based on its Y-coordinate. */
	private class ByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if (pt1.getY() > pt2.getY()) {
				return 1;
			} else if (pt1.getY() < pt2.getY()) {
				return -1;
			} else {
				if (pt1.getX() > pt2.getX()) {
					return 1;
				} else if (pt1.getX() < pt2.getX()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	/* Comparator to sort a list of points based on its label. */
	private class ByLabel implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			return pt1.getLabel().compareTo(pt2.getLabel());
		}
	}
	
	/* An extended kd-tree has a root, a size, a bucket size, and a bounding
	 * box, which is represented by a two-dimensional Rectangle object. */
	private Node root;
	private int size;
	private int bucketSize;
	private Rectangle2D bbox;

	/* Constructor for an extended kd-tree creates a new kd-tree with a single
	 * empty external node, and sets its bucket size and bounding box to the
	 * provided values. */
	public XkdTree(int bucketSize, Rectangle2D bbox) {
		this.root = new ExternalNode();
		this.size = 0;
		this.bucketSize = bucketSize;
		this.bbox = bbox;
	}
	
	/* Clear the extended kd-tree by removing all its contents and returning it
	 * to its default state. */
	public void clear() {
		this.root = new ExternalNode();
		this.size = 0;
	}
	
	/* Return the number of points contained within the kd-tree. */
	public int size() {
		return this.size;
	}
	
	/* Return the provided point, if it is found in the kd-tree, or null, if it
	 * is not. */
	public LPoint find(Point2D q) {
		return root.find(q);
	}
	
	/* Insert the provided point into the kd-tree, throwing an exception if a
	 * point is outside of the bounding box. Inserting a single point is
	 * functionally the same as calling bulkInsert on a list of just the one
	 * point. */
	public void insert(LPoint pt) throws Exception {
		ArrayList<LPoint> lst = new ArrayList<LPoint>();
		lst.add(pt);
		this.bulkInsert(lst);
	}
	
	/* Insert provided list of points into the extended kd-tree, throwing an
	 * exception if a point is outside of the bounding box. */
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception {
		if (pts.size() > 0) {
			Collections.sort(pts, new ByXThenY());
			if ((pts.get(0).getX() < bbox.getLow().getX()) || (pts.get(pts.size() - 1).getX() > bbox.getHigh().getX())) {
				throw new Exception("Attempt to insert a point outside bounding box");
			}
			Collections.sort(pts, new ByYThenX());
			if ((pts.get(0).getY() < bbox.getLow().getY()) || (pts.get(pts.size() - 1).getY() > bbox.getHigh().getY())) {
				throw new Exception("Attempt to insert a point outside bounding box");
			}
			this.root = this.root.bulkInsert(pts, this.bbox, this.bucketSize);
			/* Remember to increment the size property of the tree after
			 * successful insertion. */
			this.size += pts.size();
		}
	}
	
	/* Return a list of the tree's contents in accordance with instructions. */
	public ArrayList<String> list() {
		ArrayList<String> res = new ArrayList<String>();
		return this.root.list(res);
	}
	
	/* Returns the point closest to the given point, or null if the tree is
	 * empty. */
	public LPoint nearestNeighbor(Point2D center) {
		if (this.size == 0) {
			return null;
		}
		return this.root.nearestNeighbor(center, bbox, null);
	}

	/* Deletes the given point from the tree, throwing an exception if is not
	 * already in the tree. */
	public void delete(Point2D pt) throws Exception {
		if (this.find(pt) == null) {
			throw new Exception("Deletion of nonexistent point");
		} else {
			if (this.size == 1) {
				this.root = new ExternalNode();
			} else {
				this.root = this.root.deleteHelper(pt);
			}
			/* Remember to decrement the size property of the tree after
			 * successful deletion. */
			this.size--;
		}
	}
	
	/* Return a list of k points closest to the point center given in the
	 * arguments with the help of the MinK data structure. */
	public ArrayList<LPoint> kNearestNeighbor(Point2D center, int k) {
		ArrayList<LPoint> res = new ArrayList<LPoint>();
		if (this.size > 0) {
			MinK<Double, LPoint> kNN = new MinK<Double, LPoint>(k, Double.MAX_VALUE);
			this.kNNHelper(center, this.bbox, kNN);
			res = kNN.list();
		}
		return res;
	}
	
	/* Private helper function which invokes the k nearest neighbors function. */
	private void kNNHelper(Point2D q, Rectangle2D cell, MinK<Double, LPoint> minK) {
		this.root.kNNHelper(q, cell, minK);
	}
}