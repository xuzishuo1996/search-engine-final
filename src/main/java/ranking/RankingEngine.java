package ranking;

import common.Utility;
import index.IndexGeneration;
import result.Results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class RankingEngine {
    private static final String runTag = "z463xu";
    private static Map<Integer, Integer> docIdToLenMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        String indexBaseDir = args[0];
        String queryFilepath = args[1];
        String outputFilePath = args[2];

        String indexBaseDirBackSlash = indexBaseDir.charAt(indexBaseDir.length() - 1) == '/' ?
                indexBaseDir : indexBaseDir + '/';
        String metadataPath = indexBaseDirBackSlash + "metadata/";
        String idToDocnoMapPath = metadataPath + "idToDocnoMap.ser";
        String invertedIndexPath = metadataPath + "invertedIndex.ser";
        String lexiconTermToIdPath = metadataPath + "lexiconTermToId.ser";

        BufferedReader queryReader = Utility.getPlainReader(queryFilepath);
        BufferedWriter writer = Utility.getPlainWriter(outputFilePath);

        // deserialize the idToDocnoMap
        HashMap<Integer, String> idToDocnoMap = (HashMap<Integer, String>) Utility.deserialize(idToDocnoMapPath);
        HashMap<String, Integer> lexiconTermToId = (HashMap<String, Integer>) Utility.deserialize(lexiconTermToIdPath);
        List<List<Integer>> invertedIndex = (List<List<Integer>>) Utility.deserialize(invertedIndexPath);

        while (true) {
            handleNextQuery(indexBaseDirBackSlash, queryReader, writer, idToDocnoMap, invertedIndex, lexiconTermToId);
        }
    }

    public static void handleNextQuery(String indexBaseDirBackSlash, BufferedReader queryReader, BufferedWriter writer,
                                       HashMap<Integer, String> idToDocnoMap,
                                       List<List<Integer>> invertedIndex,
                                       HashMap<String, Integer> lexiconTermToId) throws IOException {
        // first line is topic id; second is the query
        String line = queryReader.readLine();
        // EOF
        if (line == null) {
            // EOF handling
            queryReader.close();
            writer.close();
            System.exit(0);
        }
        String topicId = line.trim();
        String tokensLine = queryReader.readLine();
        Map<String, Integer> queryTokenFreqMap = IndexGeneration.createTermCntMapForQuery(tokensLine);
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
        for (int i = 0; i < Math.min(entryList.size(), 1000); ++i) {
            Map.Entry<Integer, Double> docIdScorePair = entryList.get(i);
            // format: topicId "Q0" docno rank score runTag
            String entry = String.format("%s Q0 %s %s %s %s\n",
                    topicId, idToDocnoMap.get(docIdScorePair.getKey()), i + 1, docIdScorePair.getValue(), runTag);
            writer.write(entry);
        }
    }
}
