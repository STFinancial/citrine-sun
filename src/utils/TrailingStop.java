package utils;

import api.*;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

import java.math.BigDecimal;

/**
 * Created by Timothy on 3/2/17.
 */
public class TrailingStop implements Runnable {
    // TODO(stfinancial): For really small prices, we need to be careful of rounding.

    // TODO(stfinancial): Not sure if this should be runnable itself or whether it should be added to something with a thread pool that can keep tabs.
    // in the future we may want tightening stop. (higher it goes, tighter the stop)
    // TODO(stfinancial): Optionally take a Trade that we want to continue doing.

    private final Market market;
    private final CurrencyPair pair;
    // Whether or not to continually buy/sell on the way up/down to maintain the same leverage
    private boolean maintainFraction;
    // The fraction of our holdings we may use in leverage. Maintain Buys and sells will not be executed if our trades are above this fraction.
    private double maintenanceFraction;
    // margin or not
    private boolean isMargin; // TODO(stfinancial): Currently assuming margin.
    // trailing stop fraction
    private double stopFraction;
    // trailing limit fraction
    private double limitFraction;

    // maintain our current positions already. (How would this work with holdingfraction)
    private TrailingStop(Builder builder) {
        this.market = builder.market;
        this.pair = builder.pair;
        this.maintainFraction = builder.maintainFraction;
        this.maintenanceFraction = builder.maintenanceFraction;
        this.isMargin = builder.isMargin;
        this.stopFraction = builder.stopFraction;
        this.limitFraction = builder.limitFraction;
    }

    @Override
    public void run() {

        // TODO(stfinancial): These checks are not true in the case of a long position.
        if (stopFraction > 1) {
            System.out.println("Cannot have a stop fraction greater than 1");
            return;
        }
        if (limitFraction > 1) {
            System.out.println("Cannot have a limit fraction greater than 1");
            return;
        }

        // TODO(stfinancial): Think about stop and limit checks if the fractions are below 0?

        MarketResponse resp;
        MarginPosition pos;
        MarginAccountSummary summary;
        Ticker ticker;


        while (true) {
            // TODO(stfinancial): Take care of what happens if stop is hit.
            if (isMargin) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    continue;
                }
                resp = market.processMarketRequest(new MarginPositionRequest(pair));
                if (!resp.isSuccess()) {
                    continue;
                }
                pos = ((MarginPositionResponse) resp).getPosition();
                resp = market.processMarketRequest(new MarginAccountSummaryRequest());
                if (!resp.isSuccess()) {
                    continue;
                }
                summary = ((MarginAccountSummaryResponse) resp).getSummary();


                // TODO(stfinancial): Watch out for negative numbers.
                if (maintainFraction) {
                    // If we want to maintain fraction, then we need to get the BTC value of where we want to be
                    double currentFraction = pos.getTotal() / summary.getNetValue();
                    if (currentFraction < maintenanceFraction) {
                        // Then we need to make trades to get to the maintenanceFraction, we probably want to get a shallow order book for this

                    } else {
                        // Then we shouldn't do anything...
                    }
                } else {
                    // Then all we should do is adjust the stop.
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Do nothing probably
                }


                // Get our position summary again
                resp = market.processMarketRequest(new MarginPositionRequest(pair));
                if (!resp.isSuccess()) {
                    continue;
                }
                pos = ((MarginPositionResponse) resp).getPosition();

                resp = market.processMarketRequest(new TickerRequest());
                if (!resp.isSuccess()) {
                    continue;
                }
                ticker = ((TickerResponse) resp).getTickers().get(pair);

                // Set/move trailing stop limit order


                // This is the new stop limit order.
                // Get price at whatever stop percent we want and limit we want
                double stopPrice;
                double limitPrice;
                double amount;
                TradeType tradeType;
                if (pos.getType() == MarginType.LONG) {
                    // If our position is a long, then we will need to do a stop limit sell.
                    // In a stop limit sell, the stop triggers if the highest bid drops to or below the stop price

                    stopPrice = ticker.getHighestBid() * (1 - stopFraction);
                    limitPrice = ticker.getHighestBid() * (1 - limitFraction);
                    amount = pos.getAmount();
                    tradeType = TradeType.SELL;
                } else {

                    stopPrice = ticker.getLowestAsk() * (1 + stopFraction);
                    limitPrice = ticker.getHighestBid() * (1 + limitFraction);
                    amount = pos.getAmount() * -1;
                    tradeType = TradeType.BUY;
                }



                // Amount is equal to amount of our margin position. (Watch out for negative numbers)
                Trade t = new Trade(amount, limitPrice, pair, tradeType);
                TradeRequest req = new TradeRequest(t);
                req.setIsMargin(true);
                req.setStop(stopPrice);
                market.processMarketRequest(req);

                // TODO(stfinancial): How do we respond on failure?

            } else {
                // Currently not supported.
            }

        }


    }

    // TODO(stfinancial): I'm not really sure this is necessary. We can just let someone set these parameters in the class.
    public static class Builder {
        private final Market market;
        private final CurrencyPair pair;
        private double maintenanceFraction;
        private boolean maintainFraction;
        private boolean isMargin;
        private double stopFraction;
        private double limitFraction;

        public Builder(Market market, CurrencyPair pair) {
            this.market = market;
            this.pair = pair;
        }

        public Builder maintenanceFraction(double maintenanceFraction) {
            this.maintenanceFraction = maintenanceFraction;
            this.maintainFraction = true;
            return this;
        }

        public Builder maintainFraction(boolean maintainFraction) {
            this.maintainFraction = maintainFraction;
            return this;
        }

        public Builder isMargin(boolean isMargin) {
            this.isMargin = isMargin;
            return this;
        }

        public Builder stopFraction(double stopFraction) {
            this.stopFraction = stopFraction;
            return this;
        }

        public Builder limitFraction(double limitFraction) {
            this.limitFraction = limitFraction;
            return this;
        }

        public TrailingStop build() {
            return new TrailingStop(this);
        }
    }
}
