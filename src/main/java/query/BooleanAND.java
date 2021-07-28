package query;

import common.Utility;
import index.IndexGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class BooleanAND {
    private static final String runTag = "z463xuAND";

    public static void main(String[] args) throws IOException {
        String docsDir = args[0];
        String queryFilepath = args[1];
        String outputFilePath = args[2];

        String docsDirBackSlash = docsDir.charAt(docsDir.length() - 1) == '/' ?
                docsDir : docsDir + '/';
        String metadataPath = docsDirBackSlash + "metadata/";
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
            handleNextQuery(docsDirBackSlash, queryReader, writer, idToDocnoMap, invertedIndex, lexiconTermToId);
        }
    }

    public static void handleNextQuery(String docsDir, BufferedReader queryReader, BufferedWriter writer,
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
        List<String> queryTokens = IndexGeneration.extractAlphanumerics(tokensLine);

        // boolean AND intersect algo
        Set<Integer> relatedDocIds = getRelatedDocIds(queryTokens, invertedIndex, lexiconTermToId);
//        Set<String> relatedDocnos = new HashSet<>(relatedDocIds.size());
//        for (int id : relatedDocIds) {
//            relatedDocnos.add((String)idToDocnoMap.get(id));
//        }

        // write output
        int numOfDocs = relatedDocIds.size();
        int i = 1;  // ranking starting from 1
        for (int id : relatedDocIds) {
            String entry = String.format("%s Q0 %s %s %s %s\n",
                    topicId, idToDocnoMap.get(id), i, numOfDocs - i, runTag);
            writer.write(entry);
            ++i;
        }
    }

    // Note: inverted index: termId -> [docId, cnt; docId, cnt; ...]. docIds are in ascending order.
    static Set<Integer> getRelatedDocIds(List<String> queryTokens,
                                         List<List<Integer>> invertedIndex,
                                         HashMap<String, Integer> lexiconTermToId) {
        Set<Integer> relatedDocIds = new HashSet<>();
        if (queryTokens.isEmpty()) { return relatedDocIds; }

        List<List<Integer>> relatedPostings = new ArrayList<>();

        for (String queryToken : queryTokens) {
            if (!lexiconTermToId.containsKey(queryToken)) {
                return relatedDocIds;
            }
            relatedPostings.add(invertedIndex.get(lexiconTermToId.get(queryToken)));
        }

        // sort relatedPostings by size of List: ascending order
        relatedPostings.sort(Comparator.comparingInt(List::size));

        // postings intersect
        List<Integer> intersected = new ArrayList<>();
        // k = 0
        for (int i = 0; i < relatedPostings.get(0).size(); i += 2) {
            intersected.add(relatedPostings.get(0).get(i));
        }
        for (int k = 1; k < relatedPostings.size(); ++k) {
            List<Integer> tmpResult = new ArrayList<>();
            int i = 0, j = 0;
            List<Integer> currPostings = relatedPostings.get(k);
            while (i < intersected.size() && j < currPostings.size()) {
                if (intersected.get(i).equals(currPostings.get(j))) {
                    tmpResult.add(intersected.get(i));
                    ++i;
                    j += 2;
                } else if (intersected.get(i) < currPostings.get(j)) {
                    ++i;
                } else {
                    j += 2;
                }
            }
            if (tmpResult.isEmpty()) { return relatedDocIds; }
            intersected = tmpResult;
        }

        relatedDocIds.addAll(intersected);
        return relatedDocIds;
    }
}
