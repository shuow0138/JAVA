package cmsc420_s22;

/**
 * A minimal kd-tree. This is just a plain kd-tree. If you add a function for
 * nearest-neighbor searching, it can be used for Programming Assignment 3.
 */

public class MinimalKdTree<LPoint extends LabeledPoint2D> {

	// =================================================================
	// KDNode
	// =================================================================

	private class KDNode {

		private LPoint point; // the associated point
		private int cutDim; // cutting dimension (0 == x, 1 == y)
		private KDNode left, right; // children

		public KDNode(LPoint point, int cutDim) { // leaf constructor
			this.point = point;
			this.cutDim = cutDim;
			left = right = null;
		}

		boolean onLeft(LPoint pt) { // in the left subtree? (for Labeled points)
			return pt.get(cutDim) < point.get(cutDim);
		}

		boolean onLeft(Point2D pt) { // in the left subtree? (for points)
			return pt.get(cutDim) < point.get(cutDim);
		}

		public String toString() { // string representation
			String cut = (cutDim == 0 ? "x" : "y");
			return "(" + cut + "=" + point.get(cutDim) + point.toString();
		}
	}

	// -----------------------------------------------------------------
	// Recursive helpers for main functions
	// -----------------------------------------------------------------

	/**
	 * Finds a point in the node's subtree.
	 */
	LPoint find(KDNode p, Point2D pt) { // find point in subtree
		if (p == null) {
			return null;
		} else if (p.point.getPoint2D().equals(pt)) {
			return p.point;
		} else if (p.onLeft(pt)) {
			return find(p.left, pt);
		} else {
			return find(p.right, pt);
		}
	}

	/**
	 * Insert a point in the node's subtree. Uses the standard alternating cutting
	 * dimension rule.
	 */
	KDNode insert(LPoint pt, KDNode p, int cd) throws Exception {
		if (p == null) {
			return new KDNode(pt, cd);
		} else if (pt.getPoint2D().equals(p.point.getPoint2D())/* &&pt.getLabel()==p.point.getLabel() */) {
			throw new Exception("Attempt to insert a duplicate point");
		} else if (p.onLeft(pt)) { // insert on appropriate side
			p.left = insert(pt, p.left, 1 - cd);
		} else {
			p.right = insert(pt, p.right, 1 - cd);
		}
		return p;
	}

	KDNode insert_adding(LPoint pt, KDNode p, int cd) {
		if (p == null) {
			return new KDNode(pt, cd);
		} else if (pt.getPoint2D().equals(p.point.getPoint2D())/* &&pt.getLabel()==p.point.getLabel() */) {
			// throw new Exception("Attempt to insert a duplicate point");
		} else if (p.onLeft(pt)) { // insert on appropriate side
			p.left = insert_adding(pt, p.left, 1 - cd);
		} else {
			p.right = insert_adding(pt, p.right, 1 - cd);
		}
		return p;
	}

	/**
	 * Delete a point from node's subtree.
	 */
	KDNode delete(Point2D pt, KDNode p) throws Exception {
		if (p == null) { // fell out of tree?
			throw new Exception("Attempt to delete a nonexistent point");
		} else if (pt.equals(p.point.getPoint2D())) { // found it
			if (p.right != null) { // can replace from right
				p.point = findMin(p.right, p.cutDim); // find and copy replacement
				p.right = delete(p.point.getPoint2D(), p.right); // delete from right
			} else if (p.left != null) { // can replace from left
				p.point = findMin(p.left, p.cutDim); // find and copy replacement
				p.right = delete(p.point.getPoint2D(), p.left); // delete left but move to right!!
				p.left = null; // left subtree is now empty
			} else { // deleted point in leaf
				p = null; // remove this leaf
			}
		} else if (p.onLeft(pt)) {
			p.left = delete(pt, p.left); // delete from left subtree
		} else { // delete from right subtree
			p.right = delete(pt, p.right);
		}
		return p;
	}

	/**
	 * Find min node in subtree along coordinate i.
	 */
	LPoint findMin(KDNode p, int i) {
		if (p == null) { // fell out of tree?
			return null;
		} else if (p.cutDim == i) { // cutting dimension matches i?
			if (p.left == null) { // no left child?
				return p.point; // use this point
			} else {
				return findMin(p.left, i); // get min from left subtree
			}
		} else { // check both sides and this point as well
			return min(i, p.point, min(i, findMin(p.left, i), findMin(p.right, i)));
		}
	}

	/**
	 * Return the minimum non-null point w.r.t. coordinate i.
	 */
	LPoint min(int i, LPoint pt1, LPoint pt2) {
		if (pt1 == null) {
			return pt2;
		} else if (pt2 == null) {
			return pt1;
		} else if (pt1.get(i) < pt2.get(i)) {
			return pt1;
		} else {
			return pt2;
		}
	}

	// -----------------------------------------------------------------
	// Private data
	// -----------------------------------------------------------------

	private KDNode root; // root of the tree
	private int nPoints; // number of points in the tree
	private Rectangle2D bbox; // the bounding box

	// -----------------------------------------------------------------
	// Public members
	// -----------------------------------------------------------------

	/**
	 * Creates an empty tree.
	 */
	public MinimalKdTree(Rectangle2D bbox) {
		root = null;
		nPoints = 0;
		this.bbox = new Rectangle2D(bbox);
	}

	/**
	 * Number of entries in the dictionary.
	 */
	public int size() {
		return nPoints;
	}

	/**
	 * Find an point in the tree.
	 */
	public LPoint find(Point2D pt) {
		return find(root, pt);
	}

	/**
	 * Insert a point
	 */
	public void insert(LPoint pt) throws Exception {
		if (!bbox.contains(pt.getPoint2D())) {
			throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			root = insert(pt, root, 0); // insert the point
		}
		nPoints += 1; // one more point
	}

	public void insert_adding(LPoint pt) {
		if (!bbox.contains(pt.getPoint2D())) {
			// throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			root = insert_adding(pt, root, 0); // insert the point
		}
		nPoints += 1; // one more point
	}

	/**
	 * Delete a point. Note that the point being deleted does not need to match
	 * fully. It suffices that it has enough information to satisfy the comparator.
	 */
	public void delete(Point2D pt) throws Exception {
		root = delete(pt, root); // delete the point
		nPoints -= 1; // one fewer point
	}

	/**
	 * Remove all items, resulting in an empty tree
	 */
	public void clear() {
		root = null;
		nPoints = 0;
	}

	Point2D nearestNeighbor(Point2D q, KDNode p, Rectangle2D cell, Point2D best) {
		if (p != null) {
			if (q.distanceSq((Point2D) p.point) < q.distanceSq(best))
				best = (Point2D) p.point;
			int cd = p.cutDim;
			Rectangle2D leftCell = cell.leftPart(cd, p.point.get(cd));
			Rectangle2D rightCell = cell.rightPart(cd, p.point.get(cd));
			if (q.get(cd) < p.point.get(cd)) {
				best = nearestNeighbor(q, p.left, leftCell, best);
				if (rightCell.distanceSq(q) < q.distanceSq(best)) { // is right viable?
					best = nearestNeighbor(q, p.right, rightCell, best);
				}
			} else { // q is closer to right
				best = nearestNeighbor(q, p.right, rightCell, best);
				if (leftCell.distanceSq(q) < q.distanceSq(best)) { // is left viable?
					best = nearestNeighbor(q, p.left, leftCell, best);
				}
			}
		}
		return best;
	}

	LPoint nearestNeighbor(LPoint q, KDNode p, Rectangle2D cell, LPoint best) {
		if (p != null) {
			Point2D best2D;
			if (best == null) {
				best2D = null;
			} else {
				best2D = best.getPoint2D();
			}
			if (q.getPoint2D().distanceSq(p.point.getPoint2D()) < q.getPoint2D().distanceSq(best2D))
				best = p.point;

			int cd = p.cutDim;
			Rectangle2D leftCell = cell.leftPart(cd, p.point.get(cd));
			Rectangle2D rightCell = cell.rightPart(cd, p.point.get(cd));
			if (q.get(cd) < p.point.get(cd)) {
				best = nearestNeighbor(q, p.left, leftCell, best);
				if (rightCell.distanceSq(q.getPoint2D()) < q.getPoint2D().distanceSq(best2D)) { // is right viable?
					best = nearestNeighbor(q, p.right, rightCell, best);
				}
			} else { // q is closer to right
				best = nearestNeighbor(q, p.right, rightCell, best);
				if (leftCell.distanceSq(q.getPoint2D()) < q.getPoint2D().distanceSq(best2D)) { // is left viable?
					best = nearestNeighbor(q, p.left, leftCell, best);
				}
			}
		}
		return best;
	}

	public KDNode getroot() {
		return root;
	}

	public LPoint nearestNeighbor(LPoint start) {
		return nearestNeighbor(start, root, bbox, null);
	}

	public void delete(LPoint pt2) throws Exception {
		root = delete(pt2.getPoint2D(), root); // delete the point
		nPoints -= 1; // one fewer point
	}
}
