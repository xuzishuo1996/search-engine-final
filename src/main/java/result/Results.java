package result;

import exception.DuplicateException;

import java.util.*;

// ref: course example code
public class Results {
    public class Result implements Comparable<Result> {
        private String docno;
        private int rank;
        private double score;

        public Result(String docno, int rank, double score) {
            this.docno = docno;
            this.rank = rank;
            this.score = score;
        }

        public String getDocno() {
            return docno;
        }

        public int getRank() {
            return rank;
        }

        public double getScore() {
            return score;
        }

        // A descending sort on score and docno
        @Override
        public int compareTo(Result result) {
            if (this.score > result.score) {
                return -1;
            } else if (this.score < result.score) {
                return 1;
            } else {
                return -(this.docno.compareTo(result.docno));
            }
        }
    }

    // query id, result
    private Map<Integer, List<Result>> resultsMap;
    private Set<String> keysForDupDetect;

    public Results() {
        this.resultsMap = new HashMap<>();
        this.keysForDupDetect = new HashSet<>();
    }

    public Map<Integer, List<Result>> getResultsMap() {
        return resultsMap;
    }

    public void addResult(int queryID, String docno, int rank, double score) {
        // be a bit careful about catching a bad mistake
        String key = queryID + "-" + docno;
        if (keysForDupDetect.contains(key)) {
            System.out.printf("Cannot have duplicate queryID and docID data points: %s, %s\n", queryID, docno);
            throw new DuplicateException("Cannot have duplicate queryID and docID data points");
        }
        keysForDupDetect.add(key);

        // Add to database
//        List<Result> resultForCurrQuery;
        resultsMap.putIfAbsent(queryID, new ArrayList<>());
        Result result = new Result(docno, rank, score);
        resultsMap.get(queryID).add(result);
    }

    public void sortResults() {
        for (List<Result> resultList : resultsMap.values()) {
            resultList.sort(Result::compareTo);
        }
    }
}
