package snippet;

import index.IndexGeneration;

import java.util.*;

public class SnippetEngine {

    private final String query;
    private final List<String> queryTokensList;
    private final Set<String> queryUniqueTerms;

    public SnippetEngine(String query) {
        this.query = query;
        this.queryTokensList = IndexGeneration.extractAlphanumerics(query);
        this.queryUniqueTerms = new HashSet<>(queryTokensList);
    }

    /**
     *
     * @param rawDoc raw XML doc
     * @return sentences in descending order of query-related score.
     * only <HEADLINE>, <TEXT> and <GRAPHIC> in the XML doc are considered.
     */
    public String getSnippet(String rawDoc) {
        List<String> primitiveSentences = new ArrayList<>();
        // pq: [score, sentence number pair]
        PriorityQueue<int[]> pq = new PriorityQueue<>((o1, o2) -> o2[0] - o1[0]);   // in descending order of score


        // extract from <HEADLINE>
        int textStart = splitTags(rawDoc, 0, "<HEADLINE>", "</HEADLINE>", primitiveSentences, pq);
        // extract from <TEXT>
        int graphicStart = splitTags(rawDoc, textStart, "<TEXT>", "</TEXT>", primitiveSentences, pq);
        // extract from <GRAPHIC>
        splitTags(rawDoc, graphicStart, "<GRAPHIC>", "</GRAPHIC>", primitiveSentences, pq);


        // get result: use 2 sentences
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; ++i) {
            if (i == 1) {
                sb.append(" ");
            }
            int pos = pq.poll()[1];
            sb.append(primitiveSentences.get(pos));
        }
        return sb.toString();
    }

    /**
     * split and scoring the contents within <HEADLINE>, <TEXT> and <GRAPHIC>
     */
    private int splitTags(String rawDoc, int from, String startTag, String endTag,
                          List<String> primitiveSentences,
                          PriorityQueue<int[]> pq) {
        int startPos = rawDoc.indexOf(startTag, from);
        // this tag does not exist
        if (startPos == -1) {
            return from;
        }
        int endPos = rawDoc.indexOf(endTag, startPos);
        // +1 and -1: remove the '\n' after startTag and before closeTag
        String contentWithPTag = rawDoc.substring(startPos + startTag.length() + 1, endPos - 1);

        // split the <P></P> tag within it.
        // positions of the split point: multiple <P></P>s
        List<Integer> posList = new ArrayList<>();
        int stripLeadingNum = "<P>\n".length();
        int stripTrailingNum = "\n</P>\n".length();

        // the first <P> is at the start of contentWithPTag
        int pos = 0;    // relative pos in contentWithPTag
        while (pos != -1) {
            posList.add(pos);
            pos = contentWithPTag.indexOf("<P>", pos + stripLeadingNum);   // actually, could be pos + "<P>".length()
        }

        // split the sentences and calculate their scores
        for (int i = 0; i < posList.size() - 1; ++i) {
            String paraWithPTag = contentWithPTag.substring(posList.get(i), posList.get(i + 1));
            String para = paraWithPTag.substring(stripLeadingNum, paraWithPTag.length() - stripTrailingNum); // also removes the '\n' before </P>
            splitSentences(para, primitiveSentences, pq);
        }

        return endPos;
    }

    private void splitSentences(String para, List<String> primitiveSentences,
                                PriorityQueue<int[]> pq) {
        // ref: https://blog.csdn.net/qy20115549/article/details/107400321
        List<Integer> periodPos = getAllPos(para, '.');
        List<Integer> questionPos = getAllPos(para, '?');
        List<Integer> exclamationPos = getAllPos(para, '!');

        // merge the results. TODO: could optimize. 3-way merge, priorityQueue. worth it? only a few positions.
        List<Integer> periodQuestionMerge = mergeSorted(periodPos, questionPos);
        List<Integer> pos = new ArrayList<>();
        pos.add(-1);    // for the convenience to write code
        pos.addAll(mergeSorted(exclamationPos, periodQuestionMerge));

        List<String> splitSentences = new ArrayList<>();
        for (int i = 0; i < pos.size() - 1; ++i) {
            splitSentences.add(para.substring(pos.get(i) + 1, pos.get(i + 1) + 1)); // +1: include "[.?!]"
        }
        int lastSplitPos = pos.get(pos.size() - 1);
        if (lastSplitPos < para.length() - 1) {
            splitSentences.add(para.substring(lastSplitPos + 1));
        }

        for (String s : splitSentences) {
            String trimmed = s.trim();  // does not alter the primitive string
            if (!trimmed.isEmpty()) {
                // get the previous num of sentences, i.e, the pos of curr sentence in the primitive-sentences-list
                int num = primitiveSentences.size();
                // add it to the collection, for display
                primitiveSentences.add(trimmed);

                // tokenize it
                List<String> tokensList = IndexGeneration.extractAlphanumerics(trimmed);

                // calculate the score
                int score = calScore(tokensList);
                // add 2 to the score for the first sentence; 1, if second; 0 otherwise.
                if (num == 0) { // the first sentence in the doc
                    score += 2;
                } else if (num == 1) { // the second sentence in the doc
                    score += 1;
                }

                // put it into the priority queue
                pq.add(new int[]{score, num});
            }
        }
    }

    /**
     * Scoring Scheme:
     * Note: for convenience, the first part is moved into splitSentences().
     * 1. Let l be 2 if S is the first sentence, 1, if second, 0 otherwise.
     * 2. Let c be the number of wi that are query terms, including repetitions.
     * 3. Let d be the number of distinct query terms that match some wi.
     * 4. Identify the longest contiguous run of queries terms in S, say wj ... wj+k.
     * Use a equal combination of l, c, d, k to derive a score value V.
     */
    private int calScore(List<String> sentenceTokensList) {
        int c = 0;
        Set<String> sentenceUniqueTerms = new HashSet<>();
        for (String term : sentenceTokensList) {
            if (queryUniqueTerms.contains(term)) {
                ++c;
                sentenceUniqueTerms.add(term);
            }
        }
        int d = sentenceUniqueTerms.size();

        // calculate k
        int k = calMaxContiguous(sentenceTokensList);

        return c + d + k;
    }

    /**
     * Dynamic Programming:
     * Similar to longest common subsequence (LCS).
     * ref: https://en.wikipedia.org/wiki/Longest_common_subsequence_problem
     * @param sentenceTokensList
     * @return
     */
    private int calMaxContiguous(List<String> sentenceTokensList) {
        int m = queryTokensList.size();
        int n = sentenceTokensList.size();
        int[][] dp = new int[m + 1][n + 1]; // default initialized value is 0

        // initialize the first row and first col to 0. omitted because the default initialized value is 0.

        // fill the dp chart horizontally
        for (int i = 1; i <= m; ++i) {
            for (int j = 1; j <= n; ++j) {
                if (queryTokensList.get(i - 1).equals(sentenceTokensList.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
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

    private List<Integer> mergeSorted(List<Integer> lst1, List<Integer> lst2) {
        List<Integer> res = new ArrayList<>();
        int i = 0, j = 0;
        while (i < lst1.size() && j < lst2.size()) {
            int elem1 = lst1.get(i);
            int elem2 = lst2.get(j);
            if (elem1 < elem2) {
                res.add(elem1);
                ++i;
            } else {
                res.add(elem2);
                ++j;
            }
        }
        if (i < lst1.size()) {
            res.addAll(lst1.subList(i, lst1.size()));   // [inclusive, exclusive)
        }
        if (j < lst2.size()) {
            res.addAll(lst2.subList(j, lst2.size()));
        }
        return res;
    }

//    class Sentence implements Comparable<Sentence> {
//        String primitiveSentence;
//        String tokenizedSentence;
//        int score;
//        int posInDoc;
//
//        public Sentence(String primitiveSentence) {
//            this.primitiveSentence = primitiveSentence;
//        }
//
//        @Override
//        public int compareTo(Sentence o) {
//            return o.score - this.score;
//        }
//
//        public String getPrimitiveSentence() {
//            return primitiveSentence;
//        }
//
//        public void setPrimitiveSentence(String primitiveSentence) {
//            this.primitiveSentence = primitiveSentence;
//        }
//
//        public String getTokenizedSentence() {
//            return tokenizedSentence;
//        }
//
//        public void setTokenizedSentence(String tokenizedSentence) {
//            this.tokenizedSentence = tokenizedSentence;
//        }
//
//        public int getScore() {
//            return score;
//        }
//
//        public void setScore(int score) {
//            this.score = score;
//        }
//
//        public int getPosInDoc() {
//            return posInDoc;
//        }
//
//        public void setPosInDoc(int posInDoc) {
//            this.posInDoc = posInDoc;
//        }
//    }
}


