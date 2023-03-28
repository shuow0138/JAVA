package cmsc420_s22;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class EMSTree<LPoint extends LabeledPoint2D> {

	private ArrayList<LPoint> pointList;
	private HashSet<LPoint> inEMST;
	private ArrayList<Pair<LPoint>> edgeList;
	private MinimalKdTree<LPoint> kdTree;
	private QuakeHeap<Double, Pair<LPoint>> heap;
	private HashMap<LPoint, ArrayList<LPoint>> dependents;
	private int ptsinset;
	private Rectangle2D rec;

	public EMSTree(Rectangle2D bbox) {
		pointList = new ArrayList<LPoint>();
		inEMST = new HashSet<LPoint>();
		edgeList = new ArrayList<Pair<LPoint>>();
		kdTree = new MinimalKdTree<LPoint>(bbox);
		heap = new QuakeHeap<Double, Pair<LPoint>>(10);
		dependents = new HashMap<LPoint, ArrayList<LPoint>>();
		ptsinset = 0;
		rec = bbox;
	}

	public void addPoint(LPoint pt) throws Exception {
		if (pt != null) {
			pointList.add(pt);
			dependents.put(pt, new ArrayList<LPoint>());
			ptsinset++;
			kdTree.insert_adding(pt);
			LPoint nn = kdTree.nearestNeighbor(pt, kdTree.getroot(), rec, null);
			if (nn != null) {
				Pair<LPoint> nnpair = new Pair<LPoint>(pt, nn);
				heap.insert(pt.getPoint2D().distanceSq(nn.getPoint2D()), nnpair);
			}
		}
	}

	public void clear() {
		edgeList.clear();
		inEMST.clear();
		heap.clear();
		dependents.clear();
		kdTree.clear();
		pointList.clear();
		ptsinset = 0;
	}

	public int size() {
		return ptsinset;
	}

	public ArrayList<String> buildEMST(LPoint start) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> record = new ArrayList<String>();
		ArrayList<String> record2;

		if (!rec.contains(start.getPoint2D())) {
			throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			initializeEMST(start);
			LPoint nn = kdTree.nearestNeighbor(start, kdTree.getroot(), rec, null);
			if (nn == null) {
				return new ArrayList<String>();
			}

			String line1 = "new-nn: (" + start.getLabel() + "->" + nn.getLabel() + ")";
			record.add(line1);
			result.add(line1);
			addNearNeighbor(start, nn);
			String line2;

			while (kdTree.size() != 0) {
				record2 = new ArrayList<String>();
				line2 = null;
				if (heap == null) {
					return new ArrayList<String>();
				}
				Pair<LPoint> edge = heap.extractMin();
				LPoint pt2 = edge.getSecond();
				if (!inEMST.contains(pt2)) {
					addEdge(edge);
					line2 = "add: " + edge + " new-nn:";
				}

				for (LPoint pt : inEMST) {
					String line3;
					LPoint nn2 = kdTree.nearestNeighbor(pt, kdTree.getroot(), rec, null);
					if (nn2 != null) {
						line3 = " (" + pt.getLabel() + "->" + nn2.getLabel() + ")";
						if (!record.contains(line3)) {
							record2.add(line3);
						}
					}
				}
				Collections.sort(record2);
				for (String str : record2) {
					record.add(str);
					line2 = line2.concat(str);
				}
				if (line2 != null)
					result.add(line2);
			}
			return result;
		}
	}

	void initializeEMST(LPoint start) throws Exception {
		edgeList.clear();
		inEMST.clear();
		heap.clear();
		dependents.replaceAll((k, v) -> new ArrayList<LPoint>());
		kdTree.clear();
		for (LPoint pt : pointList) {
			if (pt != start)
				kdTree.insert(pt);
		}
		inEMST.add(start);
	}

	void addEdge(Pair<LPoint> edge) throws Exception {
		LPoint pt2 = edge.getSecond();

		edgeList.add(edge);
		inEMST.add(pt2);
		kdTree.delete(pt2);
		ArrayList<LPoint> dep2 = dependents.get(pt2);
		dep2.add(pt2);
		for (LPoint pt3 : dep2) {
			LPoint nn3 = kdTree.nearestNeighbor(pt3, kdTree.getroot(), rec, null);
			if (nn3 == null)
				break;
			addNearNeighbor(pt3, nn3);
		}
	}

	private double distance(LPoint pt, LPoint nn) {
		return Math.pow(pt.getX() - nn.getX(), 2) + Math.pow(pt.getY() - nn.getY(), 2);
	}

	private void addNearNeighbor(LPoint pt, LPoint nn) {
		double dist = distance(pt, nn);
		Pair<LPoint> pair = new Pair<LPoint>(pt, nn);

		heap.insert(dist, pair);
		dependents.get(nn).add(pt);
	}

	public ArrayList<String> listEMST() {
		ArrayList<String> result = new ArrayList<String>();

		for (Pair<LPoint> e : edgeList) {
			String str = "(" + e.getFirst().getLabel() + "," + e.getSecond().getLabel() + ")";
			result.add(str);
		}
		return result;
	}

}
