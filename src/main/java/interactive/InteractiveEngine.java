package interactive;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class InteractiveEngine {
    private static final String NEW_QUERY_PROMPT_MSG = "Please enter a new query.";
    private static final String NEXT_STEP_PROMPT_MSG = "Please type in the number of a document to view, " +
            "or type N for new query, or Q for quit";
    private static final String WRONG_DOCNO_MSG = "Your input docno is invalid or not among the results. Please reenter it.";
    private static final String QUIT_MSG = "Quit.";

    // store the ranking results in case the user want to see the complete document
    private static final Set<String> rankingResultDocnos = new HashSet<>(10);
    private static final Scanner userInput = new Scanner(System.in);

    public static void main() throws IOException {
//        boolean quitFlag = false;
//        boolean newQueryFlag = true;
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
                String docnoToFetch = userInput.nextLine().trim();
                while (!rankingResultDocnos.contains(docnoToFetch)) {
                    System.out.println(WRONG_DOCNO_MSG);
                    docnoToFetch = userInput.nextLine().trim();
                    if (docnoToFetch.equals("N")) {
                        break;
                    } else if (docnoToFetch.equals("Q")) {
                        System.out.println(QUIT_MSG);
                        System.exit(0);
                    }
                }
                // TODO: fetch the complete content of the doc
            } // else: new query, continue
        }
    }
}
