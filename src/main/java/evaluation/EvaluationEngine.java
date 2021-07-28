package evaluation;

import result.ResultFile;

import java.io.File;
import java.io.FileNotFoundException;

public class EvaluationEngine {
    private static final String BAD_FORMAT = "bad format";

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 3) {
            System.err.println("Invalid args! Usage: java evaluation.EvaluationEngine {qrels file path} {results dir path} {index dir path}");
            return;
        }

        // append '/' to path if it does not exist
        String qrelsPath = args[0];
        String resultsPath = args[1].charAt(args[1].length() - 1) == '/' ? args[1] : args[1] + '/';
        String indexPath = args[2].charAt(args[2].length() - 1) == '/' ? args[2] : args[2] + '/';
        // check whether the input path exists and is a dir
        checkFileOrDirExists(qrelsPath, "qrels", false);
        checkFileOrDirExists(resultsPath, "results", true);
        checkFileOrDirExists(indexPath, "index", true);

        System.out.printf("%s,%s,%s,%s,%s,%s\n", "Run Name", "Mean Average Precision", "Mean P@10",
                "Mean NDCG@10", "Mean NDCG@1000", "Mean TBG");

        Evaluation evaluation = new Evaluation(qrelsPath, indexPath);
        ResultFile resultFile = new ResultFile(resultsPath);
        for (int i = 1; i < 2; ++i) { // rename non-stemmed and stemmed ranking results as student1.result and student2.result
            try {
                resultFile.processingSingleStudent();
                evaluation.reset(resultFile);
                evaluation.calMetrics();
                evaluation.outputMetrics();
                // calculate the metrics
            } catch (Exception e) {
                System.out.print("student" + ResultFile.getNum() + ",");
                for (int j = 0; j < 4; ++j) {
                    System.out.print(BAD_FORMAT + ",");
                }
                System.out.println(BAD_FORMAT);
//                System.out.printf("File %d has problem, ignore this run!\n", i);
//                e.printStackTrace();
            }
        }
    }

    private static void checkFileOrDirExists(String path, String name, boolean shouldBeDir) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.printf("[Error] %s directory does not exist!\n", name);
            System.exit(1);
        }
        if (shouldBeDir) {
            if (!file.isDirectory()) {
                System.err.printf("[Error] %s path is not a directory!\n", name);
                System.exit(1);
            }
        } else if (file.isDirectory()) {
            System.err.printf("[Error] %s path should not be a directory!\n", name);
            System.exit(1);
        }
    }
}
