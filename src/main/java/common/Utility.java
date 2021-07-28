package common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Utility {
    // =================== Date Utilities ================ //
    /* Format: YY/MM/DD */
    public static String getDateFolderHierarchy(String docno) {
        return String.format("%s/%s/%s",
                getYear(docno), getMonth(docno), getDay(docno));
    }

    public static String getNaturalDate(String docno) {
        String monthNameAllUpper = Month.of(Integer.parseInt(getMonth(docno))).toString();
        String firstLetter = monthNameAllUpper.substring(0, 1);
        String remain = monthNameAllUpper.substring(1).toLowerCase();
        String monthName = firstLetter + remain;
        return String.format("%s %s, 19%s",
                monthName, getDay(docno), getYear(docno));
    }

    public static String getYear(String docno) {
        return docno.substring(6, 8);
    }

    public static String getMonth(String docno) {
        return docno.substring(2, 4);
    }

    public static String getDay(String docno) {
        return docno.substring(4, 6);
    }

    // =================== Text I/O Utilities ================ //
    // ref: https://stackoverflow.com/questions/1080381/gzipinputstream-reading-line-by-line
    public static BufferedReader getGzipReader(String filename) {
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the input data file:\n" + e.getMessage());
            System.exit(-1);
        }
        InputStream gzipStream = null;
        try {
            gzipStream = new GZIPInputStream(fileStream);
        } catch (IOException e) {
            System.out.println("Could not read the gzip file:\n" + e.getMessage());
            System.exit(-1);
        }
        Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        return new BufferedReader(decoder);
    }

    public static BufferedReader getPlainReader(String filename) throws FileNotFoundException {
        return new BufferedReader(new FileReader(filename));
    }

    public static BufferedWriter getPlainWriter(String filename) throws IOException {
        // create the output file if it does not exist
        new File(filename).createNewFile();
        return new BufferedWriter(new FileWriter(filename));
    }

    // =================== Deserialization Utilities ================ //
    public static Object deserialize(String filepath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filepath); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // indexBasePath includes the trailing slash
    public static int getDocLength(String indexBasePath, String docno) throws IOException {
        String docMetadataPath = indexBasePath + "metadata/" + Utility.getDateFolderHierarchy(docno) + "/" + docno;
        BufferedReader metadataReader = Utility.getGzipReader(docMetadataPath);
        metadataReader.readLine();
        String secondLine = metadataReader.readLine();
        return Integer.parseInt(secondLine.trim());
    }
}
