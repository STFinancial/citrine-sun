package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Timothy on 1/23/17.
 */
public class randtest {
    private Random random = new Random();

    public static void main(String[] args) {
        randtest r = new randtest();
        r.test();
    }

    private void test() {
        List<Double> rands = getTradeRandomization(1000);
        int[] counts = new int[1000];
        for (double rand : rands) {
            counts[(int) rand]++;
        }
        for (int count : counts) {
            System.out.println(count);
        }
    }

    private List<Double> getTradeRandomization(int numBuckets) {
        ArrayList<Double> nums = new ArrayList<>(numBuckets + 1);
        for (int i = 0; i < numBuckets - 1; i++) {
            nums.add(random.nextDouble() * (double) numBuckets);
        }
        nums.add(0.0);
        nums.add((double) numBuckets);
        Collections.sort(nums);
        ArrayList<Double> randFactors = new ArrayList<>(numBuckets);
        for (int i = 0; i < numBuckets; i++) {
            randFactors.add(nums.get(i+1) - nums.get(i));
        }
        return randFactors;
    }
}
