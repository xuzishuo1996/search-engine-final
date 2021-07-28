package index;

import stemmer.PorterStemmer;

import java.util.*;

public class IndexGeneration {
    /**
     * invoked each time when processing a single doc
     * @param tokens a list of tokens
     * @param lexiconTermToId inout para. map: (term, termId)
     * @return a list of term ids (duplicates exist, representing the word freq)
     */
    public static List<Integer> convertTokesToIds(List<String> tokens, HashMap<String, Integer> lexiconTermToId) {
        List<Integer> tokenIds = new ArrayList<>();
        for (String token : tokens) {
            if (lexiconTermToId.containsKey(token)) {
                tokenIds.add(lexiconTermToId.get(token));
            } else {
                int id = lexiconTermToId.size();    // starting from 0
                lexiconTermToId.put(token, id);
                tokenIds.add(id);
            }
        }
        return tokenIds;
    }

    /**
     * invoked each time when processing a single doc
     * @param tokenIds a list of term ids (duplicates exist, representing the word freq)
     * @return map within a single doc: (termId, count); for future use of inverted index construction
     */
    public static HashMap<Integer, Integer> countWords(List<Integer> tokenIds) {
        HashMap<Integer, Integer> wordCounts = new HashMap<>();
        for (Integer id : tokenIds) {
//            if (wordCounts.containsKey(id)) {
//                wordCounts.put(id, wordCounts.get(id) + 1);
//            } else {
//                wordCounts.put(id, 1);
//            }
            wordCounts.put(id, wordCounts.getOrDefault(id, 0) + 1);
        }
        return wordCounts;
    }

    /**
     * invoked each time when processing a single doc
     * @param wordCounts map within a single doc: (termId, count)
     * @param docId doc id
     * @param invertedIndex HashMap: inout para. an entry in it: (term id -> [docId, cnt; docId, cnt; ...])
     */
    public static void addToPostings(HashMap<Integer, Integer> wordCounts, int docId,
                                     HashMap<Integer, List<Integer>> invertedIndex) {
        for (HashMap.Entry<Integer, Integer> wordCntEntry : wordCounts.entrySet()) {
            int termId = wordCntEntry.getKey();
            int cnt = wordCntEntry.getValue();
            invertedIndex.putIfAbsent(termId, new ArrayList<>());
            List<Integer> docAndCntList = invertedIndex.get(termId);
            docAndCntList.add(docId);
            docAndCntList.add(cnt);
        }
    }

    public static List<String> extractAlphanumerics(String line) {
        String[] split = line.split("[^a-zA-Z0-9]");
        List<String> res = new ArrayList<>();
        for (String s : split) {
            if (!s.equals("")) {
                res.add(s.toLowerCase());
            }
        }
        return res;
    }

    // a line is an input query
    public static Map<String, Integer> createTermCntMapForQuery(String line) {
        String[] split = line.split("[^a-zA-Z0-9]");
        Map<String, Integer> termCntMap = new HashMap<>();
        for (String term : split) {
            if (!term.equals("")) {
                termCntMap.putIfAbsent(term, 0);
                termCntMap.put(term, termCntMap.get(term) + 1);
            }
        }
        return termCntMap;
    }
}
