package strategy.arbitrage;


import api.*;
import api.Currency;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import strategy.Strategy;
import util.MovingAverage;

import java.util.*;

class SlowArbitrageStrategy2 extends Strategy {
    static final int DEBUG = 3;
    static final double HUNDRED_MILLION = 100000000;
    static final double SATOSHI = 0.00000001;

    // TODO(stfinancial): Use moving averages to determine how often to get the order books.

    // TODO(stfinancial): IOC both trades and have a "holdover" potentially... HIGH PRIORITY!!!!.
    // TODO(stfinancial): See if we can fix 0.01 gdax issue.
    // TODO(stfinancial): Consider setting secondary rate to the lowest possible arbitrage rate. Better avoids orders that don't fill.
    // TODO(stfinancial): "Volume" bonus. To help reduce fees, increase magnitude on low volume days.
    // TODO(stfinancial): Priority "direction" allowing us to prefer buying or selling first.
    // TODO(stfinancial): Min arbitrage ratio.
    // TODO(stfinancial): Instead of setting limit at exact price, set it at lowest arbitrage price, that will allow fewer unfilled orders.
    // TODO(stfinancial): Priority currencypair/market if one arbitrage is really high or the others are non-existent.
    // TODO(stfinancial): If scaled amount is less than min amount, use min amount (before accounting for balances).

    // TODO(stfinancial): When there are multiple markets and currency pairs. Apply the adjustments to find the highest expected profit.

    private static final String POLO_KEY = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEY = "/Users/Timothy/Documents/Keys/gdax_key.txt";
//    private static final String POLO_KEY = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
//    private static final String GDAX_KEY = "F:\\Users\\Zarathustra\\Documents\\gdax_key.txt";

    // TODO(stfinancial): Replace this with an amount based on account balance.
    private static final Map<CurrencyPair, Double> PAIRS = Collections.unmodifiableMap(new HashMap<CurrencyPair, Double>() {{
        put(CurrencyPair.of(Currency.LTC, Currency.BTC), 3.6);
        put(CurrencyPair.of(Currency.ETH, Currency.BTC), 0.15);
    }});
    // TODO(stfinancial): Make this per-exchange?
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_ACCOUNT_ADJUSTMENT_RATIO = 25;

    private static final FeeRequest FEE_REQUEST = new FeeRequest(1, 1);
    private static final AccountBalanceRequest ACCOUNT_BALANCE_REQUEST = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);

    private static final int FEE_AND_BALANCE_INTERVAL = 50;
    private int feeAndBalanceCount = 500;
    private MovingAverage arbRatioMA500 = new MovingAverage(FEE_AND_BALANCE_INTERVAL);

    public static void main(String[] args) {
        SlowArbitrageStrategy2 strat = new SlowArbitrageStrategy2();
        strat.run();
    }

    @Override
    public void run() {
        MarketInfo polo = new MarketInfo();
        polo.market = new Poloniex(Credentials.fromFileString(POLO_KEY));
        polo.priority = 5;
        MarketInfo gdax = new MarketInfo();
        gdax.market = new Gdax(Credentials.fromFileString(GDAX_KEY));
        gdax.priority = 1;
        PAIRS.forEach((pair, amt) -> {
            CurrencyPairInfo c = new CurrencyPairInfo();
            c.minAmount = 0.005;
            polo.currencyPairInfos.put(pair, c);
            c = new CurrencyPairInfo();
            c.minAmount = 0.01;
            gdax.currencyPairInfos.put(pair, c);

        });
        checkForArbitrage(Arrays.asList(polo, gdax));
    }

    public void checkForArbitrage(List<MarketInfo> markets) {
        MarketResponse response;
        OrderBookResponse orderBookResponse;
        // TODO(stfinancial): Right now we will hardcode that we are using 2 exchanges.
        MarketInfo market1 = markets.get(0);
        MarketInfo market2 = markets.get(1);
        MarketInfo askSide;
        MarketInfo bidSide;
        while (true) {
            ArbitrageUtils.sleep(400);
            if (!maybeUpdateFeesAndBalances(markets)) { continue; }

            /* Get new order books */
            double arbitrageRatio1;
            double arbitrageRatio2;
            double arbitrageRatio;
            for (CurrencyPair pair : PAIRS.keySet()) {
                ArbitrageUtils.logAtLevel("Pair: " + pair.toString(), 1);
                for (MarketInfo market : markets) {
                    while (!(response = market.market.processMarketRequest(new OrderBookRequest(pair, 20, 2, 1))).isSuccess()) {
                        ArbitrageUtils.logAtLevel("(" + market.market.getName() + ") " + "Failed Orderbook Request, Sleeping...: " + response.getJsonResponse(), 1);
                        ArbitrageUtils.sleep(500);
                    }
                    orderBookResponse = (OrderBookResponse) response;
                    // TODO(stfinancial): What is the guarantee of this method if we don't get a proper order book.
                    market.currencyPairInfos.get(pair).bids = orderBookResponse.getBids().get(pair);
                    market.currencyPairInfos.get(pair).asks = orderBookResponse.getAsks().get(pair);
                }
                /* Check for arbitrages */
                // TODO(stfinancial): Need a more generic way to do this in the future, especially with futures and sockets and such.
                arbitrageRatio1 = ArbitrageUtils.getArbitrageRatio(market1.currencyPairInfos.get(pair), market2.currencyPairInfos.get(pair));
                arbitrageRatio2 = ArbitrageUtils.getArbitrageRatio(market2.currencyPairInfos.get(pair), market1.currencyPairInfos.get(pair));
                if (arbitrageRatio1 >= arbitrageRatio2) {
                    arbitrageRatio = arbitrageRatio1;
                    bidSide = market1;
                    askSide = market2;
                } else {
                    arbitrageRatio = arbitrageRatio2;
                    bidSide = market2;
                    askSide = market1;
                }
                arbRatioMA500.add(arbitrageRatio);
                ArbitrageUtils.logAtLevel(String.valueOf(arbRatioMA500.getMovingAverage()), 4);
                maybeDoArbitrage(bidSide, askSide, pair, arbitrageRatio);
                ArbitrageUtils.sleep(250);
            }
        }
    }

    private void maybeDoArbitrage(MarketInfo bidSide, MarketInfo askSide, CurrencyPair pair, double arbitrageRatio) {
        if (arbitrageRatio > 1) {
            CurrencyPairInfo bidSidePairInfo = bidSide.currencyPairInfos.get(pair);
            CurrencyPairInfo askSidePairInfo = askSide.currencyPairInfos.get(pair);
            Trade lowestAsk = askSidePairInfo.asks.get(0);
            Trade highestBid = bidSidePairInfo.bids.get(0);
            ArbitrageUtils.logAtLevel("Arbitrage Found with Ratio: " + arbitrageRatio + " on pair " + pair.toString(), 2);
            ArbitrageUtils.logAtLevel("Buy (" + askSide.market.getName() + ") - " + lowestAsk.getAmount() + " - " + lowestAsk.getRate(), 2);
            ArbitrageUtils.logAtLevel("Sell (" + bidSide.market.getName() + ") - " + highestBid.getAmount() + " - " + highestBid.getRate(), 2);

            double scaledAmount = Math.max(askSidePairInfo.minAmount, Math.max(bidSidePairInfo.minAmount, Math.floor(PAIRS.get(pair) * getMultiplier(bidSide, askSide, pair, arbitrageRatio) * HUNDRED_MILLION) / HUNDRED_MILLION));

            double askSideTradeAmount = lowestAsk.getAmount();
            // TODO(stfinancial): Make sure this is updated when we search more than 1 order deep.
            double askSideBalanceAmount = Math.floor((askSide.balances.get(pair.getQuote()) / lowestAsk.getRate()) * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
            double askSideMinAmount = Math.min(scaledAmount, Math.min(askSideBalanceAmount, askSideTradeAmount));
            double askSidePostFeeMinAmount = askSideMinAmount * (1 - askSidePairInfo.takerFee);


            double bidSideTradeAmount = highestBid.getAmount();
            double bidSideBalanceAmount = Math.floor(bidSide.balances.get(pair.getBase()) * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
            double bidSideMinAmount = Math.min(scaledAmount, Math.min(bidSideTradeAmount, bidSideBalanceAmount));
            double bidSidePostFeeMinAmount = bidSideMinAmount * (1 - bidSidePairInfo.takerFee);


            double bidAmount;
            double askAmount;
            if (askSidePostFeeMinAmount < bidSidePostFeeMinAmount) {
                bidAmount = Math.floor((askSidePostFeeMinAmount / (1 - bidSidePairInfo.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
                askAmount = askSideMinAmount;
            } else {
                bidAmount = bidSideMinAmount;
                askAmount = Math.floor((bidSidePostFeeMinAmount / (1 - askSidePairInfo.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
            }

            if (askAmount < askSidePairInfo.minAmount) {
                // TODO(stfinancial): This log doesn't make much sense.
                ArbitrageUtils.logAtLevel("Ask Side (" + askSide.market.getName() + ") amount " + askAmount + " is lower than global min: " + askSideMinAmount, 2);
                return;
            }

            if (bidAmount < bidSidePairInfo.minAmount) {
                ArbitrageUtils.logAtLevel("Bid Side (" + bidSide.market.getName() + ") amount " + bidAmount + " is lower than global min: " + bidSideMinAmount, 2);
                return;
            }

            ArbitrageUtils.logAtLevel(askSide.market.getName() + " Amount: " + askAmount, 2);
            ArbitrageUtils.logAtLevel(bidSide.market.getName() + " Amount: " + bidAmount, 2);

                /* Make the trades */
            if (askSide.priority > bidSide.priority) {
                if (!placeTrades(askSide, new Trade(askAmount, lowestAsk.getRate(), pair, TradeType.BUY), bidSide, new Trade(bidAmount, highestBid.getRate(), pair, TradeType.SELL))) {
                    ArbitrageUtils.logAtLevel("Failure placing trades. Stopping.", 1);
                    return;
                }
            } else {
                if (!placeTrades(bidSide, new Trade(bidAmount, highestBid.getRate(), pair, TradeType.SELL), askSide, new Trade(askAmount, lowestAsk.getRate(), pair, TradeType.BUY))) {
                    ArbitrageUtils.logAtLevel("Failure placing trades. Stopping.", 1);
                    return;
                }
            }
        }
    }

    private boolean placeTrades(MarketInfo priority, Trade priorityTrade, MarketInfo secondary, Trade secondaryTrade) {
        MarketResponse response;
        TradeResponse tradeResponse;
        CurrencyPair pair = priorityTrade.getPair();
        CurrencyPairInfo prioritySidePairInfo = priority.currencyPairInfos.get(pair);
        CurrencyPairInfo secondarySidePairInfo = secondary.currencyPairInfos.get(pair);

        TradeRequest request = new TradeRequest(priorityTrade, 5, 5);
        request.setIsMarket(false);
        request.setIsPostOnly(false);
        request.setTimeInForce(TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL);
        response = priority.market.processMarketRequest(request);
        if (!response.isSuccess()) {
            ArbitrageUtils.logAtLevel("Failure placing primary trade on " + priority.market.getName() + ": " + response.getJsonResponse(), 1);
            // This is recoverable, just act like it didn't happen, though returning true may be confusing.
            return true;
        }
        tradeResponse = (TradeResponse) response;
        if (priorityTrade.getType() == TradeType.BUY) {
            priority.balances.put(pair.getBase(), priority.balances.getOrDefault(pair.getBase(), 0.0) + tradeResponse.getBaseAmountFilled());
            priority.balances.put(pair.getQuote(), priority.balances.getOrDefault(pair.getQuote(), 0.0) - tradeResponse.getQuoteAmountFilled());
        } else {
            priority.balances.put(pair.getBase(), priority.balances.getOrDefault(pair.getBase(), 0.0) - tradeResponse.getBaseAmountFilled());
            priority.balances.put(pair.getQuote(), priority.balances.getOrDefault(pair.getQuote(), 0.0) + tradeResponse.getQuoteAmountFilled());
        }
        double filledAmount = tradeResponse.getBaseAmountFilled();
        ArbitrageUtils.logAtLevel("Amount filled on " + priority.market.getName() + ": " + filledAmount, 2);

        double secondaryAmount;
        if (filledAmount == priorityTrade.getAmount() || filledAmount + SATOSHI == priorityTrade.getAmount()) {
            // The right amount was filled.
            ArbitrageUtils.logAtLevel("Correct amount filled, placing amount on secondary: " + secondaryTrade.getAmount(), 2);
            request = new TradeRequest(secondaryTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                ArbitrageUtils.logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) + secondaryTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) - (secondaryTrade.getAmount() * secondaryTrade.getRate()));
            } else {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) - secondaryTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) + (secondaryTrade.getAmount() * secondaryTrade.getRate()));
            }
            // TODO(stfinancial): Check that we filled the right amount?
            return true;
        }
        if (filledAmount == 0.0) {
            ArbitrageUtils.logAtLevel("No trades filled, so none required.", 2);
            return true;
        }
        if (filledAmount * (1 - prioritySidePairInfo.takerFee) / (1 - secondarySidePairInfo.takerFee) < MIN_AMOUNT) {
            // TODO(stfinancial): See if it is closer to 0 or min amount after accounting for arb ratio. Place trade accordingly.
            // TODO(stfinancial): Other option is to hold amount over until next trade and just include it in that.
            ArbitrageUtils.logAtLevel("Priority filled amount " + filledAmount + " leads to fee adjusted secondary amount " + filledAmount * (1 - prioritySidePairInfo.takerFee) / (1 - secondarySidePairInfo.takerFee) + " below minimum amount.", 1);
            // TODO(stfinancial): Ignore this for now.
            return true;
        } else if (filledAmount + SATOSHI < priorityTrade.getAmount()) {
            ArbitrageUtils.logAtLevel("Alternate amount was filled: " + filledAmount + "  Constructing new trade.", 2);
            secondaryAmount = Math.floor((filledAmount * (1 - prioritySidePairInfo.takerFee) / (1 - secondarySidePairInfo.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
            Trade revisedTrade = new Trade(secondaryAmount, secondaryTrade.getRate(), secondaryTrade.getPair(), secondaryTrade.getType());
            request = new TradeRequest(revisedTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            ArbitrageUtils.logAtLevel("Placing alternate amount: " + filledAmount, 2);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                ArbitrageUtils.logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) + revisedTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) - (revisedTrade.getAmount() * revisedTrade.getRate()));
            } else {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) - revisedTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) + (revisedTrade.getAmount() * revisedTrade.getRate()));
            }
            return true;
        }
        // A few cases here:
            // We fill an amount smaller than MIN_AMOUNT
                // One solution for this is check whether its closer to 0 or MIN_AMOUNT (slightly adjusted by arb ratio) and do whatever is closer.
            // We fill an amount smaller than the balance in secondaryTrade
                // Should use the balance we have left.
            // We fill exactly the right amount
                // Check that the secondary amount is the same as the amount available in the trade.
                // Round up or down by one satoshi if needed (check
        // TODO(stfinancial): This can occur if there are multiple "fills" due to rounding error. Alternatively can check amountUnfilled.
        filledAmount = Math.floor(filledAmount * HUNDRED_MILLION) / HUNDRED_MILLION;
        if (filledAmount <= priorityTrade.getAmount()) {
            // The right amount was filled.
            ArbitrageUtils.logAtLevel("Correct (rounded) amount filled, placing amount on secondary: " + secondaryTrade.getAmount(), 2);
            request = new TradeRequest(secondaryTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                ArbitrageUtils.logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) + secondaryTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) - (secondaryTrade.getAmount() * secondaryTrade.getRate()));
            } else {
                secondary.balances.put(pair.getBase(), secondary.balances.getOrDefault(pair.getBase(), 0.0) - secondaryTrade.getAmount());
                secondary.balances.put(pair.getQuote(), secondary.balances.getOrDefault(pair.getQuote(), 0.0) + (secondaryTrade.getAmount() * secondaryTrade.getRate()));
            }
            // TODO(stfinancial): Check that we filled the right amount?
            return true;
        }
        ArbitrageUtils.logAtLevel("AmountFilled: " + filledAmount + " is greater than expected amount: " + priorityTrade.getAmount(), 1);
        return false;

    }

    private double getMultiplier(MarketInfo bidSide, MarketInfo askSide, CurrencyPair pair, double arbitrageRatio) {
        CurrencyPairInfo askPairInfo = askSide.currencyPairInfos.get(pair);
        CurrencyPairInfo bidPairInfo = bidSide.currencyPairInfos.get(pair);
//        double amount = STANDARD_AMOUNT;
        // TODO(stfinancial): Not sure it makes a lot of sense to use fees here. This ratio increases as fees are lower, which we don't necessarily want.
        double arbitrageMultiplier = Math.pow((arbitrageRatio - ((1 - bidPairInfo.takerFee) * (1 - askPairInfo.takerFee))) * 100, 2);
        ArbitrageUtils.logAtLevel("ArbitrageMultiplier: " + arbitrageMultiplier, 3);

        double bidBase = bidSide.balances.get(pair.getBase()) * bidPairInfo.bids.get(0).getRate();
        double bidQuote = bidSide.balances.get(pair.getQuote());
        if (bidQuote == 0) {
            ArbitrageUtils.logAtLevel("Bid Side Quote Balance is 0. Setting max adjustment ratio.", 3);
            return MAX_ACCOUNT_ADJUSTMENT_RATIO;
        }
        double bidRatio = bidBase / bidQuote;

        double askBase = askSide.balances.get(pair.getBase()) * askPairInfo.asks.get(0).getRate();
        double askQuote = askSide.balances.get(pair.getQuote());
        if (askBase == 0) {
            ArbitrageUtils.logAtLevel("Ask Side Base Balance is 0. Setting max adjustment ratio.", 3);
            return MAX_ACCOUNT_ADJUSTMENT_RATIO;
        }
        double askRatio = askQuote / askBase;
        // TODO(stfinancial): This should be weighted by total magnitude of funds.
        double rebalanceMultiplier = Math.min(MAX_ACCOUNT_ADJUSTMENT_RATIO, Math.sqrt(askRatio * bidRatio));
        ArbitrageUtils.logAtLevel("RebalanceMultiplier: " + rebalanceMultiplier, 3);
        return arbitrageMultiplier * rebalanceMultiplier;
    }

//    private double getArbitrageRatio(CurrencyPairInfo bidSide, CurrencyPairInfo askSide) {
//        double buyingPrice = askSide.asks.get(0).getRate();
//        double sellingPrice = bidSide.bids.get(0).getRate();
//        double requiredSellingPrice = buyingPrice / ((1 - bidSide.takerFee) * (1 - askSide.takerFee));
//        ArbitrageUtils.logAtLevel("Buy (Lowest Ask): " + buyingPrice + "\tSell (HighestBid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice + "\t\tRatio: " + sellingPrice / requiredSellingPrice , 1);
//        return sellingPrice / requiredSellingPrice;
//    }

    /** @return false on request or response failure */
    private boolean maybeUpdateFeesAndBalances(List<MarketInfo> markets) {
        if (feeAndBalanceCount < FEE_AND_BALANCE_INTERVAL) {
            ArbitrageUtils.logAtLevel("FeeAndBalanceCount: " + feeAndBalanceCount, 4);
            ++feeAndBalanceCount;
            return true;
            // TODO(stfinancial): Uncomment once we have naive balance approximations. Raise interval as well.
//        } else if (arbRatioMA500.getMovingAverage() > 0.9965) {
//            return true;
        }
        ArbitrageUtils.logAtLevel("Updating fees and balances.", 3);
        MarketResponse response;
        for (MarketInfo m : markets) {
            response = m.market.processMarketRequest(FEE_REQUEST);
            if (!response.isSuccess()) {
                ArbitrageUtils.logAtLevel(m.market.getName() + " fee request failed: " + response.getJsonResponse(), 1);
                return false;
            }
            FeeResponse feeResponse = (FeeResponse) response;
            for (CurrencyPair pair : PAIRS.keySet()) {
                System.out.println(feeResponse.getJsonResponse());
                // TODO(stfinancial): Instability at this line, need to figure out why or use optional.
                try {
                    m.currencyPairInfos.get(pair).takerFee = feeResponse.getFeeInfo(pair).getTakerFee();
                } catch (NullPointerException e) {
                    m.currencyPairInfos.get(pair).takerFee = 0.003;
                }

            }
            ArbitrageUtils.sleep(250);
            response = m.market.processMarketRequest(ACCOUNT_BALANCE_REQUEST);
            if (!response.isSuccess()) {
                ArbitrageUtils.logAtLevel(m.market.getName() + " account balance request failed: " + response.getJsonResponse(), 1);
                return false;
            }
            Map<Currency, Double> balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
            m.balances = balances;
            for (CurrencyPair pair : PAIRS.keySet()) {
                if (!m.balances.containsKey(pair.getBase())) {
                    m.balances.put(pair.getBase(), 0.0);
                }
                if (!m.balances.containsKey(pair.getQuote())) {
                    m.balances.put(pair.getQuote(), 0.0);
                }
            }
        }
        feeAndBalanceCount = 0;
        return true;
    }
}
