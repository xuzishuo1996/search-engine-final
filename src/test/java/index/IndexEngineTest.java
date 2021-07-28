package index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class IndexEngineTest {
    File projRootDir = new File(new File("./").getCanonicalPath());
    String docno;

    IndexEngineTest() throws IOException {
    }

    @BeforeEach
    void setUp() {
        docno = "LA042990-0001";    // 1990-04-29
    }

    @Test
    void writeMetaDataTest() throws IOException {
        String outputDirName = projRootDir + "/tmp/metadata/";
        int id = 1;
        String headline = "This is the Headline";
        int docLen = 20;
        IndexEngine.writeMetaData(outputDirName, "", docno, id, headline, docLen);
    }

    @Test
    void writeCompressedRawDocTest() throws IOException {
        String outputDirName = projRootDir + "/tmp/raw/";
        String rawDoc = "<DOC>\n" +
                "<DOCNO> LA010189-0018 </DOCNO>\n" +
                "<DOCID> 42 </DOCID>\n" +
                "<DATE>\n" +
                "<P>\n" +
                "January 1, 1989, Sunday, Home Edition\n" +
                "</P>\n" +
                "</DATE>\n" +
                "<SECTION>\n" +
                "<P>\n" +
                "Calendar; Page 24; Calendar Desk\n" +
                "</P>\n" +
                "</SECTION>\n" +
                "<LENGTH>\n" +
                "<P>\n" +
                "101 words\n" +
                "</P>\n" +
                "</LENGTH>\n" +
                "<HEADLINE>\n" +
                "<P>OUTTAKES: MATERIAL MOLL\n" +
                "</P>\n" +
                "</HEADLINE>\n" +
                "<BYLINE>\n" +
                "<P>\n" +
                "By Craig Modderno\n" +
                "</P>\n" +
                "</BYLINE>\n" +
                "<TEXT>\n" +
                "<P>\n" +
                "Madonna and Beatty?\n" +
                "</P>\n" +
                "</TEXT>\n" +
                "<TYPE>\n" +
                "<P>\n" +
                "Column\n" +
                "</P>\n" +
                "</TYPE>\n" +
                "</DOC>";
        IndexEngine.writeCompressedRawDoc(outputDirName, "", docno, rawDoc);
    }
}