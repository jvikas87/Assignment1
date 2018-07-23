import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {

	private final Digraph G;

	private final int size;

	// constructor takes a digraph (not necessarily a DAG)
	public SAP(Digraph G) {
		this.G = new Digraph(G);
		size = G.V();
	}

	// length of shortest ancestral path between v and w; -1 if no such path
	public int length(int v, int w) {
		return computePair(v, w).distance;
	}

	private Pair computePair(int v, int w) {
		BreadthFirstDirectedPaths fromV = new BreadthFirstDirectedPaths(G, v);
		BreadthFirstDirectedPaths fromW = new BreadthFirstDirectedPaths(G, w);

		int minDistance = Integer.MAX_VALUE;
		int ancestor = -1;
		for (int vertexIndex = 0; vertexIndex < G.V(); vertexIndex++) {
			int distanceFromV = fromV.distTo(vertexIndex);
			int distanceFromW = fromW.distTo(vertexIndex);
			if (distanceFromV != Integer.MAX_VALUE && distanceFromW != Integer.MAX_VALUE) {
				int currentDistance = distanceFromV + distanceFromW;
				if (currentDistance < minDistance) {
					minDistance = currentDistance;
					ancestor = vertexIndex;
				}
			}
		}
		return new Pair(ancestor, minDistance == Integer.MAX_VALUE ? -1 : minDistance);
	}

	// a common ancestor of v and w that participates in a shortest ancestral path;
	// -1 if no such path
	public int ancestor(int v, int w) {
		return computePair(v, w).ancestor;
	}

	// length of shortest ancestral path between any vertex in v and any vertex in
	// w; -1 if no such path
	public int length(Iterable<Integer> v, Iterable<Integer> w) {

		if (v == null || w == null || !v.iterator().hasNext() || !w.iterator().hasNext()) {
			throw new IllegalArgumentException();
		}

		List<Integer> vList = getList(v);
		List<Integer> wList = getList(w);

		int distance = Integer.MAX_VALUE;
		for (Integer wEle : wList) {
			for (Integer vEle : vList) {
				int currentDistance = length(wEle, vEle);
				if (currentDistance < distance) {
					distance = currentDistance;
				}
			}
		}
		return distance;
	}

	private List<Integer> getList(Iterable<Integer> v) {
		List<Integer> list = new ArrayList<>();
		Iterator<Integer> it = v.iterator();
		while (it.hasNext()) {
			Integer ele = it.next();
			if (ele == null || ele >= size) {
				throw new IllegalArgumentException();
			}
			list.add(ele);
		}
		return list;
	}

	// a common ancestor that participates in shortest ancestral path; -1 if no such
	// path
	public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
		if (v == null || w == null || !v.iterator().hasNext() || !w.iterator().hasNext()) {
			throw new IllegalArgumentException();
		}

		List<Integer> vList = getList(v);
		List<Integer> wList = getList(w);

		int distance = Integer.MAX_VALUE;
		int ancestor = -1;
		for (Integer wEle : wList) {
			for (Integer vEle : vList) {
				Pair current = computePair(wEle, vEle);
				if (current.distance < distance && current.distance != -1) {
					distance = current.distance;
					ancestor = current.ancestor;
				}
			}
		}
		return ancestor;
	}

	// do unit testing of this class

	public static void main(String[] args) {
		In in = new In(args[0]);
		Digraph G = new Digraph(in);
		SAP sap = new SAP(G);
		while (!StdIn.isEmpty()) {
			int v = StdIn.readInt();
			int w = StdIn.readInt();
			int length = sap.length(v, w);
			int ancestor = sap.ancestor(v, w);
			StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
		}
	}

	private class Pair {
		private final int ancestor;
		private final int distance;

		public Pair(int ancestor, int distance) {
			this.ancestor = ancestor;
			this.distance = distance;
		}
	}

}
