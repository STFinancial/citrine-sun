package util;

/**
 * Created by Timothy on 5/11/17.
 */
// TODO(stfinancial): Templatize this.
public class MovingAverage {
    private final int size;
    private double[] nums;

    private int index = 0;
    private int currentSize = 0;
    private double sum = 0;
    private double average = 0;

    public MovingAverage(int size) {
        this.size = size;
        this.nums = new double[size];
    }

    public void add(double num) {
        double numToRemove = nums[index];
        nums[index] = num;
        sum += num - numToRemove;

        if (++index == size) {
            index = 0;
        }
        if (currentSize != size) {
            ++currentSize;
        }
        average = sum / currentSize;
    }

    public double getMovingAverage() {
        return average;
    }

}
