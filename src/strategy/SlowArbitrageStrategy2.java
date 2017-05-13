package strategy;


import api.*;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import util.MovingAverage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlowArbitrageStrategy2 extends Strategy {
    // TODO(stfinancial): IOC both trades and have a "holdover" potentially...

    private static final String POLO_KEY = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEY = "/Users/Timothy/Documents/Keys/gdax_key.txt";

    private static final double STANDARD_AMOUNT = 0.23;
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_ACCOUNT_ADJUSTMENT_RATIO = 100;
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);
    private static final int DEBUG = 3;

    private static final OrderBookRequest ORDER_BOOK_REQUEST = new OrderBookRequest(PAIR, 20, 2, 1);
    private static final FeeRequest FEE_REQUEST = new FeeRequest(PAIR, 1, 1);
    private static final AccountBalanceRequest ACCOUNT_BALANCE_REQUEST = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);

    private static final int FEE_AND_BALANCE_INTERVAL = 500;
    private int feeAndBalanceCount = 0;
    private MovingAverage arbRatioMA500 = new MovingAverage(FEE_AND_BALANCE_INTERVAL);

    private static final double HUNDRED_MILLION = 100000000;

    @Override
    public void run() {
        MarketInfo polo = new MarketInfo();
        polo.market = new Poloniex(Credentials.fromFileString(POLO_KEY));
        MarketInfo gdax = new MarketInfo();
        gdax.market = new Gdax(Credentials.fromFileString(GDAX_KEY));
        doArbitrage(Arrays.asList(polo, gdax));
    }

    // TODO(stfinancial): Eventually remove the priority
    public void doArbitrage(List<MarketInfo> markets) {
        TradeRequest tradeRequest;
        MarketResponse response;
        OrderBookResponse orderBookResponse;
        TradeResponse tradeResponse;
        // TODO(stfinancial): Right now we will hardcode that we are using 2 exchanges.
        MarketInfo market1 = markets.get(0);
        MarketInfo market2 = markets.get(1);
        MarketInfo askSide;
        MarketInfo bidSide;
        while (true) {
            sleep(400);
            maybeUpdateFeesAndBalances(markets);

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

            /* There is an arbitrage */
            if (arbitrageRatio > 1) {
                Trade lowestAsk = askSide.asks.get(0);
                Trade highestBid = bidSide.bids.get(0);
                logAtLevel("Arbitrage Found with Ratio: " + arbitrageRatio, 2);
                logAtLevel("Buy (" + askSide.market.getName() + ") - " + lowestAsk.getAmount() + " - " + lowestAsk.getRate(), 2);
                logAtLevel("Sell (" + askSide.market.getName() + ") - " + highestBid.getAmount() + " - " + highestBid.getRate(), 2);

                double scaledAmount = getScaledAmount(bidSide, askSide, arbitrageRatio);

                double askSideTradeAmount = lowestAsk.getAmount();
                // TODO(stfinancial): Make sure this is updated when we search more than 1 order deep.
                double askSideBalanceAmount = Math.floor((askSide.quoteBalance / lowestAsk.getRate()) * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
                double askSideMinAmount = Math.min(askSideBalanceAmount, askSideTradeAmount);
                double askSidePostFeeMinAmount = askSideMinAmount * (1 - askSide.takerFee);


                double bidSideTradeAmount = highestBid.getAmount();
                double bidSideBalanceAmount = Math.floor(bidSide.baseBalance * HUNDRED_MILLION) / HUNDRED_MILLION; // Round the decimal down.
                double bidSideMinAmount = Math.min(bidSideTradeAmount, bidSideBalanceAmount);
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

                if (askAmount < MIN_AMOUNT) {
                    logAtLevel("Ask Side (" + askSide.market.getName() + ") amount lower than global min: " + askSideMinAmount, 2);
                    continue;
                }

                if (bidAmount < MIN_AMOUNT) {
                    logAtLevel("Bid Side (" + bidSide.market.getName() + ") amount lower than global min: " + bidSideMinAmount, 2);
                    continue;
                }

                logAtLevel(askSide.market.getName() + " Amount: " + askAmount, 2);
                logAtLevel(bidSide.market.getName() + " Amount: " + bidAmount, 2);

                /* Make the trades */
                tradeRequest = askSide.priority > bidSide.priority ? new TradeRequest(new Trade(askAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), 5, 5) : new TradeRequest(new Trade(bidAmount, highestBid.getRate(), PAIR, TradeType.SELL), 5, 5);

            }
        }
    }

    private void placeTrades(MarketInfo priority, Trade priorityTrade, MarketInfo secondary, Trade secondaryTrade) {
        MarketResponse response;
        TradeResponse tradeResponse;

        TradeRequest request = new TradeRequest(priorityTrade, 5, 5);
        request.setIsMarket(false);
        request.setIsPostOnly(false);
        request.setIsImmediateOrCancel(true);
        response = priority.market.processMarketRequest(request);
        if (!response.isSuccess()) {
            logAtLevel("Failure placing trade on " + priority.market.getName() + ": " + response.getJsonResponse(), 1);
            return;
        }
        tradeResponse = (TradeResponse) response;
        logAtLevel("Amount filled on " + priority.market.getName() + ": " + tradeResponse.getQuoteAmountFilled(), 2);
        double secondaryAmount = (tradeResponse.getQuoteAmountFilled() * priority.takerFee / secondary.takerFee);
        // A few cases here:
            // We fill an amount smaller than MIN_AMOUNT
                // One solution for this is check whether its closer to 0 or MIN_AMOUNT (slightly adjusted by arb ratio) and do whatever is closer.
            // We fill an amount smaller than the balance in secondaryTrade
                // Should use the balance we have left.
            // We fill exactly the right amount
                // Check that the secondary amount is the same as the amount available in the trade.
                // Round up or down by one satoshi if needed (checking it does not put us below min amount or above account balance).

//        priority.market.

    }

    private double getScaledAmount(MarketInfo bidSide, MarketInfo askSide, double arbitrageRatio) {
//        double amount = STANDARD_AMOUNT;
        double arbitrageMultiplier = Math.pow((arbitrageRatio - 0.995) * 100, 2);
        logAtLevel("ArbitrageMultiplier: " + arbitrageMultiplier, 3);

        double bidBase = bidSide.baseBalance * bidSide.bids.get(0).getRate();
        double bidQuote = bidSide.quoteBalance;
        if (bidQuote == 0) { return MAX_ACCOUNT_ADJUSTMENT_RATIO; }
        double bidRatio = bidBase / bidQuote;

        double askBase = askSide.baseBalance * askSide.bids.get(0).getRate();
        double askQuote = askSide.quoteBalance;
        if (askBase == 0) { return MAX_ACCOUNT_ADJUSTMENT_RATIO; }
        double askRatio = askQuote / askBase;
        // TODO(stfinancial): This should be weighted by total magnitude of funds.
        double rebalanceMultiplier = Math.max(MAX_ACCOUNT_ADJUSTMENT_RATIO, Math.sqrt(askRatio * bidRatio));
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
            ++feeAndBalanceCount;
            return true;
        } else if (arbRatioMA500.getMovingAverage() > 0.9965) {
            return true;
        }
        logAtLevel("Updating fees and balances.", 3);
        AccountBalanceResponse accountBalanceResponse;
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
            m.quoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;
            m.baseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        }
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
        List<Trade> asks;
        List<Trade> bids;
        int priority;
    }
}
