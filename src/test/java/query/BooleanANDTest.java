package query;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BooleanANDTest {

    @Test
    void comparingIntTest() {
        List<List<Integer>> lst = new ArrayList<>();
        lst.add(Arrays.asList(1,2,3,4,5));
        lst.add(Arrays.asList(1,2,3));
        lst.add(Arrays.asList(1,2,3,4));
        lst.sort(Comparator.comparingInt(List::size));
        assertEquals(3, lst.get(0).size());
        assertEquals(4, lst.get(1).size());
        assertEquals(5, lst.get(2).size());
    }
}