package ranking;

import common.Utility;
import index.IndexGeneration;
import result.Results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class RankingEngine {
//    private static final String runTag = "z463xu";
    private static Map<Integer, Integer> docIdToLenMap = new HashMap<>();

    public static List<String> getTop10DocnosOfQuery(String query, String indexBaseDirBackSlash,
                                                     HashMap<Integer, String> idToDocnoMap,
                                                     HashMap<String, Integer> lexiconTermToId,
                                                     List<List<Integer>> invertedIndex) throws IOException {
        List<String> top10Docnos = new ArrayList<>(10);

        Map<String, Integer> queryTokenFreqMap = IndexGeneration.createTermCntMapForQuery(query);
        Map<Integer, Double> docScoreMap = new HashMap<>();

        // BM25 algo: term-at-a-time for scoring docs.
        // more memory requirement, but less disk seek compared with doc-at-a-time.
        for (String token : queryTokenFreqMap.keySet()) {
            if (lexiconTermToId.get(token) == null) {
                continue;
            }
            int tokenId = lexiconTermToId.get(token);
            // postings: docId, cnt; docId, cnt; ...
            List<Integer> postings = invertedIndex.get(tokenId);
            for (int i = 0; i < postings.size(); i += 2) {
                int docId = postings.get(i);
                int tokenFreqInDoc = postings.get(i + 1);

                // get doc length (save it in a HashMap)
                int docLen;
                if (!docIdToLenMap.containsKey(docId)) {
                    docLen = Utility.getDocLength(indexBaseDirBackSlash, idToDocnoMap.get(docId));
                    docIdToLenMap.put(docId, docLen);
                } else {
                    docLen = docIdToLenMap.get(docId);
                }

                // calculate score
                double score = BM25Ranking.calcSingleTermScoreForADoc(
                        tokenFreqInDoc, queryTokenFreqMap.get(token), docLen, postings.size() / 2);
                double updatedPartialScore = docScoreMap.getOrDefault(docId, 0.0) + score;
                docScoreMap.put(docId, updatedPartialScore);
            }
        }

        // sort: descending by score
        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(docScoreMap.entrySet());
        entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // write output
        for (int i = 0; i < Math.min(entryList.size(), 10); ++i) {
            Map.Entry<Integer, Double> docIdScorePair = entryList.get(i);
            String docno = idToDocnoMap.get(docIdScorePair.getKey());
            top10Docnos.add(docno);
        }
        return top10Docnos;
    }
}
