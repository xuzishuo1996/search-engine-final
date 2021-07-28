package interactive;

import common.Utility;

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

//        // deserialization
//        HashMap<Integer, String> idToDocnoMap = (HashMap<Integer, String>) Utility.deserialize(idToDocnoMapPath);
//        HashMap<String, Integer> lexiconTermToId = (HashMap<String, Integer>) Utility.deserialize(lexiconTermToIdPath);
//        List<List<Integer>> invertedIndex = (List<List<Integer>>) Utility.deserialize(invertedIndexPath);

        while (true) {
            System.out.println(NEW_QUERY_PROMPT_MSG);
            rankingResultDocnos.clear();
            String query = userInput.nextLine();    // excluding any line separator at the end.
            // TODO: invokes the ranking engine, displays the result, and update rankingResultDocnos;


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
                // TODO: fetch the complete content of the doc
                String dateHierarchy = Utility.getDateFolderHierarchy(choice);
                String rawDocPath = indexBaseDirBackSlash + "raw/" + dateHierarchy + "/" + choice;
                System.out.println(getWholeContentFromGzipReader(rawDocPath));
            } // else: new query, continue
        }
    }

    private static String getWholeContentFromGzipReader(String rawDocPath) throws IOException {
        BufferedReader reader = Utility.getGzipReader(rawDocPath);
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
