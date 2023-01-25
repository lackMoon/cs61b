package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import jh61b.junit.In;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.ArrayList;
import java.util.List;

public class TestArrayDequeEC {
    @Test
    public void dequeTest() {
        StudentArrayDeque<Integer> std = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ad = new ArrayDequeSolution<>();
        List<String> callSequence = new ArrayList<>();
        for (int i = 0; i < 50; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();
            if (numberBetweenZeroAndOne < 0.25) {
                std.addFirst(i);
                ad.addFirst(i);
                callSequence.add("addFirst(" + i + ")");
            } else if (numberBetweenZeroAndOne >= 0.25 && numberBetweenZeroAndOne < 0.5) {
                std.addLast(i);
                ad.addLast(i);
                callSequence.add("addLast(" + i + ")");
            } else if (numberBetweenZeroAndOne >= 0.5 && numberBetweenZeroAndOne < 0.75) {
                Integer sv = std.isEmpty() ? null : std.removeFirst();
                Integer av = ad.isEmpty() ? null : ad.removeFirst();
                callSequence.add("removeFirst()");
                assertEquals(String.join("\n", callSequence), av, sv);
            } else {
                Integer sv = std.isEmpty() ? null : std.removeLast();
                Integer av = ad.isEmpty() ? null : ad.removeLast();
                callSequence.add("removeLast()");
                assertEquals(String.join("\n", callSequence), av, sv);
            }
        }
    }
}
