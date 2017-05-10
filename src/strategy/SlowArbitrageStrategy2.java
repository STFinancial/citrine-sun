package strategy;


import api.AccountType;
import api.Currency;
import api.CurrencyPair;
import api.Market;
import api.request.*;
import api.tmp_trade.Trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlowArbitrageStrategy2 extends Strategy {
    private static final double STANDARD_AMOUNT = 0.23;
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_ACCOUNT_ADJUSTMENT_RATIO = 100;
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);
    private static final int DEBUG = 3;

    @Override
    public void run() {

    }

    public void doArbitrage() {

    }

    private double getScaledAmount(MarketInfo bidSide, MarketInfo askSide, double arbitrageRatio) {
//        double amount = STANDARD_AMOUNT;
        double arbitrageMultiplier = Math.pow((arbitrageRatio - 0.995) * 100, 2);

        double bidBase = bidSide.baseBalance * bidSide.bids.get(0).getRate();
        double bidQuote = bidSide.quoteBalance;
        if (bidQuote == 0) { return MAX_ACCOUNT_ADJUSTMENT_RATIO; }
        double bidRatio = bidBase / bidQuote;

        double askBase = askSide.baseBalance * askSide.bids.get(0).getRate();
        double askQuote = askSide.quoteBalance;
        if (askBase == 0) { return MAX_ACCOUNT_ADJUSTMENT_RATIO; }
        double askRatio = askQuote / askBase;
        double rebalanceMultiplier = Math.max(MAX_ACCOUNT_ADJUSTMENT_RATIO, Math.sqrt(askRatio * bidRatio));
        return STANDARD_AMOUNT * arbitrageMultiplier * rebalanceMultiplier;
    }

    private double getArbitrageRatio(MarketInfo bidSide, MarketInfo askSide) {
        double buyingPrice = askSide.asks.get(0).getRate();
        double sellingPrice = bidSide.bids.get(0).getRate();
        double requiredSellingPrice = buyingPrice / ((1 - bidSide.takerFee) * (1 - askSide.takerFee));
        logAtLevel("Buy (Lowest Ask): " + buyingPrice + "\tSell (HighestBid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice + "\t\tRatio: " + sellingPrice / requiredSellingPrice , 1);
        return sellingPrice / requiredSellingPrice;
    }

    private boolean updateFeesAndBalances(List<MarketInfo> markets) {
        logAtLevel("Updating fees and balances.", 1);
        FeeRequest feeRequest = new FeeRequest(PAIR, 1, 1);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
        AccountBalanceResponse accountBalanceResponse;
        MarketResponse response;
        for (MarketInfo m : markets) {
            response = m.market.processMarketRequest(feeRequest);
            if (!response.isSuccess()) {
                logAtLevel(m.market.getName() + " fee request failed: " + response.getJsonResponse(), 0);
                return false;
            }
            m.takerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
            response = m.market.processMarketRequest(accountBalanceRequest);
            if (!response.isSuccess()) {
                logAtLevel(m.market.getName() + " account balance request failed: " + response.getJsonResponse(), 0);
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
    }
}
