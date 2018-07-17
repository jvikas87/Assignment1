import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;

public class WordNet {

	private final Map<Integer, Synset> synsetMap;

	private final Set<String> lookupNoun;

	private final Map<String, List<Synset>> lookupSynsetForNoun;

	// constructor takes the name of the two input files
	public WordNet(String synsets, String hypernyms) {
		if (synsets == null || hypernyms == null) {
			throw new IllegalArgumentException();
		}
		synsetMap = new HashMap<>();
		lookupNoun = new HashSet<>();
		lookupSynsetForNoun = new HashMap<>();

		In in = new In(synsets);
		while (in.hasNextLine()) {
			String temp = in.readLine();
			String[] array = temp.split(",");
			Integer synsetKey = Integer.valueOf(array[0]);
			List<String> nounList = Arrays.asList(array[1].split(" "));
			Synset synset = new Synset(synsetKey, nounList);
			synsetMap.put(synsetKey, synset);
			for (String noun : synset.list) {
				lookupNoun.add(noun);
				if (!lookupSynsetForNoun.containsKey(noun)) {
					lookupSynsetForNoun.put(noun, new ArrayList<>());
				}
				lookupSynsetForNoun.get(noun).add(synset);
			}
		}
		In hypIn = new In(hypernyms);
		Digraph digraph = new Digraph(synsetMap.size());
		while (hypIn.hasNextLine()) {
			String temp = hypIn.readLine();
			String[] array = temp.split(",");
			Integer synsetId = Integer.valueOf(array[0]);
			Synset synset = synsetMap.get(synsetId);

			for (int i = 1; i < array.length; i++) {
				Integer adj = Integer.valueOf(array[i]);
				synset.parent.add(synsetMap.get(adj));
				digraph.addEdge(synsetId, adj);
			}
		}
		Set<Synset> synsetCollection = new HashSet<>();
		for (Map.Entry<Integer, Synset> entry : synsetMap.entrySet()) {
			if (entry.getValue().parent.size() == 0) {
				synsetCollection.add(entry.getValue());
			}
		}
		if (synsetCollection.size() != 1) {
			throw new IllegalArgumentException();
		}
		DirectedCycle cycle = new DirectedCycle(digraph);
		if (cycle.hasCycle()) {
			throw new IllegalArgumentException();
		}
	}

	// returns all WordNet nouns
	public Iterable<String> nouns() {
		return lookupNoun;
	}

	// is the word a WordNet noun?
	public boolean isNoun(String word) {
		if (word == null) {
			throw new IllegalArgumentException();
		}
		return lookupNoun.contains(word);
	}

	// distance between nounA and nounB (defined below)
	public int distance(String nounA, String nounB) {
		if (nounA == null || nounB == null || !lookupNoun.contains(nounA) || !lookupNoun.contains(nounB)) {
			throw new IllegalArgumentException();
		}
		List<Synset> setA = lookupSynsetForNoun.get(nounA);
		List<Synset> setB = lookupSynsetForNoun.get(nounB);

		int distance = Integer.MAX_VALUE;

		for (Synset outer : setA) {
			for (Synset inner : setB) {
				Pair pair = computeDistance(outer, inner);
				if (pair.distance != -1 && pair.distance < distance) {
					distance = pair.distance;
				}
			}
		}
		return distance == Integer.MAX_VALUE ? -1 : distance;
	}

	private Pair computeDistance(Synset outer, Synset inner) {
		List<List<Synset>> outerPathList = getPathToRoot(outer);
		List<List<Synset>> innerPathList = getPathToRoot(inner);
		int distance = Integer.MAX_VALUE;
		Pair pair = new Pair(null, -1);
		for (List<Synset> outerPath : outerPathList) {
			for (List<Synset> innerPath : innerPathList) {
				Pair currentPair = computeDistanceBetweenPath(outerPath, innerPath);
				if (currentPair.distance != -1 && currentPair.distance < distance) {
					distance = currentPair.distance;
					pair = currentPair;
				}
			}
		}
		return pair;
	}

	private Pair computeDistanceBetweenPath(List<Synset> outerPath, List<Synset> innerPath) {
		int distance = Integer.MAX_VALUE;
		Synset ancestor = null;
		for (int outerIndex = 0; outerIndex < outerPath.size(); outerIndex++) {
			Synset outerVariable = outerPath.get(outerIndex);
			if (innerPath.contains(outerVariable)) {
				int currentDistance = outerIndex + innerPath.indexOf(outerVariable);
				if (currentDistance < distance) {
					distance = currentDistance;
					ancestor = outerVariable;
				}
			}
		}
		return distance == Integer.MAX_VALUE ? new Pair(null, -1) : new Pair(ancestor, distance);

	}

	private List<List<Synset>> getPathToRoot(Synset inner) {
		List<List<Synset>> manyPath = new ArrayList<>();
		computePath(inner, manyPath, 0, new ArrayList<>());
		return manyPath;
	}

	private void computePath(Synset inner, List<List<Synset>> manyPath, int index, List<Synset> tempList) {
		if (inner.parent.isEmpty()) {
			List<Synset> list = new ArrayList<>();
			for (int i = 0; i < index; i++) {
				list.add(tempList.get(i));
			}
			list.add(inner);
			manyPath.add(list);
			return;
		}
		if (tempList.size() > index) {
			tempList.set(index, inner);
		} else {
			tempList.add(inner);
		}
		for (Synset parent : inner.parent) {
			computePath(parent, manyPath, index + 1, tempList);
		}
	}

	// a synset (second field of synsets.txt) that is the common ancestor of nounA
	// and nounB
	// in a shortest ancestral path (defined below)
	public String sap(String nounA, String nounB) {
		if (nounA == null || nounB == null || !lookupNoun.contains(nounA) || !lookupNoun.contains(nounB)) {
			throw new IllegalArgumentException();
		}
		List<Synset> setA = lookupSynsetForNoun.get(nounA);
		List<Synset> setB = lookupSynsetForNoun.get(nounB);

		int distance = Integer.MAX_VALUE;
		Pair p = null;
		for (Synset outer : setA) {
			for (Synset inner : setB) {
				Pair temp = computeDistance(outer, inner);
				if (temp.distance != -1 && temp.distance < distance) {
					distance = temp.distance;
					p = temp;
				}
			}
		}
		return combine(p.synset);
	}

	private String combine(Synset synset) {
		StringBuilder builder = new StringBuilder();
		for (String array : synset.list) {
			builder.append(" ");
			builder.append(array);
		}
		return builder.substring(1).trim();
	}

	public static void main(String[] args) {
		WordNet wordnet = new WordNet(args[0], args[1]);
		System.out.println(wordnet.distance("a", "b"));
	}

	private class Synset {
		private final Integer id;

		private final List<String> list;

		private final List<Synset> parent;

		public Synset(Integer id, List<String> list) {
			this.id = id;
			this.list = list;
			this.parent = new ArrayList<>();
		}

		@Override
		public boolean equals(Object obj) {
			Synset temp = (Synset) obj;
			return temp.id.equals(this.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	private class Pair {
		private final Synset synset;
		private final int distance;

		public Pair(Synset synset, int distance) {
			this.synset = synset;
			this.distance = distance;
		}
	}
}