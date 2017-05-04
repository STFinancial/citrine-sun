package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Timothy on 4/23/17.
 */
public class SlowArbitrageStrategy extends Strategy {
    // TODO(stfinancial): Withdrawal support.
    // TODO(stfinancial): Market should return futures to be able to process in parallel. Can have multiple threads to simulate for now.

    private static final String POLONIEX_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEYS = "/Users/Timothy/Documents/Keys/gdax_key.txt";

//    private static final String POLONIEX_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
//    private static final String GDAX_KEYS = "F:\\Users\\Zarathustra\\Documents\\gdax_key.txt";

    private static final double CURRENT_POLO_FEE = 0.0022;
    private static final double CURRENT_GDAX_FEE = 0.003;

    // TODO(stfinancial): Eventually we will scale with the size of the arbitrage
    private static final double MAX_AMOUNT = 1;
    private static final double MIN_AMOUNT = 0.01;

    // TODO(stfinancial): We will expand to more pairs as we hook up the WAMP and socket endpoints.
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);

    private static final boolean DRY_RUN = true;
    Poloniex polo;
    Gdax gdax;

    private double poloTakerFee;
    private double poloBaseBalance;
    private double poloQuoteBalance;
    private double gdaxTakerFee;
    private double gdaxBaseBalance;
    private double gdaxQuoteBalance;

    public static void main(String[] args) {
        SlowArbitrageStrategy strategy = new SlowArbitrageStrategy();
        strategy.run();
    }

//    public static void main(String[] args) {
//        SlowArbitrageStrategy strategy = new SlowArbitrageStrategy();
//        strategy.test();
//    }
//
//    private void test() {
//        printTradeAmounts()
//    }

    @Override
    public void run() {
        polo = new Poloniex(Credentials.fromFileString(POLONIEX_KEYS));
        gdax = new Gdax(Credentials.fromFileString(GDAX_KEYS));
        // TODO(stfinancial): Do this on a timer.
        refreshFees();
        if (!refreshBalances()) {
            sleep(10000);
        }

        // TODO(stfinancial): GDAX does not support immediateorcancel, but for now we will use it on poloniex.

        OrderBookRequest orderBookRequest = new OrderBookRequest(PAIR, 20, 2, 1);
        OrderBookResponse orderBookResponse;
        List<Trade> poloBids;
        List<Trade> poloAsks;
        List<Trade> gdaxBids;
        List<Trade> gdaxAsks;
        MarketResponse response;
        TradeResponse tradeResponse;
        while (true) {
            sleep(500);
            response = polo.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
            }
            orderBookResponse = (OrderBookResponse) response;
            poloBids = orderBookResponse.getBids().get(PAIR);
            poloAsks = orderBookResponse.getAsks().get(PAIR);
            response = gdax.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
            }
            orderBookResponse = (OrderBookResponse) response;
            gdaxBids = orderBookResponse.getBids().get(PAIR);
            gdaxAsks = orderBookResponse.getAsks().get(PAIR);

            TradeRequest poloTradeRequest;
            TradeRequest gdaxTradeRequest;
            // Test buy on poloniex and sell on GDAX
            if (isArbitrage(gdaxBids.get(0), gdaxTakerFee, poloAsks.get(0), poloTakerFee)) {
                System.out.println("Arbitrage found!!!");
                System.out.println("Polo (Buy): " + poloAsks.get(0).getRate() + " - " + poloAsks.get(0).getAmount());
                System.out.println("Gdax (Sell): " + gdaxBids.get(0).getRate() + " - " + gdaxBids.get(0).getAmount());

                double gdaxMinAmount = Math.min(MAX_AMOUNT, Math.min(gdaxBids.get(0).getAmount(), gdaxBaseBalance));
                double poloMinAmount = Math.min(MAX_AMOUNT, Math.min(poloAsks.get(0).getAmount(), poloQuoteBalance / poloAsks.get(0).getRate()));

                if (gdaxMinAmount < MIN_AMOUNT) {
                    System.out.println("Gdax minAmount below min amount: " + gdaxMinAmount);
                    continue;
                }
                if (poloMinAmount < MIN_AMOUNT) {
                    System.out.println("Polo minAmount below min amount: " + poloMinAmount);
                    continue;
                }

                double gdaxPostFeeMin = gdaxMinAmount * (1 - gdaxTakerFee);
                double poloPostFeeMin = poloMinAmount * (1 - poloTakerFee);
//                double minAmount = Math.min(gdaxMinAmount, poloMinAmount);

                double gdaxAmount;
                double poloAmount;
                if (gdaxPostFeeMin > poloPostFeeMin) {
                    gdaxAmount = poloPostFeeMin / (1 - gdaxTakerFee);
                    poloAmount = poloMinAmount;
                } else {
                    gdaxAmount = gdaxMinAmount;
                    poloAmount = gdaxPostFeeMin / (1 - poloTakerFee);
                }
                System.out.println("Polo Amount: " + poloAmount);
                System.out.println("Gdax Amount: " + gdaxAmount);

                if (!DRY_RUN) {
                    // TODO(stfinancial): Immediate or cancel, test what the resposne looks like.
                    poloTradeRequest = new TradeRequest(new Trade(poloAmount, poloAsks.get(0).getRate(), PAIR, TradeType.BUY), 5, 1);
                    poloTradeRequest.setIsPostOnly(false);
                    poloTradeRequest.setIsMarket(false);
                    response = polo.processMarketRequest(poloTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        continue;
                    }
                    String poloTradeId = ((TradeResponse) response).getOrderNumber();
                    // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
                    gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, gdaxBids.get(0).getRate(), PAIR, TradeType.SELL), 5, 1);
                    gdaxTradeRequest.setIsPostOnly(false);
                    gdaxTradeRequest.setIsMarket(false);
                    response = gdax.processMarketRequest(gdaxTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                        continue;
                    }
                    String gdaxTradeId = ((TradeResponse) response).getOrderNumber();


                    // Poloniex has higher volume so we make the trade there first.


                    // Wait to see if they get filled if they weren't...
                    // TODO(stfinancial): Check if they actually weren't filled before sleeping.
                    sleep(30000);
                    gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                    polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                }
            }
            // Test sell on poloniex and buy on GDAX
            if (isArbitrage(poloBids.get(0), poloTakerFee, gdaxAsks.get(0), gdaxTakerFee)) {
                System.out.println("Arbitrage found!!!");
                System.out.println("Gdax (Buy): " + gdaxAsks.get(0).getRate() + " - " + gdaxAsks.get(0).getAmount());
                System.out.println("Polo (Sell): " + poloBids.get(0).getRate() + " - " + poloBids.get(0).getAmount());

                double gdaxMinAmount = Math.min(MAX_AMOUNT, Math.min(gdaxAsks.get(0).getAmount(), gdaxQuoteBalance / gdaxAsks.get(0).getRate()));
                double poloMinAmount = Math.min(MAX_AMOUNT, Math.min(poloBids.get(0).getAmount(), poloBaseBalance));


                if (gdaxMinAmount < MIN_AMOUNT) {
                    System.out.println("Gdax minAmount below min amount: " + gdaxMinAmount);
                    continue;
                }
                if (poloMinAmount < MIN_AMOUNT) {
                    System.out.println("Polo minAmount below min amount: " + poloMinAmount);
                    continue;
                }

                double gdaxPostFeeMin = gdaxMinAmount * (1 - gdaxTakerFee);
                double poloPostFeeMin = poloMinAmount * (1 - poloTakerFee);


                double gdaxAmount;
                double poloAmount;
                if (gdaxPostFeeMin > poloPostFeeMin) {
                    gdaxAmount = poloPostFeeMin / (1 - gdaxTakerFee);
                    poloAmount = poloMinAmount;
                } else {
                    gdaxAmount = gdaxMinAmount;
                    poloAmount = gdaxPostFeeMin / (1 - poloTakerFee);
                }

                System.out.println("Polo Amount: " + poloAmount);
                System.out.println("Gdax Amount: " + gdaxAmount);


                if (!DRY_RUN) {
                    poloTradeRequest = new TradeRequest(new Trade(poloAmount, poloBids.get(0).getRate(), PAIR, TradeType.SELL), 5, 1);
                    poloTradeRequest.setIsPostOnly(false);
                    poloTradeRequest.setIsMarket(false);
                    response = polo.processMarketRequest(poloTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        continue;
                    }
                    String poloTradeId = ((TradeResponse) response).getOrderNumber();
                    // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
                    gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, gdaxAsks.get(0).getRate(), PAIR, TradeType.BUY), 5, 1);
                    gdaxTradeRequest.setIsPostOnly(false);
                    gdaxTradeRequest.setIsMarket(false);
                    response = gdax.processMarketRequest(gdaxTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                        continue;
                    }
                    String gdaxTradeId = ((TradeResponse) response).getOrderNumber();


                    // Poloniex has higher volume so we make the trade there first.

                    // Wait to see if they get filled if they weren't...
                    // TODO(stfinancial): Check if they actually weren't filled before sleeping.
                    sleep(30000);
                    gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                    polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                }

            }
        }
    }

    // TODO(stfinancial): We can probably precompute the fee coefficient and stuff.
    private boolean isArbitrage(Trade bid, double buyFee, Trade ask, double sellFee) {
        // Say both fees are 20%
        // Buy 1 BTC at 100 dollars. I get 1 * (1- 0.2) = 0.8 BTC
        // Sell 0.8 BTC at what price to get 100 dollars?
        // Say 200 bucks... I get 0.8 * 200 = 160 -> 160 * (1 - 0.2) = 128 bucks
        // The actual price would be (100 / (1 - 0.2)) / (1 - 0.2) = 156.25
        // Lets test
        // 0.8 * 156.25 * (1- 0.2) = 100 √
        // So that means give buy price B... arbitrage exists if sell price S > B / (1 - buyfee) / (1 - sellfee)

        double buyingPrice = ask.getRate();
        double requiredSellingPrice = buyingPrice / ((1 - buyFee) * (1 - sellFee));
        System.out.println("Buying Price (Lowest Ask): " + buyingPrice);
        System.out.println("Selling Price (Highest Bid): " + bid.getRate());
        System.out.println("Required Selling Price: " + requiredSellingPrice);
        return requiredSellingPrice < bid.getRate();
    }

    private boolean refreshBalances() {
        AccountBalanceRequest request = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
        MarketResponse response;
        Map<Currency, Double> balances;
        response = polo.processMarketRequest(request);
        // TODO(stfinancial): How do we handle failure here... for now we will return boolean...?
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        // TODO(stfinancial): What about the case where the account is totally empty? There still should be an empty map...
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        poloBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        poloQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;

        response = gdax.processMarketRequest(request);
        // TODO(stfinancial): How do we handle failure here... for now we will return boolean...?
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        // TODO(stfinancial): What about the case where the account is totally empty? There still should be an empty map...
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        gdaxBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        gdaxQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;
        return true;
    }

    private boolean refreshFees() {
        // WE can gracefully recover from failure here... we just be as conservative as needed.
        FeeRequest feeRequest = new FeeRequest(PAIR, 1, 1);
        MarketResponse response;
        response = polo.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            poloTakerFee = CURRENT_POLO_FEE;
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        poloTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
        response = gdax.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            poloTakerFee = CURRENT_POLO_FEE;
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        // TODO(stfinancial): Gotta handle GDAX's stupid way of handling fees.
        gdaxTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
        return true;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
