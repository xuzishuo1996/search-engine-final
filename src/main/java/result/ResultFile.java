package result;

import exception.DuplicateException;
import exception.InputFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultFile {
//    private String basePath = "/home/xuzishuo1996/Desktop/msci720-hw/hw3-files/results-files/";
    private final String basePath;
    private static int num = 0; // suffix of the file name: "student" + num + ".result"

    private Results results;
    private String runID;

    // docno example: LA080190-0001
    private final Pattern PATTERN = Pattern.compile("^LA\\d{6}-\\d{4}$", Pattern.CASE_INSENSITIVE);

    public ResultFile(String basePath) {
        this.basePath = basePath;
    }

    public Results getResults() {
        return results;
    }

    public static int getNum() {
        return num;
    }

    public void processingSingleStudent() throws FileNotFoundException {
        ++num;
        String filepath = String.format(basePath + "student%d.results", num);
        File file = new File(filepath);
        results = new Results();
        Scanner scanner = new Scanner(file);
        boolean isFirstLine = true;
        // int preTopic = -1;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String [] fields = line.split("\\s");
            //"queryID Q0 doc-id rank score runID"
            if (fields.length != 6) {
                System.out.println("Student " + num + "'s input should have 6 columns!");
                throw new InputFormatException("Student " + num + "'s input should have 6 columns!");
            }

            int queryID = Integer.parseInt(fields[0]);
            String docno = fields[2];
            // regular expression to check the format of docno
            // ref: https://www.w3schools.com/java/java_regex.asp
            Matcher matcher = PATTERN.matcher(docno);
            if (!matcher.find()) {
                throw new DuplicateException("Invalid docno in student file " + num);
            }

            int rank = Integer.parseInt(fields[3]);
            double score = Double.parseDouble(fields[4]);
            try {
                results.addResult(queryID, docno, rank, score);
            } catch (DuplicateException e) {
                throw new DuplicateException(e.getMessage());
            }
            if (isFirstLine) {
                this.runID = fields[5];
                isFirstLine = false ;
            } else if (!this.runID.equals(fields[5])) {
                System.out.println("mismatching runIDs in student file " + num);
                throw new InputFormatException("mismatching runIDs in student file " + num);
            }
        }
        scanner.close();
        results.sortResults();
    }
}
