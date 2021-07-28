package interactive;

import common.Utility;
import ranking.RankingEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static common.Utility.getGzipReader;

public class InteractiveEngine {
    private static final String NEW_QUERY_PROMPT_MSG = "Please enter a new query.";
    private static final String NEXT_STEP_PROMPT_MSG = "Please type in the number of a document to view, " +
            "or type N for new query, or Q for quit";
    private static final String WRONG_DOCNO_MSG = "Your input docno is invalid or not among the results. Please reenter it.";
    private static final String QUIT_MSG = "Quit.";

    private static final int NANO_TO_SECOND = 1000000000;

    // store the ranking results in case the user want to see the complete document
    private static final Set<String> rankingResultDocnos = new HashSet<>(10);
    private static final Scanner userInput = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            promptHelpMsg();
        }
        String indexBaseDir = args[0];
        String indexBaseDirBackSlash = indexBaseDir.charAt(indexBaseDir.length() - 1) == '/' ?
                indexBaseDir : indexBaseDir + '/';
        String metadataPath = indexBaseDirBackSlash + "metadata/";
        String idToDocnoMapPath = metadataPath + "idToDocnoMap.ser";
        String invertedIndexPath = metadataPath + "invertedIndex.ser";
        String lexiconTermToIdPath = metadataPath + "lexiconTermToId.ser";

        // deserialization
        HashMap<Integer, String> idToDocnoMap = (HashMap<Integer, String>) Utility.deserialize(idToDocnoMapPath);
        HashMap<String, Integer> lexiconTermToId = (HashMap<String, Integer>) Utility.deserialize(lexiconTermToIdPath);
        List<List<Integer>> invertedIndex = (List<List<Integer>>) Utility.deserialize(invertedIndexPath);

        while (true) {
            System.out.println(NEW_QUERY_PROMPT_MSG);
            rankingResultDocnos.clear();
            String query = userInput.nextLine();    // excluding any line separator at the end.

            // https://stackoverflow.com/questions/180158/how-do-i-time-a-methods-execution-in-java
            // start the timer
            long startTime = System.nanoTime();

            // invokes the ranking engine, displays the result, and update rankingResultDocnos;
            List<String> top10Docnos = RankingEngine.getTop10DocnosOfQuery(query, indexBaseDirBackSlash,
                    idToDocnoMap, lexiconTermToId, invertedIndex);
            rankingResultDocnos.addAll(top10Docnos);


            for (int i = 1; i <= top10Docnos.size(); ++i) {
                String docno = top10Docnos.get(i - 1);
                String dateHierarchy = Utility.getDateFolderHierarchy(docno);

                // TODO: generate the snippets
                String queryBiasedSnippet = "Query-Biased Snippet";
                String rawDocPath = indexBaseDirBackSlash + "raw/" + dateHierarchy + "/" + docno;

                // get the headline from the metadata
                String docMetadataPath = metadataPath + dateHierarchy + "/" + docno;
                String metadata = getWholeContentFromGzipReader(docMetadataPath);
                String[] split = metadata.split("\n");
                String id = split[0];
                String docLen = split[1];
                String headline = null;
                int headlinePos = id.length() + docLen.length() + 2;    // +2: two '\n'
                if (metadata.length() <= headlinePos) {
                    if (queryBiasedSnippet.length() > 50) {
                        headline = queryBiasedSnippet.substring(0, 50) + " ...";
                    } else {
                        headline = queryBiasedSnippet + " ...";
                    }
                } else {
                    // +2: two
                    headline = metadata.substring(headlinePos).trim(); // not include a leading '\n'
                }

                System.out.printf("%d. %s (%s)\n%s (%s)\n", i,
                        headline, Utility.getNaturalDate(docno), queryBiasedSnippet, docno);
            }

            // calculate retrieval and snippet-generation time
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            double durationInSecond = (double) duration / (double) NANO_TO_SECOND;
            System.out.printf("Retrieval took %s seconds\n", durationInSecond);

            // TODO: remove it. for test only
//            for (String docno : top10Docnos) {
//                System.out.println(docno);
//            }

            // next prompt
            System.out.println(NEXT_STEP_PROMPT_MSG);
            String choice = userInput.nextLine().trim();

            // quit;
            if (choice.equals("Q")) {
                System.out.println(QUIT_MSG);
                System.exit(0);
            }

            // the user want to see the complete content of a result, input is a docno
            if (!choice.equals("N")) {
//                // TODO: remove it! only for test
//                rankingResultDocnos.add("LA010189-0001");

                while (!rankingResultDocnos.contains(choice)) {
                    System.out.println(WRONG_DOCNO_MSG);
                    choice = userInput.nextLine().trim();
                    if (choice.equals("N")) {
                        break;
                    } else if (choice.equals("Q")) {
                        System.out.println(QUIT_MSG);
                        System.exit(0);
                    }
                }
                // fetch the complete content of the doc
                String dateHierarchy = Utility.getDateFolderHierarchy(choice);
                String rawDocPath = indexBaseDirBackSlash + "raw/" + dateHierarchy + "/" + choice;
                System.out.println(getWholeContentFromGzipReader(rawDocPath));
            } // else: new query, continue
        }
    }



    private static String getWholeContentFromGzipReader(String filepath) throws IOException {
        BufferedReader reader = Utility.getGzipReader(filepath);
        StringBuilder sb = new StringBuilder();
        List<String> lines = reader.lines().collect(Collectors.toList());
        for (String line : lines) {
            sb.append(line).append('\n');
        }
        reader.close();
        return sb.toString().trim();
    }

    private static void promptHelpMsg() {
        System.err.println("[Error] Invalid input arguments:\n===== Usage ======\n" +
                "Argument 1: file path that stores metadata and raw docs.\n");
        System.exit(-1);
    }
}
