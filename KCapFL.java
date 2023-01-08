import java.util.ArrayList;

/* Implementation of the k-capacitated facility locator, which, given a set of data points (referred to as "service
centers") and an integer k, locates service centers such that they can serve at most k customers and are located as
optimally as possible. */

public class KCapFL<LPoint extends LabeledPoint2D> {

	/* KCapFL contains an integer representing the number of points that can be within a certain radius, an extended
	kd-tree to store points, and a leftist heap to store key-value pairs representing the distances of pairs to a
	point. */

	private int capacity;
	private XkdTree<LPoint> kdTree;
	private LeftistHeap<Double, ArrayList<LPoint>> heap;

	/* Constructor for KCapFL sets the capacity to the provided value, creates an extended kd-tree with the given
	bucket size and bounding box, and a new empty leftist heap. */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) {
		this.capacity = capacity;
		this.kdTree = new XkdTree<LPoint>(bucketSize, bbox);
		this.heap = new LeftistHeap<Double, ArrayList<LPoint>>();
	}

	/* Clears the contents of KCapFL by invoking clear() on its extended kd-tree and leftist heap. */
	public void clear() {
		this.kdTree.clear();
		this.heap.clear();
	}

	/* Stores a given list of points in KCapFL by inserting them into the extended kd-tree and calculating the k
	nearest neighbors for each point to store the kth nearest neighbor in the leftist heap. */
	public void build(ArrayList<LPoint> pts) throws Exception {
		if (pts.size() <= 0 || pts.size() % this.capacity != 0) {
			throw new Exception("Invalid point set size");
		}
		this.kdTree.bulkInsert(pts);
		for (LPoint point : pts) {
			ArrayList<LPoint> kNearestNeighbor = this.kdTree.kNearestNeighbor(point.getPoint2D(), this.capacity);
			this.heap.insert(point.getPoint2D().distanceSq(kNearestNeighbor.get(capacity - 1).getPoint2D()), kNearestNeighbor);
		}
	}

	/* Returns a list representing a "cluster" of labeled points. */
	public ArrayList<LPoint> extractCluster() {
		if (this.kdTree.size() == 0) {
			return null;
		}
		try {
			ArrayList<LPoint> labeledPoints = new ArrayList<LPoint>(this.heap.extractMin());
			boolean success = true;
			for (LPoint point : labeledPoints) {
				if (this.kdTree.find(point.getPoint2D()) == null) {
					success = false;
					break;
				}
			}

			/* If the extended kd-tree contains the minimum key in the leftist heap, we simply return it. */

			if (success) {
				for (LPoint point : labeledPoints) {
					this.kdTree.delete(point.getPoint2D());
				}
				return labeledPoints;
				
			/* If the extended kd-tree does not contain every element in the list from above, but does contain the
			minimum element, create a new list of labeled points, radius, and corresponding leftist heap, and continue
			extracting clusters. */

			} else {
				LPoint c = labeledPoints.get(0);
				if (this.kdTree.find(c.getPoint2D()) != null) {
					ArrayList<LPoint> newLabeledPoints = this.kdTree.kNearestNeighbor(c.getPoint2D(), this.capacity);
					double newRadius = c.getPoint2D().distanceSq(newLabeledPoints.get(this.capacity - 1).getPoint2D());
					this.heap.insert(newRadius, newLabeledPoints);
				}
			}
		} catch (Exception e) {

			/* Theoretically, if we're doing everything right, we should never reach the catch clause, since we should
			never be trying to extract elements from an empty leftist heap. */

			System.out.println("You're doing something wrong.");
		}
		return this.extractCluster();
	}

	/* Returns a list representation of the extended kd-tree, for debugging purposes. */
	public ArrayList<String> listKdTree() {
		return this.kdTree.list();
	}

	/* Returns a list representation of the leftist heap, for debugging purposes. */
	public ArrayList<String> listHeap() {
		return this.heap.list();
	}
}