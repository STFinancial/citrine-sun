package test;

import java.util.*;
import java.util.jar.Pack200;

/**
 * Created by Timothy on 11/26/16.
 */
public class spreadtest {
    private static final int TRIALS = 100000;
    public static void main(String[] args) {
        int start = 1;
        int end = -1;
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        int numSteps;
        int currentValue;
        LinkedList<Integer> counts = new LinkedList<>();
        for (int i = 0; i < TRIALS; i++) {
            currentValue = start;
            numSteps = 0;
            while (numSteps < 10000 && currentValue != end) {
                ++numSteps;
                if (r.nextInt(99) < 50) {
                    --currentValue;
                } else {
                    ++currentValue;
                }
            }
            counts.add(numSteps);
        }
        Collections.sort(counts);
        int prev = counts.getFirst();
        int current;
        int count = 0;
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<Integer, Integer>();
        for (Integer i : counts) {
            current = i;
            if (prev == current) {
                ++count;
            } else {
                m.put(prev, count);
                count = 1;
            }
            prev = current;
        }
        m.put(prev, count);
        double prob = 0;
        double cumProb = 0;
        for (Map.Entry<Integer, Integer> e : m.entrySet()) {
            prob = e.getValue() / (double) TRIALS;
            cumProb += prob;

            System.out.println(e.getKey() + "\t" + e.getValue() + "\t" + prob + "\t" + cumProb);
        }
    }
}
