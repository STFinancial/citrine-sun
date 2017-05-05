package strategy;


import api.Market;
import api.tmp_trade.Trade;

import java.util.List;

public class SlowArbitrageStrategy2 extends Strategy {

    @Override
    public void run() {

    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class MarketInfo {
        Market market;
        double takerFee;
        double quoteBalance;
        double baseBalance;
        List<Trade> asks;
        List<Trade> bids;
    }
}
