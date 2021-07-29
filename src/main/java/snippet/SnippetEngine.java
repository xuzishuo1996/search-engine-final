package snippet;

import common.Utility;
import index.IndexGeneration;

import java.io.BufferedReader;
import java.util.*;

public class SnippetEngine {

    private final String query;
    private final List<String> tokenizedQuery;
    private final Set<String> uniqueQueryTerms;

    public SnippetEngine(String query) {
        this.query = query;
        this.tokenizedQuery = IndexGeneration.extractAlphanumerics(query);
        this.uniqueQueryTerms = new HashSet<>(tokenizedQuery);
    }

    /**
     *
     * @param rawDoc raw XML doc
     * @return sentences in descending order of query-related score.
     * only <HEADLINE>, <TEXT> and <GRAPHIC> in the XML doc are considered.
     */
    public String getSnippet(String rawDoc) {
        List<String> primitiveSentences = new ArrayList<>();
        PriorityQueue<Sentence> pq = new PriorityQueue<>(Comparator.comparingInt(Sentence::getScore).reversed());

        int startPos = rawDoc.indexOf("<HEADLINE>");
        int endPos = rawDoc.indexOf("</HEADLINE>");
        String headlineWithPTag = rawDoc.substring(startPos + "<HEADLINE>".length(), endPos);
        System.out.println(headlineWithPTag);
        // TODO: split the <P></P> tag within it.

        // TODO: <TEXT> <GRAPHIC>. may abstract the steps into a method

        // get result: use 2 sentences
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; ++i) {
            int pos = pq.poll().getPosInDoc();
            if (i == 1) {
                sb.append(" ");
            }
            sb.append(primitiveSentences.get(pos));
        }
        return sb.toString();
    }

    private void splitSentences(String para, List<String> primitiveSentences,
                                PriorityQueue<Sentence> pq) {
        // ref: https://blog.csdn.net/qy20115549/article/details/107400321
        List<Integer> periodPos = getAllPos(para, '.');
        List<Integer> questionPos = getAllPos(para, '?');
        List<Integer> exclamationPos = getAllPos(para, '!');

        List<String> splitSentences = new ArrayList<>();
        // TODO: merge the results
        List<Integer> pos = new ArrayList<>();
        pos.add(-1);    // for the convenience to write code
        for (int i = 0; i < pos.size() - 1; ++i) {
            splitSentences.add(para.substring(pos.get(i) + 1, pos.get(i + 1) + 1)); // include "[.?!]"
        }
        splitSentences.add(para.substring(pos.get(pos.size() - 1)) + 1);

        for (String s : splitSentences) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                int num = primitiveSentences.size();

                // TODO:
                // tokenize it
                // calculate the score
                // put it into the priority queue
            }
        }
    }

    private List<Integer> getAllPos(String para, char c) {
        List<Integer> res = new ArrayList<>();
        int start = 0;
        int pos = para.indexOf(c, start);
        while (pos != -1) {
            res.add(pos);
            start = pos + 1;
            pos = para.indexOf(c, start);
        }
        return res;
    }

    class Sentence implements Comparable<Sentence> {
        String primitiveSentence;
        String tokenizedSentence;
        int score;
        int posInDoc = 0;

        public Sentence(String primitiveSentence) {
            this.primitiveSentence = primitiveSentence;
        }

        @Override
        public int compareTo(Sentence o) {
            return o.score - this.score;
        }

        public String getPrimitiveSentence() {
            return primitiveSentence;
        }

        public void setPrimitiveSentence(String primitiveSentence) {
            this.primitiveSentence = primitiveSentence;
        }

        public String getTokenizedSentence() {
            return tokenizedSentence;
        }

        public void setTokenizedSentence(String tokenizedSentence) {
            this.tokenizedSentence = tokenizedSentence;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getPosInDoc() {
            return posInDoc;
        }

        public void setPosInDoc(int posInDoc) {
            this.posInDoc = posInDoc;
        }
    }
}


