package strategy.arbitrage;

import api.Currency;

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

    static double getArbitrageRatio(CurrencyInfo bidSide, CurrencyInfo askSide) {
        double buyingPrice = askSide.asks.get(0).getRate();
        double sellingPrice = bidSide.bids.get(0).getRate();
        double requiredSellingPrice = buyingPrice / ((1 - bidSide.takerFee) * (1 - askSide.takerFee));
        ArbitrageUtils.logAtLevel("Buy (Lowest Ask): " + buyingPrice + "\tSell (HighestBid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice + "\t\tRatio: " + sellingPrice / requiredSellingPrice , 1);
        return sellingPrice / requiredSellingPrice;
    }
}
