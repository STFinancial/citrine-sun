package strategy.arbitrage;

/**
 * Created by Timothy on 5/31/17.
 */
public class ArbitrageUtils {


    static void logAtLevel(String message, int debugLevel) {
        if (SlowArbitrageStrategy2.DEBUG >= debugLevel) {
            System.out.println(message);
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static double getArbitrageRatio(CurrencyPairInfo bidSide, CurrencyPairInfo askSide) {
        double buyingPrice = askSide.asks.get(0).getRate();
        double sellingPrice = bidSide.bids.get(0).getRate();
        double requiredSellingPrice = buyingPrice / ((1 - bidSide.takerFee) * (1 - askSide.takerFee));
        ArbitrageUtils.logAtLevel("Ratio: " + sellingPrice / requiredSellingPrice + "\t\tBuy (Lowest Ask): " + buyingPrice + "\tSell (HighestBid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice, 3);
        return sellingPrice / requiredSellingPrice;
    }
}
