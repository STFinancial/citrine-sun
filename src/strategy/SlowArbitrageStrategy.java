package strategy;

import api.*;
import api.Currency;
import api.gdax.Gdax;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

import java.util.*;

/**
 * Created by Timothy on 4/23/17.
 */
public class SlowArbitrageStrategy extends Strategy {
    // TODO(stfinancial): Choice whether we increase base or quote holdings.

    // TODO(stfinancial): Multiple accounts to circumvent withdrawal limits.

    // TODO(stfinancial): Withdrawal support.
    // TODO(stfinancial): Market should return futures to be able to process in parallel. Can have multiple threads to simulate for now.

    private static final String POLONIEX_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String GDAX_KEYS = "/Users/Timothy/Documents/Keys/gdax_key.txt";

//    private static final String POLONIEX_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
//    private static final String GDAX_KEYS = "F:\\Users\\Zarathustra\\Documents\\gdax_key.txt";

    private static final double CURRENT_POLO_FEE = 0.0022;
    private static final double CURRENT_GDAX_FEE = 0.003;

    // TODO(stfinancial): Eventually we will scale with the size of the arbitrage
    private static final double MAX_AMOUNT = 3.5;
    private static final double MIN_AMOUNT = 0.01;
    private static final double STANDARD_AMOUNT = 0.23;
//    private static final double MIN_ADJUSTED_AMOUNT = 0.5;

    // TODO(stfinancial): We will expand to more pairs as we hook up the WAMP and socket endpoints.
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);

    private static final boolean DRY_RUN = false;
    private static final int REFRESH_INTERVAL = 400;
    // TODO(stfinancial): Think about if we actually want to be cancelling orders
    private static final int CANCEL_WAIT = 60;
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

    @Override
    public void run() {
        polo = new Poloniex(Credentials.fromFileString(POLONIEX_KEYS));
        gdax = new Gdax(Credentials.fromFileString(GDAX_KEYS));
        // TODO(stfinancial): Do this on a timer.
        while (!updateAccountInfo()) {
            System.out.println("Failure updating account info. Sleeping...");
            sleep(10000);
        }

        // TODO(stfinancial): GDAX does not support immediateorcancel, but for now we will use it on poloniex.

        OrderBookRequest orderBookRequest = new OrderBookRequest(PAIR, 20, 2, 1);
        OrderBookResponse orderBookResponse;
        List<Trade> poloBids;
        List<Trade> poloAsks;
        List<Trade> gdaxBids;
        List<Trade> gdaxAsks;
        HashSet<String> gdaxOrders = new HashSet<>();
        MarketResponse response;
        TradeResponse tradeResponse;
        int refreshIterations = 0;

        double arbitrageRatio;
        while (true) {
            sleep(500);
            // TODO(stfinancial): Check that the arbitrage ratio is not good at the moment (and we have money in our account), because we want to wait if that's the case.
            if (gdaxOrders.size() > 500) {
                System.out.println("Clearing orders.");
                Iterator<String> it = gdaxOrders.iterator();
                String id;
                while (it.hasNext()) {
                    id = it.next();
                    response = gdax.processMarketRequest(new CancelRequest(id, CancelRequest.CancelType.TRADE, 1, 1));
                    if (response.isSuccess()) {
                        it.remove();
                    }
                    sleep(300);
                }
            }
            if (++refreshIterations > REFRESH_INTERVAL) {
                refreshIterations = 0;
                while (!updateAccountInfo()) {
                    System.out.println("Failure updating account info. Sleeping...");
                    sleep(10000);
                }
            }
            response = polo.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
                continue;
            }
            orderBookResponse = (OrderBookResponse) response;
            poloBids = orderBookResponse.getBids().get(PAIR);
            poloAsks = orderBookResponse.getAsks().get(PAIR);
            response = gdax.processMarketRequest(orderBookRequest);
            if (!response.isSuccess()) {
                System.out.println("error: " + response.getJsonResponse());
                sleep(10000);
                continue;
            }
            orderBookResponse = (OrderBookResponse) response;
            gdaxBids = orderBookResponse.getBids().get(PAIR);
            gdaxAsks = orderBookResponse.getAsks().get(PAIR);

            TradeRequest poloTradeRequest;
            TradeRequest gdaxTradeRequest;
            // Test buy on poloniex and sell on GDAX
            if ((arbitrageRatio = getArbitrageRatio(gdaxBids.get(0), gdaxTakerFee, poloAsks.get(0), poloTakerFee)) > 1) {
                Trade lowestAsk = poloAsks.get(0);
                Trade highestBid = gdaxBids.get(0);
                System.out.println("Arbitrage found!!!");
                System.out.println("Polo (Buy): " + lowestAsk.getRate() + " - " + lowestAsk.getAmount());
                System.out.println("Gdax (Sell): " + highestBid.getRate() + " - " + highestBid.getAmount());

                double gdaxMinAmount = Math.min(getScaledAmount(arbitrageRatio), Math.min(highestBid.getAmount(), gdaxBaseBalance));
                double poloMinAmount = Math.min(getScaledAmount(arbitrageRatio), Math.min(lowestAsk.getAmount(), poloQuoteBalance / lowestAsk.getRate()));

                if (gdaxMinAmount < MIN_AMOUNT) {
                    System.out.println("Gdax minAmount below min amount: " + gdaxMinAmount);
                    // TODO(stfinancial): Check if other arbitrage exists (sell on polo, buy on gdax).
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
                // TODO(stfinancial): This is leaving 1 satoshi on the board and messing up my bot, potentially remove the -1 (or figure a way to make it so we never fail due to being 1 satoshi off funds)
                gdaxAmount = gdaxAmount == MIN_AMOUNT ? gdaxAmount : Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
                poloAmount = poloAmount == MIN_AMOUNT ? poloAmount : Math.round((poloAmount * 100000000.0) - 1) / 100000000.0;
                System.out.println("Polo Amount: " + poloAmount);
                System.out.println("Gdax Amount: " + gdaxAmount);

                if (!DRY_RUN) {
                    // TODO(stfinancial): Immediate or cancel, test what the resposne looks like.
                    poloTradeRequest = new TradeRequest(new Trade(poloAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), 5, 1);
                    poloTradeRequest.setIsPostOnly(false);
                    poloTradeRequest.setIsMarket(false);
                    response = polo.processMarketRequest(poloTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        return;
                    }
                    System.out.println("Amount filled on Polo: " + ((TradeResponse) response).getQuoteAmountFilled());
                    gdaxAmount = (((TradeResponse) response).getQuoteAmountFilled() * (1 - poloTakerFee)) / (1 - gdaxTakerFee);
                    if (gdaxAmount < 0.001) {
                        continue;
                    }
                    gdaxAmount = Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
                    System.out.println("Revised Gdax Amount: " + gdaxAmount);
                    String poloTradeId = ((TradeResponse) response).getOrderNumber();
                    // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
                    gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, highestBid.getRate(), PAIR, TradeType.SELL), 5, 1);
                    gdaxTradeRequest.setIsPostOnly(false);
                    gdaxTradeRequest.setIsMarket(false);
                    response = gdax.processMarketRequest(gdaxTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        return;
                    }
                    System.out.println(response.getJsonResponse());
                    String gdaxTradeId = ((TradeResponse) response).getOrderNumber();
                    // Poloniex has higher volume so we make the trade there first.
                    // Wait to see if they get filled if they weren't...
                    // TODO(stfinancial): Check if they actually weren't filled before sleeping.
                    // Test without canceling.
                    // TODO(stfinancial): Handle case where we accidentally try to fill our own unfilled order.
                    // TODO(stfinancial): Once portfolio rebalancing is in place, we shouldn't need to worry about unfilled orders.
//                    response = gdax.processMarketRequest(new OrderTradesRequest(gdaxTradeId, 1, 1));
                    gdaxOrders.add(gdaxTradeId);

                    // TODO(stfinancial): One solution is to hold all the orders in a list and cancel them all at once later.
//                    sleep(60000);
//                    gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                }
            }
            // Test sell on poloniex and buy on GDAX
            if ((arbitrageRatio = getArbitrageRatio(poloBids.get(0), poloTakerFee, gdaxAsks.get(0), gdaxTakerFee)) > 1) {
                Trade lowestAsk = gdaxAsks.get(0);
                Trade highestBid = poloBids.get(0);
                System.out.println("Arbitrage found!!!");
                System.out.println("Gdax (Buy): " + lowestAsk.getRate() + " - " + lowestAsk.getAmount());
                System.out.println("Polo (Sell): " + highestBid.getRate() + " - " + highestBid.getAmount());

//                double scaledAmount = getScaledAmount(arbitrageRatio, )

                double gdaxMinAmount = Math.min(getScaledAmount(arbitrageRatio), Math.min(lowestAsk.getAmount(), gdaxQuoteBalance / lowestAsk.getRate()));
                double poloMinAmount = Math.min(getScaledAmount(arbitrageRatio), Math.min(highestBid.getAmount(), poloBaseBalance));

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
                gdaxAmount = gdaxAmount == MIN_AMOUNT ? gdaxAmount : Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
                poloAmount = poloAmount == MIN_AMOUNT ? poloAmount : Math.round((poloAmount * 100000000.0) - 1) / 100000000.0;
                System.out.println("Polo Amount: " + poloAmount);
                System.out.println("Gdax Amount: " + gdaxAmount);
                if (!DRY_RUN) {
                    poloTradeRequest = new TradeRequest(new Trade(poloAmount, highestBid.getRate(), PAIR, TradeType.SELL), 5, 1);
                    poloTradeRequest.setIsPostOnly(false);
                    poloTradeRequest.setIsMarket(false);
                    poloTradeRequest.setIsImmediateOrCancel(true);
                    response = polo.processMarketRequest(poloTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        continue;
                    }
                    System.out.println("Amount filled on Polo: " + ((TradeResponse) response).getQuoteAmountFilled());
                    gdaxAmount = (((TradeResponse) response).getQuoteAmountFilled() * (1 - poloTakerFee)) / (1 - gdaxTakerFee);
                    if (gdaxAmount < 0.001) {
                        continue;
                    }
                    gdaxAmount = Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
                    System.out.println("Revised Gdax Amount: " + gdaxAmount);
                    String poloTradeId = ((TradeResponse) response).getOrderNumber();
                    // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
                    gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), 5, 1);
                    gdaxTradeRequest.setIsPostOnly(false);
                    gdaxTradeRequest.setIsMarket(false);
                    response = gdax.processMarketRequest(gdaxTradeRequest);
                    if (!response.isSuccess()) {
                        System.out.println("Failure: " + response.getJsonResponse());
                        continue;
                    }
                    System.out.println(response.getJsonResponse());
                    String gdaxTradeId = ((TradeResponse) response).getOrderNumber();
                    // Poloniex has higher volume so we make the trade there first.
                    // Wait to see if they get filled if they weren't...
                    // TODO(stfinancial): Check if they actually weren't filled before sleeping.
                    // Test without canceling.
                    // TODO(stfinancial): Handle case where we accidentally try to fill our own unfilled order.
                    // TODO(stfinancial): Once portfolio rebalancing is in place, we shouldn't need to worry about unfilled orders.
//                    response = gdax.processMarketRequest(new OrderTradesRequest(gdaxTradeId, 1, 1));
                    gdaxOrders.add(gdaxTradeId);

                    // TODO(stfinancial): One solution is to hold all the orders in a list and cancel them all at once later.
//                    sleep(60000);
//                    gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
                }

            }
        }
    }

    // TODO(stfinancial): Remove the priority flag once the application is multithreaded.
//    private boolean doArbitrage(MarketInfo sellSide, MarketInfo buySide, boolean buySidePriority) {
//        if (isArbitrage(gdaxBids.get(0), gdaxTakerFee, poloAsks.get(0), poloTakerFee)) {
//            Trade lowestAsk = poloAsks.get(0);
//            Trade highestBid = gdaxBids.get(0);
//            System.out.println("Arbitrage found!!!");
//            System.out.println("Polo (Buy): " + lowestAsk.getRate() + " - " + lowestAsk.getAmount());
//            System.out.println("Gdax (Sell): " + highestBid.getRate() + " - " + highestBid.getAmount());
//
//            double gdaxMinAmount = Math.min(MAX_AMOUNT, Math.min(highestBid.getAmount(), gdaxBaseBalance));
//            double poloMinAmount = Math.min(MAX_AMOUNT, Math.min(lowestAsk.getAmount(), poloQuoteBalance / lowestAsk.getRate()));
//
//            if (gdaxMinAmount < MIN_AMOUNT) {
//                System.out.println("Gdax minAmount below min amount: " + gdaxMinAmount);
//                // TODO(stfinancial): Check if other arbitrage exists (sell on polo, buy on gdax).
//                continue;
//            }
//            if (poloMinAmount < MIN_AMOUNT) {
//                System.out.println("Polo minAmount below min amount: " + poloMinAmount);
//                continue;
//            }
//
//            double gdaxPostFeeMin = gdaxMinAmount * (1 - gdaxTakerFee);
//            double poloPostFeeMin = poloMinAmount * (1 - poloTakerFee);
////                double minAmount = Math.min(gdaxMinAmount, poloMinAmount);
//
//            double gdaxAmount;
//            double poloAmount;
//            if (gdaxPostFeeMin > poloPostFeeMin) {
//                gdaxAmount = poloPostFeeMin / (1 - gdaxTakerFee);
//                poloAmount = poloMinAmount;
//            } else {
//                gdaxAmount = gdaxMinAmount;
//                poloAmount = gdaxPostFeeMin / (1 - poloTakerFee);
//            }
//            gdaxAmount = gdaxAmount == MIN_AMOUNT ? gdaxAmount : Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
//            poloAmount = poloAmount == MIN_AMOUNT ? poloAmount : Math.round((poloAmount * 100000000.0) - 1) / 100000000.0;
//            System.out.println("Polo Amount: " + poloAmount);
//            System.out.println("Gdax Amount: " + gdaxAmount);
//
//            if (!DRY_RUN) {
//                // TODO(stfinancial): Immediate or cancel, test what the resposne looks like.
//                poloTradeRequest = new TradeRequest(new Trade(poloAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), 5, 1);
//                poloTradeRequest.setIsPostOnly(false);
//                poloTradeRequest.setIsMarket(false);
//                response = polo.processMarketRequest(poloTradeRequest);
//                if (!response.isSuccess()) {
//                    System.out.println("Failure: " + response.getJsonResponse());
//                    return;
//                }
//                String poloTradeId = ((TradeResponse) response).getOrderNumber();
//                // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
//                gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, highestBid.getRate(), PAIR, TradeType.SELL), 5, 1);
//                gdaxTradeRequest.setIsPostOnly(false);
//                gdaxTradeRequest.setIsMarket(false);
//                response = gdax.processMarketRequest(gdaxTradeRequest);
//                if (!response.isSuccess()) {
//                    System.out.println("Failure: " + response.getJsonResponse());
//                    polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//                    return;
//                }
//                String gdaxTradeId = ((TradeResponse) response).getOrderNumber();
//                // Poloniex has higher volume so we make the trade there first.
//                // Wait to see if they get filled if they weren't...
//                // TODO(stfinancial): Check if they actually weren't filled before sleeping.
//                sleep(60000);
//                gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//                polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//            }
//        }
//        // Test sell on poloniex and buy on GDAX
//        if (isArbitrage(poloBids.get(0), poloTakerFee, gdaxAsks.get(0), gdaxTakerFee)) {
//            Trade lowestAsk = gdaxAsks.get(0);
//            Trade highestBid = poloBids.get(0);
//            System.out.println("Arbitrage found!!!");
//            System.out.println("Gdax (Buy): " + lowestAsk.getRate() + " - " + lowestAsk.getAmount());
//            System.out.println("Polo (Sell): " + highestBid.getRate() + " - " + highestBid.getAmount());
//
//            double gdaxMinAmount = Math.min(MAX_AMOUNT, Math.min(lowestAsk.getAmount(), gdaxQuoteBalance / lowestAsk.getRate()));
//            double poloMinAmount = Math.min(MAX_AMOUNT, Math.min(highestBid.getAmount(), poloBaseBalance));
//
//            if (gdaxMinAmount < MIN_AMOUNT) {
//                System.out.println("Gdax minAmount below min amount: " + gdaxMinAmount);
//                continue;
//            }
//            if (poloMinAmount < MIN_AMOUNT) {
//                System.out.println("Polo minAmount below min amount: " + poloMinAmount);
//                continue;
//            }
//            double gdaxPostFeeMin = gdaxMinAmount * (1 - gdaxTakerFee);
//            double poloPostFeeMin = poloMinAmount * (1 - poloTakerFee);
//
//            double gdaxAmount;
//            double poloAmount;
//            if (gdaxPostFeeMin > poloPostFeeMin) {
//                gdaxAmount = poloPostFeeMin / (1 - gdaxTakerFee);
//                poloAmount = poloMinAmount;
//            } else {
//                gdaxAmount = gdaxMinAmount;
//                poloAmount = gdaxPostFeeMin / (1 - poloTakerFee);
//            }
//            gdaxAmount = gdaxAmount == MIN_AMOUNT ? gdaxAmount : Math.round((gdaxAmount * 100000000.0) - 1) / 100000000.0;
//            poloAmount = poloAmount == MIN_AMOUNT ? poloAmount : Math.round((poloAmount * 100000000.0) - 1) / 100000000.0;
//            System.out.println("Polo Amount: " + poloAmount);
//            System.out.println("Gdax Amount: " + gdaxAmount);
//            if (!DRY_RUN) {
//                poloTradeRequest = new TradeRequest(new Trade(poloAmount, highestBid.getRate(), PAIR, TradeType.SELL), 5, 1);
//                poloTradeRequest.setIsPostOnly(false);
//                poloTradeRequest.setIsMarket(false);
//                response = polo.processMarketRequest(poloTradeRequest);
//                if (!response.isSuccess()) {
//                    System.out.println("Failure: " + response.getJsonResponse());
//                    continue;
//                }
//                String poloTradeId = ((TradeResponse) response).getOrderNumber();
//                // TODO(stfinancial): Once we use immediate or cancel, modify the amount of the second request accordingly.
//                gdaxTradeRequest = new TradeRequest(new Trade(gdaxAmount, lowestAsk.getRate(), PAIR, TradeType.BUY), 5, 1);
//                gdaxTradeRequest.setIsPostOnly(false);
//                gdaxTradeRequest.setIsMarket(false);
//                response = gdax.processMarketRequest(gdaxTradeRequest);
//                if (!response.isSuccess()) {
//                    System.out.println("Failure: " + response.getJsonResponse());
//                    polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//                    continue;
//                }
//                String gdaxTradeId = ((TradeResponse) response).getOrderNumber();
//                // Poloniex has higher volume so we make the trade there first.
//                // Wait to see if they get filled if they weren't...
//                // TODO(stfinancial): Check if they actually weren't filled before sleeping.
//                sleep(60000);
//                gdax.processMarketRequest(new CancelRequest(gdaxTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//                polo.processMarketRequest(new CancelRequest(poloTradeId, CancelRequest.CancelType.TRADE, 5, 5));
//            }
//
//        }
//    }
//

    // TODO(stfinancial): We can probably precompute the fee coefficient and stuff.
    private double getArbitrageRatio(Trade bid, double buyFee, Trade ask, double sellFee) {
        // Say both fees are 20%
        // Buy 1 BTC at 100 dollars. I get 1 * (1- 0.2) = 0.8 BTC
        // Sell 0.8 BTC at what price to get 100 dollars?
        // Say 200 bucks... I get 0.8 * 200 = 160 -> 160 * (1 - 0.2) = 128 bucks
        // The actual price would be (100 / (1 - 0.2)) / (1 - 0.2) = 156.25
        // Lets test
        // 0.8 * 156.25 * (1- 0.2) = 100 √
        // So that means give buy price B... arbitrage exists if sell price S > B / (1 - buyfee) / (1 - sellfee)
        double buyingPrice = ask.getRate();
        double sellingPrice = bid.getRate();
        // TODO(stfinancial): We can precompute this ratio.
        double requiredSellingPrice = buyingPrice / ((1 - buyFee) * (1 - sellFee));
        System.out.println("Buy (Lowest Ask): " + buyingPrice + "\tSell (Highest Bid): " + sellingPrice + "\tRequired Sell: " + requiredSellingPrice + "\t\tRatio: " + sellingPrice / requiredSellingPrice);
        return sellingPrice / requiredSellingPrice;
    }

//    // TODO(stfinancial): We can probably precompute the fee coefficient and stuff.
//    private boolean isArbitrage(Trade bid, double buyFee, Trade ask, double sellFee) {
//        // Say both fees are 20%
//        // Buy 1 BTC at 100 dollars. I get 1 * (1- 0.2) = 0.8 BTC
//        // Sell 0.8 BTC at what price to get 100 dollars?
//        // Say 200 bucks... I get 0.8 * 200 = 160 -> 160 * (1 - 0.2) = 128 bucks
//        // The actual price would be (100 / (1 - 0.2)) / (1 - 0.2) = 156.25
//        // Lets test
//        // 0.8 * 156.25 * (1- 0.2) = 100 √
//        // So that means give buy price B... arbitrage exists if sell price S > B / (1 - buyfee) / (1 - sellfee)
//
//        double buyingPrice = ask.getRate();
//        double requiredSellingPrice = buyingPrice / ((1 - buyFee) * (1 - sellFee));
//        System.out.println("Buy (Lowest Ask): " + buyingPrice + "\tSell (Highest Bid): " + bid.getRate() + "\tRequired Sell: " + requiredSellingPrice);
//        return requiredSellingPrice < bid.getRate();
//    }

    private boolean updateAccountInfo() {
        System.out.println("Refreshing Fees...");
        // WE can gracefully recover from failure here... we just be as conservative as needed.
        FeeRequest feeRequest = new FeeRequest(PAIR, 1, 1);
        MarketResponse response;
        response = polo.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println("Poloniex Fee Request unsuccessful: " + response.getJsonResponse());
            poloTakerFee = CURRENT_POLO_FEE;
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        poloTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();
        response = gdax.processMarketRequest(feeRequest);
        if (!response.isSuccess()) {
            System.out.println("Gdax Fee Request unsuccessful: " + response.getJsonResponse());
            gdaxTakerFee = CURRENT_GDAX_FEE;
            return false;
        }
        // TODO(stfinancial): Gotta handle GDAX's stupid way of handling fees.
        gdaxTakerFee = ((FeeResponse) response).getFeeInfo().getTakerFee();

        System.out.println("Refreshing Account Balances...");
        AccountBalanceRequest request = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
        Map<Currency, Double> balances;
        response = polo.processMarketRequest(request);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        poloBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        poloQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;

        response = gdax.processMarketRequest(request);
        if (!response.isSuccess()) {
            System.out.println(response.getJsonResponse());
            return false;
        }
        balances = ((AccountBalanceResponse) response).getBalances().get(AccountType.EXCHANGE);
        gdaxBaseBalance = balances.containsKey(PAIR.getBase()) ? balances.get(PAIR.getBase()) : 0;
        gdaxQuoteBalance = balances.containsKey(PAIR.getQuote()) ? balances.get(PAIR.getQuote()) : 0;
        return true;
    }

    // The better the arbitrage, the more we we want to transfer.
//    private double getScaledAmount(double arbitrageRatio, double rebalanceBonus) {
    private double getScaledAmount(double arbitrageRatio) {
        // TODO(stfinancial): Could also be a function of how imbalanced our portfolio is. If arbitrages are skewed, could be useful. I like this.
        return Math.max(MIN_AMOUNT, STANDARD_AMOUNT * Math.pow((arbitrageRatio - 0.995) * 100, 2));
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
