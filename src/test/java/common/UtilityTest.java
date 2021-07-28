package common;

import index.IndexEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilityTest {
    File projRootDir = new File(new File("./").getCanonicalPath());
    String docno;

    String metadataPath = "/home/xuzishuo1996/waterloo/msci720-hw/latime_index/metadata/";

    UtilityTest() throws IOException {
    }

    @BeforeEach
    void setUp() {
        docno = "LA042990-0001";    // 1990-04-29
    }

    @Test
    void getYearTest() {
        assertEquals("90", Utility.getYear(docno));
    }

    @Test
    void getMonthTest() {
        assertEquals("04", Utility.getMonth(docno));
    }

    @Test
    void getDayTest() {
        assertEquals("29", Utility.getDay(docno));
    }

    @Test
    void getDateFolderHierarchyTest() {
        assertEquals("90/04/29", Utility.getDateFolderHierarchy(docno));
    }

    @Test
    void getNaturalDateTest() {
        assertEquals("April 29, 1990", Utility.getNaturalDate(docno));
    }

    @Test
    void getLexiconAndInvertedIndex() throws IOException {
        HashMap<String, Integer> lexiconTermToId = (HashMap<String, Integer>) Utility.deserialize(metadataPath + "lexiconTermToId.ser");
        List<List<Integer>> invertedIndex = (List<List<Integer>>) Utility.deserialize(metadataPath + "invertedIndex.ser");
        int a = 1;
    }

    @Test
    void indexSerialize() throws IOException {
        List<List<Integer>> index = new ArrayList<>();
        index.add(Arrays.asList(1,2,3));
        index.add(Arrays.asList(2,3,5,6));
        IndexEngine.serializeInvertedIndex("./", index);

        List<List<Integer>> deserializedIndex = (List<List<Integer>>) Utility.deserialize("invertedIndex.ser");
        System.out.println(deserializedIndex.size());
    }
}