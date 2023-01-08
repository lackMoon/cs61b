package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> noResizing = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        int[] N = new int[]{4,5,6};
        for (int n : N) {
            noResizing.addLast(n);
            buggyAList.addLast(n);
        }
        assertEquals(noResizing.size(),buggyAList.size());
        assertEquals(noResizing.removeLast(),buggyAList.removeLast());
        assertEquals(noResizing.removeLast(),buggyAList.removeLast());
        assertEquals(noResizing.removeLast(),buggyAList.removeLast());
    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> BL = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                BL.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                assertEquals(L.size(),BL.size());
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() > 0 && BL.size() > 0) {
                    assertEquals(L.getLast(),BL.getLast());
                }
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() > 0 && BL.size() > 0) {
                    assertEquals(L.removeLast(),BL.removeLast());
                }
            }
        }
    }

}
