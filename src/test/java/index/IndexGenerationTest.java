package index;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

class IndexGenerationTest {

    @Test
    void extractAlphanumerics() {
        List<String> res = IndexGeneration.extractAlphanumerics("./Processor /home/xuzishuo1996/waterloo/msci720-hw/hw1/tests/latimes.gz /home/xuzishuo1996/waterloo/msci720-hw/latime_index\n");
        for (String s : res) {
            System.out.println(s);
        }
    }

    @Test
    void serializePrimitiveArray() throws Exception {
        // Serialize an int[][]
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("test.ser"));
        int[][] a = new int[2][3];
        a[0] = new int[]{0, 1, 2};
        a[1] = new int[]{3, 4, 5};
        out.writeObject(a);
        out.flush();
        out.close();

        // Deserialize the int[]
        ObjectInputStream in = new ObjectInputStream(new FileInputStream("test.ser"));
        int[][] arr = (int[][]) in.readObject();
        in.close();

        // Print out contents of deserialized int[]
        // System.out.println("It is " + (array instanceof Serializable) + " that int[] implements Serializable");
        System.out.print("Deserialized array:\n");
        for (int[] row : arr) {
            for (int elem : row) {
                System.out.print(elem + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}