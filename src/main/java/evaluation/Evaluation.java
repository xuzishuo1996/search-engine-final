package evaluation;

import result.ResultFile;
import result.Results;
import common.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Evaluation {

//    private final String qrelsPath = "/home/xuzishuo1996/Desktop/msci720-hw/hw3-files/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt";
//    private final String metadataBasePath = "/home/xuzishuo1996/Desktop/msci720-hw/latime_index/";
    private final String qrelsPath;
    private final String indexBasePath;
    Map<Integer, Set<String>> queryRelDocsMap;
    Map<Integer, List<Results.Result>> queryResultsMap;

    // metrics
    private double MAP = 0;
    private double meanPrecisionAt10 = 0;
    private double meanNDCGAt10 = 0;
    private double meanNDCGAt1000 = 0;
    private double meanTBG = 0;

    private static final double TIME_BIASED_GAIN = 0.64 * 0.77;

    public Evaluation(String qrelsPath, String indexBasePath) throws FileNotFoundException {
        this.qrelsPath = qrelsPath;
        this.indexBasePath = indexBasePath;
        this.queryRelDocsMap = getRelForTopic(qrelsPath);
    }

    public void reset(ResultFile resultFile) {
        this.queryResultsMap = resultFile.getResults().getResultsMap();
        MAP = 0;
        meanPrecisionAt10 = 0;
        meanNDCGAt10 = 0;
        meanNDCGAt1000 = 0;
        meanTBG = 0;
        System.out.println("=== student" + ResultFile.getNum() + " ===");
    }

    public Map<Integer, Set<String>> getRelForTopic(String qrelsPath) throws FileNotFoundException {
        Map<Integer, Set<String>> relDocForTopic = new HashMap<>();

        File qrelsFile = new File(qrelsPath);
        Scanner scanner = new Scanner(qrelsFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] fields = line.split("\\s");
            // format: topicID ignore docno judgment
            // the doc is relevant for this topic
            if (!fields[3].equals("0")) {
                int queryId = Integer.parseInt(fields[0]);
                String docno = fields[2];
                relDocForTopic.putIfAbsent(queryId, new HashSet<>());
                relDocForTopic.get(queryId).add(docno);
            }
        }
        scanner.close();
        return relDocForTopic;
    }

    // May be missing results for a topic, or might have extra topics. It is OKAY to have no results for a topic. On each
    // of the measures in this homework, having no results for a topic will result in a score of 0 for that topic. Having
    // extra topics is okay. Ignore extra topics.

    // 1. MAP = 1/|R| * Σ (relevant(i) * Precision@k); relevant(i) = 1 if relevant, else 0.
    public void calMAP() {
        System.out.println("1. AP for each query");
        MAP = 0;
        for (Map.Entry<Integer, List<Results.Result>> result : queryResultsMap.entrySet()) {
            double AP = 0;
            int queryId = result.getKey();
            List<Results.Result> returnedDocs = result.getValue();
            if (!queryRelDocsMap.containsKey(queryId)) {
                System.out.printf("%s\t%.4f\n", queryId, 0.0000);
                continue;
            }
            Set<String> relDocs = queryRelDocsMap.get(queryId);

            int relCnt = 0;
            for (int i = 1; i <= returnedDocs.size(); ++i) {
                Results.Result currDoc = returnedDocs.get(i - 1);
                // if the curr returned doc is relevant
                if (relDocs.contains(currDoc.getDocno())) {
                    ++relCnt;
                    AP += (double)relCnt / (double)i;   // Precision@i
                }
            }
            AP /= queryRelDocsMap.get(queryId).size(); // divided by the num of related docs in qrels file
            System.out.printf("%s\t%.4f\n", queryId, AP);   // Note: some results may be 0.0000, but actually not 0 in more decimal places.
            MAP += AP;
        }
        MAP /= queryRelDocsMap.size();
    }

    // 2. mean Precision@10
    public void calMeanPrecisionAt10() {
        System.out.println("2. Precision@10 for each query");
        meanPrecisionAt10 = 0;
        for (Map.Entry<Integer, List<Results.Result>> result : queryResultsMap.entrySet()) {
            double PrecisionAt10 = 0;
            int queryId = result.getKey();
            List<Results.Result> returnedDocs = result.getValue();
            int relCnt = 0;
            if (!queryRelDocsMap.containsKey(queryId)) {
                System.out.printf("%s\t%.4f\n", queryId, 0.0000);
                continue;
            }
            Set<String> relDocs = queryRelDocsMap.get(queryId);

            for (int i = 0; i < Math.min(10, returnedDocs.size()); ++i) {
                Results.Result currDoc = returnedDocs.get(i);
                // if the curr returned doc is relevant
                if (relDocs.contains(currDoc.getDocno())) {
                    ++relCnt;
                }
            }
            PrecisionAt10 += ((double)relCnt) / 10;
            System.out.printf("%s\t%.4f\n", queryId, PrecisionAt10);
            meanPrecisionAt10 += PrecisionAt10;
        }
        meanPrecisionAt10 /= queryRelDocsMap.size(); // divided by query nums
    }

    // 3. mean NDCG@10
    public void calMeanNDCGAt10() {
        System.out.println("3. NDCG@10 for each query");
        meanNDCGAt10 = calMeanNDCGAtK(10);
    }

    // 4. mean NDCG@1000
    public void calMeanNDCGAt1000() {
        System.out.println("4. NDCG@1000 for each query");
        meanNDCGAt1000 = calMeanNDCGAtK(1000);
    }

    // NDCG@k = DCG@k / IDCG@k
    public double calMeanNDCGAtK(int k) {
        double meanNDCGAtK = 0;
        for (Map.Entry<Integer, List<Results.Result>> result : queryResultsMap.entrySet()) {
            double NDCGAtK = 0;
            int queryId = result.getKey();
            List<Results.Result> returnedDocs = result.getValue();
            if (!queryRelDocsMap.containsKey(queryId)) {
                System.out.printf("%s\t%.4f\n", queryId, 0.0000);
                continue;
            }
            Set<String> relDocs = queryRelDocsMap.get(queryId);

            NDCGAtK += calNDCGAtK(k, returnedDocs, relDocs);
            System.out.printf("%s\t%.4f\n", queryId, NDCGAtK);
            meanNDCGAtK += NDCGAtK;
        }
        meanNDCGAtK /= queryRelDocsMap.size();
        return meanNDCGAtK;
    }

    // DCG@k = Σ (G(i)/log2(i+1)); G(i) = 1 if relevant, else 0.
    // NDCG@k = DCG@k / IDCG@k
    public double calNDCGAtK(int k, List<Results.Result> returnedDocs, Set<String> relDocs) {
        // calculate DCG
        double DCGAtK = 0;
        int endIndex = Math.min(k, returnedDocs.size());
        for (int i = 1; i <= endIndex; ++i) {
            Results.Result currDoc = returnedDocs.get(i - 1);
            // if curr doc is relevant
            if (relDocs.contains(currDoc.getDocno())) {
                DCGAtK += Math.log10(2) / Math.log10(i + 1);
            }
        }

        // calculate IDCG
        double IDCGAtK = 0;
        for (int i = 1; i <= Math.min(k, relDocs.size()); ++i) {
            IDCGAtK += Math.log10(2) / Math.log10(i + 1);
        }
        return DCGAtK / IDCGAtK;
    }

    // 5. mean TBG
    public void calMeanTBG() throws IOException {
        System.out.println("5. TBG for each query");
        meanTBG = 0;
        for (Map.Entry<Integer, List<Results.Result>> result : queryResultsMap.entrySet()) {
            double TBG = 0;
            int queryId = result.getKey();
            List<Results.Result> returnedDocs = result.getValue();
            if (!queryRelDocsMap.containsKey(queryId)) {
                System.out.printf("%s\t%.4f\n", queryId, 0.0000);
                continue;
            }
            Set<String> relDocs = queryRelDocsMap.get(queryId);

            double tk = 0;
            for (int i = 1; i <= returnedDocs.size(); ++i) {
                Results.Result currDoc = returnedDocs.get(i - 1);

                int length = getDocLength(currDoc.getDocno());
                double td = 0.018 * length + 7.8;

                // if the curr returned doc is relevant
                if (relDocs.contains(currDoc.getDocno())) {
                    double decay = Math.exp((-tk) * Math.log(2) / 224);
                    TBG += TIME_BIASED_GAIN * decay;
                    tk += (4.4 + td * 0.64);
                } else {
                    tk += (4.4 + td * 0.39);
                }
            }
            System.out.printf("%s\t%.4f\n", queryId, TBG);
            meanTBG += TBG;
        }
        meanTBG /= queryRelDocsMap.size();
    }

    public int getDocLength(String docno) throws IOException {
        String docMetadataPath = indexBasePath + "metadata/" + Utility.getDateFolderHierarchy(docno) + "/" + docno;
        BufferedReader metadataReader = Utility.getGzipReader(docMetadataPath);
        metadataReader.readLine();
        String secondLine = metadataReader.readLine();
        return Integer.parseInt(secondLine.trim());
    }

    public void calMetrics() throws IOException {
        calMAP();
        calMeanPrecisionAt10();
        calMeanNDCGAt10();
        calMeanNDCGAt1000();
        calMeanTBG();
    }

    public void outputMetrics() {
        System.out.printf("%s,%.3f,%.3f,%.3f,%.3f,%.3f\n", "student" + ResultFile.getNum(),
                MAP, meanPrecisionAt10, meanNDCGAt10, meanNDCGAt1000, meanTBG);
    }
}
