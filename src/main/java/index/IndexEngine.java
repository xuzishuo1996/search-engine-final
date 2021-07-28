package index;

import common.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class IndexEngine {
    static int docId = -1; // doc internal id starts from 0

    // for average doc length calculation
    static long totalTokenNumOfAllDocs = 0;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            promptHelpMsg();
        }
        String inputFileName = args[0];
        // ref: https://stackoverflow.com/questions/1816673/how-do-i-check-if-a-file-exists-in-java
        if (!new File(inputFileName).isFile()) {
            System.err.println("[Error] Input data file does not exist!");
            System.exit(-1);
        }
        String outputDirName = args[1];
        if (new File(outputDirName).exists()) {
            System.err.println("[Error] Output directory already exist!");
            System.exit(-1);
        }
        // with '/' in the end
        String outputDirNameWithBackSlash = outputDirName.charAt(outputDirName.length() - 1) == '/' ?
                outputDirName : outputDirName + '/';

        BufferedReader reader = Utility.getGzipReader(inputFileName);


        // ============================= Data Structures ======================================
        // <id, docno> HashMap
        HashMap<Integer, String> idToDocnoMap = new HashMap<>();

        // 2 lexicons: map between term to id and reversely
        HashMap<String, Integer> lexiconTermToId = new HashMap<>();
        // could use int[] instead, but use HashMap for consistency with lexiconTermToId
        // HashMap<Integer, String> lexiconIdToTerm = new HashMap<>();

        // in-memory inverted index: map from term id to array(doc id, term cnt; doc id, term cnt, ...)
        HashMap<Integer, List<Integer>> invertedIndex = new HashMap<>();

        // deal with one doc at a time
        while (true) {
            handleNextDoc(reader, outputDirNameWithBackSlash, idToDocnoMap,
                    lexiconTermToId, invertedIndex);
        }
    }

    private static void handleNextDoc(BufferedReader reader, String outputDirName,
                                      HashMap<Integer, String> idToDocnoMap,
                                      HashMap<String, Integer> lexiconTermToId,
                                      HashMap<Integer, List<Integer>> invertedIndex) {
        // StringBuilder is ok if it not multi-threaded.
        StringBuilder rawDoc = new StringBuilder();

        // down-case all characters of any contiguous sequence of alphanumerics within
        // <HEADLINE> <TEXT> <GRAPH> tags (except the tags themselves)
        List<String> tokens = new ArrayList<>();

        try {
            String line = reader.readLine();
            // EOF
            if (line == null) {
                // output average doc length
                double averageDocLen = (double) totalTokenNumOfAllDocs / (double) idToDocnoMap.size();
                System.out.printf("Total num of docs: %d\n", idToDocnoMap.size());
                System.out.printf("Total num of tokens: %d\n", totalTokenNumOfAllDocs);
                System.out.printf("Average doc length: %s\n", averageDocLen);

                // TODO: remove when submission
                // debug-only
                System.out.println("Before serialization!");

                // serialize lexicons and invertedIndex
                String serializationDir = outputDirName + "metadata/";
                // Serialize the id-docno HashMap
                serializeIdToDocnoMap(serializationDir, idToDocnoMap);
                // Serialize the term-id lexicon
                serializeLexiconTermToId(serializationDir, lexiconTermToId);
                // Serialize the id-term lexicon
                serializeLexiconIdToTerm(serializationDir, createLexiconIdToTerm(lexiconTermToId));

                // TODO: remove when submission
                // debug-only
                System.out.println("Before serializing inverted index!");

                // convert to an array of arrays when saving invertedIndex
                List<List<Integer>> compactInvertedIndex = createCompactInvertedIndex(invertedIndex);
                // serialize the compact inverted index
                serializeInvertedIndex(serializationDir, compactInvertedIndex);

                // TODO: remove when submission
                // debug-only
                System.out.println("All data structures have been serialized!");

                reader.close();
                System.exit(0);
            }

            // fields for metadata
            ++docId;
            String docno = "";
            String headline = "";   // default is empty

            boolean docnoExtracted = false;
            boolean headlineExtracted = false;
            boolean testExtracted = false;
            boolean graphExtracted = false;

            while (!line.trim().equals("</DOC>")) {
                // ref: https://stackoverflow.com/questions/9922859/bufferedreader-readline-issue-detecting-end-of-file-and-empty-return-lines
                // readLine considers \n, \r, and EOF all to be line terminators,
                // and doesn't include the terminator in what it returns

                // extract <DOCNO>: <DOCNO> and </DOCNO> are within the same line
                if (!docnoExtracted && line.startsWith("<DOCNO>")) {
                    int startPos = line.indexOf('>') + 1;
                    int endPos = line.lastIndexOf('<');
                    docno = line.substring(startPos, endPos).trim();

                    // update the <id, docno> map
                    idToDocnoMap.put(docId, docno);
                    docnoExtracted = true;
                }
                // extract headline: there could be multi-paras in the headline
                // append lines in separate <P>s into a single line as the headline
                if (!headlineExtracted && line.startsWith("<HEADLINE>")) {
                    // in <HEADLINE> there are <P>s
                    StringBuilder headlineBuilder = new StringBuilder();
                    while (!line.startsWith("</HEADLINE>")) {
                        if (!line.startsWith("<")) {
                            headlineBuilder.append(line).append('\n');
                            tokens.addAll(IndexGeneration.extractAlphanumerics(line));
                        }

                        rawDoc.append(line).append('\n');
                        line = reader.readLine();
                    }
                    headline = headlineBuilder.toString();
                    headlineExtracted = true;
                }
                // deal with text and graph: extract the tokens within the tags
                if (!testExtracted && line.startsWith("<TEXT>")) {
                    while (!line.startsWith("</TEXT>")) {
                        if (!line.startsWith("<")) {
                            tokens.addAll(IndexGeneration.extractAlphanumerics(line));
                        }

                        rawDoc.append(line).append('\n');
                        line = reader.readLine();
                    }
                    testExtracted = true;
                }
                if (!graphExtracted && line.startsWith("<GRAPHIC>")) {
                    while (!line.startsWith("</GRAPHIC>")) {
                        if (!line.startsWith("<")) {
                            tokens.addAll(IndexGeneration.extractAlphanumerics(line));
                        }

                        rawDoc.append(line).append('\n');
                        line = reader.readLine();
                    }
                    graphExtracted = true;
                }

                // append the line to raw doc. add '\n\ because readLine() removes '\n'.
                rawDoc.append(line).append('\n');
                line = reader.readLine();
            }
            rawDoc.append(line);

            // ==================== Index Construction =======================
            // lexicon is an inout para: updated in convertTokesToIds
            List<Integer> tokenIds = IndexGeneration.convertTokesToIds(tokens, lexiconTermToId);
            HashMap<Integer, Integer> wordCounts = IndexGeneration.countWords(tokenIds);
            IndexGeneration.addToPostings(wordCounts, docId, invertedIndex);

            // ====================== Serialization ==========================
            String suffixDir = Utility.getDateFolderHierarchy(docno);
            // write meta-data
            writeMetaData(outputDirName + "metadata/", suffixDir, docno, docId, headline, tokens.size());
            // write compressed raw docs.
            writeCompressedRawDoc(outputDirName + "raw/", suffixDir, docno, rawDoc.toString());

            // for average doc length calculation
            totalTokenNumOfAllDocs += tokens.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================================== Data Structure Conversion ======================================
    private static HashMap<Integer, String> createLexiconIdToTerm(HashMap<String, Integer> lexiconTermToId) {
        HashMap<Integer, String> lexiconIdToTerm = new HashMap<>();
        for (Map.Entry<String, Integer> entry : lexiconTermToId.entrySet()) {
            String term = entry.getKey();
            int id = entry.getValue();
            lexiconIdToTerm.put(id, term);
        }
        return lexiconIdToTerm;
    }

    private static List<List<Integer>> createCompactInvertedIndex(HashMap<Integer, List<Integer>> invertedIndex) {
        List<List<Integer>> compactInvertedIndex = new ArrayList<>(invertedIndex.size());
        for (int i = 0; i < invertedIndex.size(); ++i) {
            compactInvertedIndex.add(invertedIndex.get(i));
        }
        return compactInvertedIndex;
    }

    // ===================================== Data Structure Serialization ======================================
    /**
     * <id, DOCNO> Map Serialization (uncompressed):
     * ref: https://beginnersbook.com/2013/12/how-to-serialize-hashmap-in-java/
     */
    public static void serializeIdToDocnoMap(String dir, HashMap<Integer, String> idToDocnoMap) throws IOException {
        serialize(dir, "idToDocnoMap.ser", idToDocnoMap);
    }

    public static void serializeLexiconTermToId(String dir, HashMap<String, Integer> lexiconTermToId) throws IOException {
        serialize(dir, "lexiconTermToId.ser", lexiconTermToId);
    }

    public static void serializeLexiconIdToTerm(String dir, HashMap<Integer, String> lexiconIdToTerm) throws IOException {
        serialize(dir, "lexiconIdToTerm.ser", lexiconIdToTerm);
    }

    public static void serializeInvertedIndex(String dir, List<List<Integer>> invertedIndex) throws IOException {
        serialize(dir, "invertedIndex.ser", invertedIndex);
    }

    private static void serialize(String dir, String filename, Object object) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dir + filename); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            oos.flush();    // have to add this, otherwise EOF exception when deserializing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================================== Write Output In Text From ======================================
    /**
     * write compressed metadata. path: .../metadata/YY/MM/DD/docno
     * @param baseMetadataDir dir to store raw metadata: .../metadata/
     * @param suffixDir path related to date: YY/MM/DD
     * @param docno identifier between <DOCNO></DOCNO>
     * @param id auto-incremented internal id
     * @param headline extracted from <HEADLINE><P> headline </P></HEADLINE>; stripped
     * @param docLen num of tokens this doc contains
     */
    static void writeMetaData(String baseMetadataDir, String suffixDir,
                              String docno, int id, String headline, int docLen) throws IOException {

        // only need to store id and headline.
        // docno will be provided or converted from id on retrieval. date will be extracted from docno
        // alternatively could only store headline
        // construct the <docno, id> map from deserialized <id, docno> map and get corresponding id by docno.
        String metadata = String.format("%s\n%s\n%s", id, docLen, headline);

        // complete output path: baseMetadataDir + suffixDir + "/" + docno
        writeData(baseMetadataDir, suffixDir, docno, metadata);
    }

    /**
     * write compressed raw docs. path: .../raw/YY/MM/DD/docno
     * ref: https://stackoverflow.com/questions/5994674/java-save-string-as-gzip-file
     * @param baseRawDir dir to store raw docs: .../raw/
     * @param suffixDir path related to date: YY/MM/DD
     * @param docno identifier between <DOCNO></DOCNO>
     * @param rawDoc doc text
     */
    static void writeCompressedRawDoc(String baseRawDir, String suffixDir,
                                      String docno, String rawDoc) throws IOException {
        writeData(baseRawDir, suffixDir, docno, rawDoc);
    }

    private static void writeData(String baseDir, String suffixDir, String docno, String content) throws IOException {
        // get the output dir: create it and necessary parent dirs if not exists
        File outputDir = new File(baseDir + suffixDir);
        boolean res = getOrCreateOutputDir(outputDir);
        if (!res) {
            System.err.println("Fail to create folder: " + baseDir + suffixDir);
            return;
        }

        // write the raw doc
        try (FileOutputStream fos = new FileOutputStream(outputDir + "/" + docno)) {
            try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(fos), StandardCharsets.UTF_8)) {
                writer.write(content);
                writer.write('\n'); // new line at the end of the file
            }
        }
    }

    // ===================================== Other Helpers ======================================
    /**
     * get the output dir: create it and necessary parent dirs if not exists
     * ref: https://stackoverflow.com/questions/28947250/create-a-directory-if-it-does-not-exist-and-then-create-the-files-in-that-direct
     * @param outputDir dir to write the files
     * @return false if the dir not exists and fail to create; otherwise true.
     */
    private static boolean getOrCreateOutputDir(File outputDir) {
        return outputDir.exists() || outputDir.mkdirs();
    }

    private static void promptHelpMsg() {
        System.err.println("[Error] Invalid input arguments:\n===== Usage ======\n" +
                "Argument 1: input data file path.\nArgument 2: output directory path.");
        System.exit(-1);
    }
}
