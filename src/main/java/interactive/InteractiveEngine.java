package interactive;

import common.Utility;
import index.IndexGeneration;
import ranking.RankingEngine;
import snippet.SnippetEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InteractiveEngine {
    private static final String NEW_QUERY_PROMPT_MSG = "Please enter a new query.";
    private static final String NEXT_STEP_PROMPT_MSG = "Please type in the number of a document to view, " +
            "or type N for new query, or Q for quit";
    private static final String WRONG_DOCNO_MSG = "Your input docno is invalid or not among the results. Please reenter it.";
    private static final String QUIT_MSG = "Quit.";

    private static final int NANO_TO_SECOND = 1000000000;

    private static final Set<String> RANK_NUMBERS = new HashSet<>(
            Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));

    // store the ranking results in case the user want to see the complete document
    private static final List<String> rankingResultDocnos = new ArrayList<>(10);
    private static final Scanner userInput = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            promptHelpMsg();
        }
        System.out.println("Loading metadata. Please wait a moment...");

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
            System.out.println("\n" + NEW_QUERY_PROMPT_MSG);
            rankingResultDocnos.clear();
            String query = userInput.nextLine();    // excluding any line separator at the end.

            // https://stackoverflow.com/questions/180158/how-do-i-time-a-methods-execution-in-java
            // start the timer
            long startTime = System.nanoTime();

            // invokes the ranking engine, displays the result, and update rankingResultDocnos;
            List<String> top10Docnos = RankingEngine.getTop10DocnosOfQuery(query, indexBaseDirBackSlash,
                    idToDocnoMap, lexiconTermToId, invertedIndex);
            rankingResultDocnos.addAll(top10Docnos);

            SnippetEngine snippetEngine = new SnippetEngine(query);
            for (int i = 1; i <= top10Docnos.size(); ++i) {
                String docno = top10Docnos.get(i - 1);
                String dateHierarchy = Utility.getDateFolderHierarchy(docno);

                // TODO: generate the snippets
                String queryBiasedSnippet = "Query-Biased Snippet";
                String rawDocPath = indexBaseDirBackSlash + "raw/" + dateHierarchy + "/" + docno;
                String rawDoc = getWholeContentFromGzipReader(rawDocPath);
                queryBiasedSnippet = snippetEngine.getSnippet(rawDoc);

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
            System.out.printf("\nRetrieval took %s seconds\n", durationInSecond);

            // next prompt
            System.out.println("\n" + NEXT_STEP_PROMPT_MSG);
            String choice = userInput.nextLine().trim();

            // quit;
            if (choice.equals("Q")) {
                System.out.println(QUIT_MSG);
                System.exit(0);
            }

            // the user want to see the complete content of a result, input is a ranking number within 1-10
            boolean newQueryFlag = false;
            while (!choice.equals("N")) {

                while (!RANK_NUMBERS.contains(choice)) {
                    System.out.println(WRONG_DOCNO_MSG);
                    choice = userInput.nextLine().trim();
                    if (choice.equals("N")) {
                        newQueryFlag = true;
                        break;
                    } else if (choice.equals("Q")) {
                        System.out.println(QUIT_MSG);
                        System.exit(0);
                    }
                }
                if (!newQueryFlag) {
                    // fetch the complete content of the doc
                    String docnoToFetch = rankingResultDocnos.get(Integer.parseInt(choice) - 1);    // remember -1
                    String dateHierarchy = Utility.getDateFolderHierarchy(docnoToFetch);
                    String rawDocPath = indexBaseDirBackSlash + "raw/" + dateHierarchy + "/" + docnoToFetch;
                    System.out.println(getWholeContentFromGzipReader(rawDocPath));

                    // next prompt
                    System.out.println("\n" + NEXT_STEP_PROMPT_MSG);
                    choice = userInput.nextLine().trim();

                    // quit;
                    if (choice.equals("Q")) {
                        System.out.println(QUIT_MSG);
                        System.exit(0);
                    }
                }

            } // else: new query, continue
        }
    }


    public static String getWholeContentFromGzipReader(String filepath) throws IOException {
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
