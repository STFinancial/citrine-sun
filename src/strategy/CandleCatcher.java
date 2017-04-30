package strategy;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static api.Currency.*;

/**
 * Created by Timothy on 2/12/17.
 */
public class CandleCatcher extends Strategy {
    // TODO(stfinancial): This code definitely needs to be cleaned up.

    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final double[] ABOVE_FRACS = { 0.05 };
    private static final double[] BELOW_FRACS = { 0.05 };
    private static final double BTC_AMOUNT = 1;

    private static final CurrencyPair PAIR = CurrencyPair.of(FCT, BTC);
    private static final boolean IS_MARGIN = true;

    private static final int WAIT_CYCLES = 10;

    public static void main(String[] args) {
        CandleCatcher c = new CandleCatcher();
        c.run();
    }

    @Override
    public void run() {
        Credentials credentials = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(credentials);

        // TODO(stfinancial): Subscribe to the pubsub at some point.
        MarketResponse resp = polo.processMarketRequest(new TickerRequest(1, 1));
        // Place trades at specified price fractions
        TickerResponse ticker;
        if (resp.isSuccess()) {
            ticker = (TickerResponse) resp;
        } else {
            return;
        }
        List<TradeRequest> above_trades = new LinkedList<>();
        double lowestAsk = ticker.getTickers().get(PAIR).getLowestAsk();
        TradeRequest r;
        Trade t;
        double price;
        for (double frac : ABOVE_FRACS) {
            price = lowestAsk * (1 + frac);
            t = new Trade(price, BTC_AMOUNT / price, PAIR, TradeType.SELL);
            r = new TradeRequest(t, 1, 1);
            r.setIsMargin(IS_MARGIN);
            r.setIsPostOnly(true);
            above_trades.add(r);
        }

        List<TradeRequest> below_trades = new LinkedList<>();
        double highestBid = ticker.getTickers().get(PAIR).getHighestBid();
        for (double frac : BELOW_FRACS) {
            price = highestBid * (1 - frac);
            t = new Trade(price, BTC_AMOUNT / price, PAIR, TradeType.BUY);
            r = new TradeRequest(t, 1, 1);
            r.setIsMargin(IS_MARGIN);
            r.setIsPostOnly(true);
            below_trades.add(r);
        }

        // Store ids of trades.
//        TradeResponse tradeResponse;
        Map<String, Trade> above_orders = new HashMap<>();
        for (TradeRequest aboveTrade : above_trades) {
            resp = polo.processMarketRequest(aboveTrade);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (resp.isSuccess()) {
                above_orders.put(((TradeResponse) resp).getOrderNumber(), aboveTrade.getTrade());
                // TODO(stfinancial): Handle case where there are resulting trades. (Very unlikely, but possible).
            }
        }

        Map<String, Trade> below_orders = new HashMap<>();
        for (TradeRequest belowTrade : below_trades) {
            resp = polo.processMarketRequest(belowTrade);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (resp.isSuccess()) {
                below_orders.put(((TradeResponse) resp).getOrderNumber(), belowTrade.getTrade());
                // TODO(stfinancial): Handle case where there are resulting trades. (Very unlikely, but possible).
            }
        }

        // TODO(stfinancial): See if we can cancel to minimize lending fees.

        // Continually get the ticker data every few seconds
        int counter = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                continue;
            }
            // Check that our previously placed orders are not missing
            MarketResponse mResp = polo.processMarketRequest(new OpenOrderRequest(1, 1));
            if (!mResp.isSuccess()) {
                System.out.println("Error: " + mResp.getJsonResponse());
                continue;
            }
            OpenOrderResponse oo = (OpenOrderResponse) mResp;
            Map<String, TradeOrder> orders = rekeyOrders(oo.getOpenOrders());
            // If they are missing, then we sold/bought something and we should unload ASAP
            for (Map.Entry<String, Trade> o : above_orders.entrySet()) {
                // TODO(stfinancial): Need to make sure that orderNumber doesn't change if some of the order got eaten.
                if (!orders.containsKey(o.getKey())) {
                    System.out.println("Candle caught");
                    // TODO(stfinancial): How to unload.
                    // The order got eaten totally.
                } else if (!orders.get(o.getKey()).getTrade().equals(o.getValue())) {
                    // TODO(stfinancial): How to unload.
                    // The order changed.
                }
            }
            for (Map.Entry<String, Trade> o : below_orders.entrySet()) {
                // TODO(stfinancial): Need to make sure that orderNumber doesn't change if some of the order got eaten.
                if (!orders.containsKey(o.getKey())) {
                    System.out.println("Candle caught");
                    // TODO(stfinancial): How to unload.
                    // The order got eaten totally.
                } else if (!orders.get(o.getKey()).getTrade().equals(o.getValue())) {
                    // TODO(stfinancial): How to unload.
                    // The order changed.
                }
            }
            // If they aren't missing, then cancel all of them
            ++counter;
            if (counter == WAIT_CYCLES) {
                counter = 0;
                // TODO(stfinancial): Cancel all of the orders and replace them.
                for (String s : above_orders.keySet()) {
                    polo.processMarketRequest(new CancelRequest(s, CancelRequest.CancelType.TRADE, 1, 1));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (String s : below_orders.keySet()) {
                    polo.processMarketRequest(new CancelRequest(s, CancelRequest.CancelType.TRADE, 1, 1));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Replace new trades at the price fractions above and below
            // Store the ids of these trades to check against them.
        }
    }

    private Map<String, TradeOrder> rekeyOrders(Map<CurrencyPair, List<TradeOrder>> orders) {
        Map<String, TradeOrder> rekeyedOrders = new HashMap<>();
        orders.values().forEach((orderList) -> {
            orderList.forEach((order) -> {
                rekeyedOrders.put(order.getOrderId(), order);
            });
        });
        return rekeyedOrders;
    }
}
