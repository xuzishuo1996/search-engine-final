package retriever;

import common.Utility;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static common.Utility.getGzipReader;

public class GetDoc {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            promptHelpMsg();
        }

        String inputBaseDir = args[0];
        File inputPath = new File(inputBaseDir);
        if (!inputPath.exists()) {
            System.err.println("[Error] Input directory does not exist!");
            System.exit(-1);
        }
        if (!inputPath.isDirectory()) {
            System.err.println("[Error] Input path is not a directory!");
            System.exit(-1);
        }
        // with '/' in the end
        String inputBaseDirWithBackSlash = inputBaseDir.charAt(inputBaseDir.length() - 1) == '/' ?
                inputBaseDir : inputBaseDir + '/';

        boolean usingDocno = false;
        switch (args[1]) {
            case "docno": usingDocno = true; break;
            case "id" : usingDocno = false; break;
            default:
                System.err.println("retrieval type is wrong: should be id or docno.");
                System.exit(-1);
                break;
        }

        String docName = args[2];   // if using docno directly

        if (!usingDocno) {
            // deserialize the hashmap
            // ref: https://beginnersbook.com/2013/12/how-to-serialize-hashmap-in-java/
            HashMap<Integer, String> idToDocnoMap = (HashMap<Integer, String>) Utility.deserialize(inputBaseDirWithBackSlash + "metadata/idToDocnoMap.ser");

            // update docName from id to docno
            docName = idToDocnoMap.get(Integer.parseInt(docName));
        }

        // retrieve metadata and raw doc, and display
        String dateHierarchy = Utility.getDateFolderHierarchy(docName);
        String metadataPath = inputBaseDirWithBackSlash + "metadata/" + dateHierarchy + "/" + docName;
        String rawDocPath = inputBaseDirWithBackSlash + "raw/" + dateHierarchy + "/" + docName;

        BufferedReader metadataReader = getGzipReader(metadataPath);
        BufferedReader rawDocReader = getGzipReader(rawDocPath);

        // get raw metadata
        String rawMetadata = getWholeContentFromGzipReader(metadataReader);
        String[] split = rawMetadata.split("\n");
        String id = split[0];
        String docLen = split[1];
        String headline = rawMetadata.substring(id.length() + 1 + docLen.length()).trim(); // including a leading '\n'
        // generate formatted metadata given its fields
        String metadata = String.format("docno: %s\n" +
                "internal id: %s\n" +
                "date: %s\n" +
                "headline: \n%s\n" +
                "document length (in tokens): %s", docName, id, Utility.getNaturalDate(docName), headline, docLen);

        String rawDoc = getWholeContentFromGzipReader(rawDocReader);

        System.out.printf("%s\nraw document:\n%s%n", metadata, rawDoc);
    }

    private static String getWholeContentFromGzipReader(BufferedReader reader) {
        StringBuilder sb = new StringBuilder();
        List<String> lines = reader.lines().collect(Collectors.toList());
        for (String line : lines) {
            sb.append(line).append('\n');
        }
        return sb.toString().trim();
    }

    private static void promptHelpMsg() {
        System.err.println("[Error] Invalid input arguments:\n===== Usage ======\n" +
                "Argument 1: file path that stores metadata and raw docs.\n" +
                "Argument 2: retrieval type (id or docno).\n" +
                "Argument 3: id or docno string.");
        System.exit(-1);
    }
}
