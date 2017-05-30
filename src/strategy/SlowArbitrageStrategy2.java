package strategy;


import api.*;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import util.MovingAverage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SlowArbitrageStrategy2 extends Strategy {
    // TODO(stfinancial): IOC both trades and have a "holdover" potentially... HIGH PRIORITY!!!!.
    // TODO(stfinancial): Estimate account balances.
    // TODO(stfinancial): See if we can fix 0.01 gdax issue.
    // TODO(stfinancial): Consider setting secondary rate to the lowest possible arbitrage rate. Better avoids orders that don't fill.
    // TODO(stfinancial): "Volume" bonus. To help reduce fees, increase magnitude on low volume days.
    // TODO(stfinancial): Priority "direction" allowing us to prefer buying or selling first.
    // TODO(stfinancial): Min arbitrage ratio.

    private static final String POLO_KEY = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEY = "/Users/Timothy/Documents/Keys/gdax_key.txt";
//    private static final String POLO_KEY = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
//    private static final String GDAX_KEY = "F:\\Users\\Zarathustra\\Documents\\gdax_key.txt";

    // TODO(stfinancial): Replace this with an amount based on account balance.
    private static final double STANDARD_AMOUNT = 0.531;
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);
//    private static final double STANDARD_AMOUNT = 0.2;
//    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.ETH, Currency.BTC);
    // TODO(stfinancial): Make this per-exchange?
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_ACCOUNT_ADJUSTMENT_RATIO = 100;
    private static final int DEBUG = 3;

    private static final OrderBookRequest ORDER_BOOK_REQUEST = new OrderBookRequest(PAIR, 20, 2, 1);
    private static final FeeRequest FEE_REQUEST = new FeeRequest(PAIR, 1, 1);
    private static final AccountBalanceRequest ACCOUNT_BALANCE_REQUEST = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);

    private static final int FEE_AND_BALANCE_INTERVAL = 10;
    private int feeAndBalanceCount = 500;
    private MovingAverage arbRatioMA500 = new MovingAverage(FEE_AND_BALANCE_INTERVAL);

    private static final double HUNDRED_MILLION = 100000000;
    private static final double SATOSHI = 0.00000001;

    public static void main(String[] args) {
        SlowArbitrageStrategy2 strat = new SlowArbitrageStrategy2();
        strat.run();
    }

    @Override
    public void run() {
        MarketInfo polo = new MarketInfo();
        polo.market = new Poloniex(Credentials.fromFileString(POLO_KEY));
        polo.priority = 5;
        polo.minAmount = 0.005;
        MarketInfo gdax = new MarketInfo();
        gdax.market = new Gdax(Credentials.fromFileString(GDAX_KEY));
        gdax.priority = 1;
        gdax.minAmount = 0.01;
        doArbitrage(Arrays.asList(polo, gdax));
    }

    public void doArbitrage(List<MarketInfo> markets) {
        MarketResponse response;
        OrderBookResponse orderBookResponse;
        // TODO(stfinancial): Right now we will hardcode that we are using 2 exchanges.
        MarketInfo market1 = markets.get(0);
        MarketInfo market2 = markets.get(1);
        MarketInfo askSide;
        MarketInfo bidSide;
        while (true) {
            sleep(400);
            if (!maybeUpdateFeesAndBalances(markets)) { continue; }

            /* Get new order books */
            for (MarketInfo market : markets) {
                while (!(response = market.market.processMarketRequest(ORDER_BOOK_REQUEST)).isSuccess()) {
                    logAtLevel("Failed Orderbook Request, Sleeping...: " + response.getJsonResponse(), 1);
                    sleep(500);
                }
                orderBookResponse = (OrderBookResponse) response;
                // TODO(stfinancial): What is the guarantee of this method if we don't get a proper order book.
                market.bids = orderBookResponse.getBids().get(PAIR);
                market.asks = orderBookResponse.getAsks().get(PAIR);
            }

            /* Check for arbitrages */
            double arbitrageRatio1 = getArbitrageRatio(market1, market2);
            double arbitrageRatio2 = getArbitrageRatio(market2, market1);
            double arbitrageRatio;
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
            logAtLevel(String.valueOf(arbRatioMA500.getMovingAverage()), 4);
            /* There is an arbitrage */
            if (arbitrageRatio > 1) {
                Trade lowestAsk = askSide.asks.get(0);
                Trade highestBid = bidSide.bids.get(0);
                logAtLevel("Arbitrage Found with Ratio: " + arbitrageRatio, 2);
                logAtLevel("Buy (" + askSide.market.getName() + ") - " + lowestAsk.getAmount() + " - " + lowestAsk.getRate(), 2);
                logAtLevel("Sell (" + bidSide.market.getName() + ") - " + highestBid.getAmount() + " - " + highestBid.getRate(), 2);

                double scaledAmount = Math.floor(getScaledAmount(bidSide, askSide, arbitrageRatio) * HUNDRED_MILLION) / HUNDRED_MILLION;

                double askSideTradeAmount = lowestAsk.getAmount();
                // TODO(stfinancial): Make sure this is updated when we search more than 1 order deep.
                double askSideBalanceAmount = Math.floor((askSide.quoteBalance / lowestAsk.getRate()) * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
                double askSideMinAmount = Math.min(scaledAmount, Math.min(askSideBalanceAmount, askSideTradeAmount));
                double askSidePostFeeMinAmount = askSideMinAmount * (1 - askSide.takerFee);


                double bidSideTradeAmount = highestBid.getAmount();
                double bidSideBalanceAmount = Math.floor(bidSide.baseBalance * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
                double bidSideMinAmount = Math.min(scaledAmount, Math.min(bidSideTradeAmount, bidSideBalanceAmount));
                double bidSidePostFeeMinAmount = bidSideMinAmount * (1 - bidSide.takerFee);


                double bidAmount;
                double askAmount;
                if (askSidePostFeeMinAmount < bidSidePostFeeMinAmount) {
                    bidAmount = Math.floor((askSidePostFeeMinAmount / (1 - bidSide.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
                    askAmount = askSideMinAmount;
                } else {
                    bidAmount = bidSideMinAmount;
                    askAmount = Math.floor((bidSidePostFeeMinAmount / (1 - askSide.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
                }

                if (askAmount < askSide.minAmount) {
                    // TODO(stfinancial): This log doesn't make much sense.
                    logAtLevel("Ask Side (" + askSide.market.getName() + ") amount " + askAmount + " is lower than global min: " + askSideMinAmount, 2);
                    continue;
                }

                if (bidAmount < bidSide.minAmount) {
                    logAtLevel("Bid Side (" + bidSide.market.getName() + ") amount " + bidAmount + " is lower than global min: " + bidSideMinAmount, 2);
                    continue;
                }

                logAtLevel(askSide.market.getName() + " Amount: " + askAmount, 2);
                logAtLevel(bidSide.market.getName() + " Amount: " + bidAmount, 2);

                /* Make the trades */
                if (askSide.priority > bidSide.priority) {
                    if (!placeTrades(askSide, new Trade(askAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), bidSide, new Trade(bidAmount, highestBid.getRate(), PAIR, TradeType.SELL))) {
                        logAtLevel("Failure placing trades. Stopping.", 1);
                        return;
                    }
                } else {
                    if (!placeTrades(bidSide, new Trade(bidAmount, highestBid.getRate(), PAIR, TradeType.SELL), askSide, new Trade(askAmount, lowestAsk.getRate(), PAIR, TradeType.BUY))) {
                        logAtLevel("Failure placing trades. Stopping.", 1);
                        return;
                    }
                }
            }
        }
    }

    private boolean placeTrades(MarketInfo priority, Trade priorityTrade, MarketInfo secondary, Trade secondaryTrade) {
        MarketResponse response;
        TradeResponse tradeResponse;

        TradeRequest request = new TradeRequest(priorityTrade, 5, 5);
        request.setIsMarket(false);
        request.setIsPostOnly(false);
        request.setTimeInForce(TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL);
        response = priority.market.processMarketRequest(request);
        if (!response.isSuccess()) {
            logAtLevel("Failure placing primary trade on " + priority.market.getName() + ": " + response.getJsonResponse(), 1);
            // This is recoverable, just act like it didn't happen, though returning true may be confusing.
            return true;
        }
        tradeResponse = (TradeResponse) response;
        if (priorityTrade.getType() == TradeType.BUY) {
            priority.baseBalance += tradeResponse.getBaseAmountFilled();
            priority.quoteBalance -= tradeResponse.getQuoteAmountFilled();
        } else {
            priority.baseBalance -= tradeResponse.getBaseAmountFilled();
            priority.quoteBalance += tradeResponse.getQuoteAmountFilled();
        }
        double filledAmount = tradeResponse.getBaseAmountFilled();
        logAtLevel("Amount filled on " + priority.market.getName() + ": " + filledAmount, 2);

        double secondaryAmount;
        if (filledAmount == priorityTrade.getAmount() || filledAmount + SATOSHI == priorityTrade.getAmount()) {
            // The right amount was filled.
            logAtLevel("Correct amount filled, placing amount on secondary: " + secondaryTrade.getAmount(), 2);
            request = new TradeRequest(secondaryTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.baseBalance += secondaryTrade.getAmount();
                secondary.quoteBalance -= secondaryTrade.getAmount() * secondaryTrade.getRate();
            } else {
                secondary.baseBalance -= secondaryTrade.getAmount();
                secondary.quoteBalance += secondaryTrade.getAmount() * secondaryTrade.getRate();
            }
            // TODO(stfinancial): Check that we filled the right amount?
            return true;
        }
        if (filledAmount == 0.0) {
            logAtLevel("No trades filled, so none required.", 2);
            return true;
        }
        if (filledAmount * (1 - priority.takerFee) / (1 - secondary.takerFee) < MIN_AMOUNT) {
            // TODO(stfinancial): See if it is closer to 0 or min amount after accounting for arb ratio. Place trade accordingly.
            // TODO(stfinancial): Other option is to hold amount over until next trade and just include it in that.
            logAtLevel("Priority filled amount " + filledAmount + " leads to fee adjusted secondary amount " + filledAmount * (1 - priority.takerFee) / (1 - secondary.takerFee) + " below minimum amount.", 1);
            // TODO(stfinancial): Ignore this for now.
            return true;
        } else if (filledAmount + SATOSHI < priorityTrade.getAmount()) {
            logAtLevel("Alternate amount was filled: " + filledAmount + "  Constructing new trade.", 2);
            secondaryAmount = Math.floor((filledAmount * (1 - priority.takerFee) / (1 - secondary.takerFee)) * HUNDRED_MILLION) / HUNDRED_MILLION;
            Trade revisedTrade = new Trade(secondaryAmount, secondaryTrade.getRate(), secondaryTrade.getPair(), secondaryTrade.getType());
            request = new TradeRequest(revisedTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            logAtLevel("Placing alternate amount: " + filledAmount, 2);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.baseBalance += revisedTrade.getAmount();
                secondary.quoteBalance -= revisedTrade.getAmount() * revisedTrade.getRate();
            } else {
                secondary.baseBalance -= revisedTrade.getAmount();
                secondary.quoteBalance += revisedTrade.getAmount() * revisedTrade.getRate();
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
            logAtLevel("Correct (rounded) amount filled, placing amount on secondary: " + secondaryTrade.getAmount(), 2);
            request = new TradeRequest(secondaryTrade, 5, 5);
            request.setIsMarket(false);
            request.setIsPostOnly(false);
            response = secondary.market.processMarketRequest(request);
            if (!response.isSuccess()) {
                logAtLevel("Failure placing secondary trade on " + secondary.market.getName() + ": " + response.getJsonResponse(), 1);
                return false;
            }
            if (secondaryTrade.getType() == TradeType.BUY) {
                secondary.baseBalance += secondaryTrade.getAmount();
                secondary.quoteBalance -= secondaryTrade.getAmount() * secondaryTrade.getRate();
            } else {
                secondary.baseBalance -= secondaryTrade.getAmount();
                secondary.quoteBalance += secondaryTrade.getAmount() * secondaryTrade.getRate();
            }
            // TODO(stfinancial): Check that we filled the right amount?
            return true;
        }
        logAtLevel("AmountFilled: " + filledAmount + " is greater than expected amount: " + priorityTrade.getAmount(), 1);
        return false;

    }

    private double getScaledAmount(MarketInfo bidSide, MarketInfo askSide, double arbitrageRatio) {
//        double amount = STANDARD_AMOUNT;
        double arbitrageMultiplier = Math.pow((arbitrageRatio - ((1 - bidSide.takerFee) * (1 - askSide.takerFee))) * 100, 2);
        logAtLevel("ArbitrageMultiplier: " + arbitrageMultiplier, 3);

        double bidBase = bidSide.baseBalance * bidSide.bids.get(0).getRate();
        double bidQuote = bidSide.quoteBalance;
        if (bidQuote == 0) {
            logAtLevel("Bid Side Quote Balance is 0. Setting max adjustment ratio.", 3);
            return MAX_ACCOUNT_ADJUSTMENT_RATIO;
        }
        double bidRatio = bidBase / bidQuote;

        double askBase = askSide.baseBalance * askSide.asks.get(0).getRate();
        double askQuote = askSide.quoteBalance;
        if (askBase == 0) {
            logAtLevel("Ask Side Base Balance is 0. Setting max adjustment ratio.", 3);
            return MAX_ACCOUNT_ADJUSTMENT_RATIO;
        }
        double askRatio = askQuote / askBase;
        // TODO(stfinancial): This should be weighted by total magnitude of funds.
        double rebalanceMultiplier = Math.min(MAX_ACCOUNT_ADJUSTMENT_RATIO, Math.sqrt(askRatio * bidRatio));
        logAtLevel("RebalanceMultiplier: " + rebalanceMultiplier, 3);
        return STANDARD_AMOUNT * arbitrageMultiplier * rebalanceMultiplier;
    }

    private double getArbitrageRatio(MarketInfo bidSide, MarketInfo askSide) {
        double buyingPrice = askSide.asks.get(0).getRate();
        double sellingPrice = bidSide.bids.get(0).getRate();
        double requiredSellingPrice = buyingPrice / ((1 - bidSide.takerFee) * (1 - askSide.takerFee));
        logAtLevel("Buy (Lowest Ask): " + buyingPrice + "\tSell (HighestBid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice + "\t\tRatio: " + sellingPrice / requiredSellingPrice , 1);
        return sellingPrice / requiredSellingPrice;
    }

    /** @return false on request or response failure */
    private boolean maybeUpdateFeesAndBalances(List<MarketInfo> markets) {
        if (feeAndBalanceCount < FEE_AND_BALANCE_INTERVAL) {
            logAtLevel("FeeAndBalanceCount: " + feeAndBalanceCount, 4);
            ++feeAndBalanceCount;
            return true;
            // TODO(stfinancial): Uncomment once we have naive balance approximations. Raise interval as well.
//        } else if (arbRatioMA500.getMovingAverage() > 0.9965) {
//            return true;
        }
        logAtLevel("Updating fees and balances.", 3);
        MarketResponse response;
        for (MarketInfo m : markets) {
            response = m.market.processMarketRequest(FEE_REQUEST);
            if (!response.isSuccess()) {
                logAtLevel(m.market.getName() + " fee request failed: " + response.getJsonResponse(), 1);
                return false;
            }
            m.takerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
            response = m.market.processMarketRequest(ACCOUNT_BALANCE_REQUEST);
            if (!response.isSuccess()) {
                logAtLevel(m.market.getName() + " account balance request failed: " + response.getJsonResponse(), 1);
                return false;
            }
            Map<Currency, Double> balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
//            System.out.println(response.getJsonResponse());
            m.quoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;
            m.baseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        }
        feeAndBalanceCount = 0;
        return true;
    }


    private void logAtLevel(String message, int debugLevel) {
        if (DEBUG >= debugLevel) {
            System.out.println(message);
        }
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
        double minAmount;
        List<Trade> asks;
        List<Trade> bids;
        int priority;
    }
}
