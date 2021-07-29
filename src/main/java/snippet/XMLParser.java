package snippet;

import interactive.InteractiveEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XMLParser {
    public static void main(String[] args) throws IOException {
        SnippetEngine snippetEngine = new SnippetEngine("123");
        String res = snippetEngine.getSnippet(InteractiveEngine.getWholeContentFromGzipReader(
                "/home/xuzishuo1996/Desktop/msci720-hw/latime_index/raw/89/01/01/LA010189-0001"));
//        System.out.println(res);
    }

    // use XML libraries
//    public static void main(String[] args) {
//        try {
//            // Instantiate the Factory
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//
//            // parse XML file
//            DocumentBuilder db = dbf.newDocumentBuilder();
//
//            Document doc = db.parse(InteractiveEngine.getWholeContentFromGzipReader(
//                    "/home/xuzishuo1996/Desktop/msci720-hw/latime_index/raw/89/01/01/LA010189-0001"));
//
//            // optional
////            doc.getDocumentElement().normalize();
//            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
//            System.out.println("------");
//
//            // get <staff>
//            NodeList list = doc.getElementsByTagName("HEADLINE");
//
//            int a = 1;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
