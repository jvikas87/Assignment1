import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet wordNet;

    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }       // constructor takes a WordNet object
    public String outcast(String[] nouns) {
        int val = Integer.MIN_VALUE;
        String noun = null;
        for(String outer: nouns){
            int currentDistane = 0;
            for(String inner:nouns){
                if(!inner.equals(outer)){
                    currentDistane+= wordNet.distance(outer,inner);
                }
            }
            if(val < currentDistane){
                val = currentDistane;
                noun = outer;
            }
        }
        return noun;
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }

}