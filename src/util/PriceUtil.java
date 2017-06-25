package util;

/**
 * Created by Timothy on 6/24/17.
 */
public final class PriceUtil {

    public static double getPercentChange(final double start, final double end) {
        // TODO(stfinancial): How to handle end being 0?
        return ((end - start) / start) * 100;
    }
}
