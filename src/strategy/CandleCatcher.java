package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.CompletedTrade;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.*;

import static api.Currency.*;

/**
 * Created by Timothy on 2/12/17.
 */
public class CandleCatcher extends Strategy {
    // Construct Market and obtain account balances.
    // Get Tickers for each currency pair
    // Place trades below according to what fraction we're using.
    // Sleep and move the trades as needed.
    // Log the orderid of the trades we've made.


    // Then place a new trade.
//                    tr = new TradeRequest(getTradeForFraction(highestBid, order.getValue(), quoteAmountPerFraction));
//                    do {
//                        r = p.processMarketRequest(tr);
//                        System.out.println(r.getJsonResponse());
//                        sleep(350);
//                    } while (!r.isSuccess());
//                    newOrders.put(((TradeResponse) r).getOrderNumber(), order.getValue());

    private static final AccountType ACCOUNT_TYPE = AccountType.EXCHANGE;
    private static final CurrencyPair PAIR = CurrencyPair.of(LTC, BTC);
//    private static final double AMOUNT_PER_FRACTION = 1; // Amount in Quote currency.
    private static final List<Double> FRACTIONS = Arrays.asList(new Double[]{ 0.01, 0.015, 0.02, 0.025, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1 });
    private static final double SATOSHI = 0.00000001;


    // TODO(stfinancial): Handle edge case where order fills immediately when we place it (this may be handled automatically).

    public static void main(String[] args) {
        CandleCatcher c = new CandleCatcher();
        c.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(KeyManager.getCredentialsForMarket("Poloniex", KeyManager.Machine.DESKTOP));
        Map<String, Double> orders;
        MarketResponse r;

        // Get account balances
        double balance = getQuoteBalance(p, PAIR);
        double quoteAmountPerFraction = Math.max(0.0, (balance / FRACTIONS.size()) - SATOSHI);

        // Get ticker and highest bid then place trades
        double highestBid = getHighestBid(p, PAIR);
        orders = placeTrades(p, highestBid, quoteAmountPerFraction);

        // Whether a placed order was filled.
        boolean fillFound = false;
        // Loop perpetually
        while (true) {
            if (fillFound) {
                fillFound = false;
                // Sleep until we've given a chance for the counter-orders to fill.
                System.out.println("Sleeping while waiting for counter-orders to fill.");
                sleep(60000);
                System.out.println("Canceling remaining buy orders.");
                // TODO(stfinancial): Handle the case where a buy order filled in the time that we were waiting.
                cancelAllOrders(p, orders);
                quoteAmountPerFraction = Math.max(0.0, (getQuoteBalance(p, PAIR) / FRACTIONS.size()) - SATOSHI);
                highestBid = getHighestBid(p, PAIR);
                System.out.println("Current highest bid at: " + highestBid + ". Placing new trades.");
                orders = placeTrades(p, highestBid, quoteAmountPerFraction);
                continue;
            }

            // TODO(stfinancial): Need to account for the balances that are in trades as well.
//            balance = getQuoteBalance(p, PAIR);
//            quoteAmountPerFraction = Math.max(0.0, (balance / FRACTIONS.size()) - SATOSHI);
            highestBid = getHighestBid(p, PAIR);
            System.out.println("New highest bid at: " + highestBid);

            // Check that none of our orders got filled.
            HashMap<String, Double> newOrders = new HashMap<>();
            List<String> ordersToRemove = new ArrayList<>();
            for (Map.Entry<String, Double> order : orders.entrySet()) {
                OrderTradesRequest t = new OrderTradesRequest(order.getKey());
                r = p.processMarketRequest(t);
                sleep(350);
                if (r.isSuccess() && ((OrderTradesResponse) r).getTrades().size() != 0) {
                    fillFound = true;
//                    System.out.println("Order with fraction " + order.getValue() + " was at least partially filled: " + r.getJsonResponse());
//                    // Cancel the remainder of the order... if it exists.
//                    OpenOrderRequest o = new OpenOrderRequest(PAIR);
//                    r = processRequest(o, p, 350, true);
//                    if (((OpenOrderResponse) r).getOpenOrdersById().containsKey(order.getKey())) {
//                        System.out.println("Canceling remainder of order.");
//                        // Cancel the remainder of the order.
//                        processRequest(new CancelRequest(order.getKey(), CancelRequest.CancelType.TRADE), p, 350, true);
//                    }
//                    // Now look at the trades executed by this order
//                    r = processRequest(t, p, 350, true);
//
//                    // Figure out how much of the order was filled and the amount of fees charged.
//                    double amountFilled = 0.0;
//                    double totalFees = 0.0; // fees assessed in base currency.
//                    for (CompletedTrade trade : ((OrderTradesResponse) r).getTrades()) {
//                        totalFees += trade.getFee();
//                        amountFilled += trade.getTrade().getAmount();
//                    }
//
//                    // Sell this amount at some specified level
//                    // TODO(stfinancial): Figure out how best to do this. Make fraction a constant.
//                    double price = highestBid * (1 - 0.002);
//                    double amt = (quoteAmountPerFraction / price) - amountFilled - totalFees - SATOSHI - SATOSHI;
//                    TradeRequest tr = new TradeRequest(new Trade(amt, highestBid * (1 - 0.002), PAIR, TradeType.SELL));
//                    System.out.println("Amount filled: " + amountFilled + "\tFees: " + totalFees + "\tNew Trade: " + tr.toString());
//                    processRequest(tr, p, 500, true);
//                    ordersToRemove.add(order.getKey());
                } else if (!fillFound) {
                    System.out.println("No fills found. Moving fraction: " + order.getValue());
                    // Move the trade
                    double movePrice = highestBid * (1 - order.getValue());
                    MoveOrderRequest m = new MoveOrderRequest(order.getKey(), movePrice);
                    m.setAmount(((quoteAmountPerFraction) / movePrice) - SATOSHI);
                    r = processRequest(m, p, 350, false);
                    newOrders.put(((MoveOrderResponse) r).getOrderNumber(), order.getValue());
                }
            }
            if (!fillFound) {
                orders.clear();
                orders.putAll(newOrders);
            } else {
                orders.clear();
//                cancelOrdersAndSellBase(p);
            }
        }
    }

    private Map<String, Double> placeTrades(Poloniex p, double highestBid, double quoteAmountPerFraction) {
        double price;
        double amount;
        MarketResponse r;
        Map<String, Double> orders = new HashMap<>();
        for (double fraction : FRACTIONS) {
            price = highestBid * (1 - fraction);
            amount = (quoteAmountPerFraction / price) - SATOSHI;
            TradeRequest tr = new TradeRequest(new Trade(amount, price, PAIR, TradeType.BUY));
            r = processRequest(tr, p, 350, false);
            orders.put(((TradeResponse) r).getOrderNumber(), fraction);
        }
        return orders;
    }

    private double getQuoteBalance(Poloniex p, CurrencyPair pair) {
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(ACCOUNT_TYPE);
        MarketResponse r = processRequest(accountBalanceRequest, p, 350, false);
        return ((AccountBalanceResponse) r).getBalances().getOrDefault(ACCOUNT_TYPE, Collections.emptyMap()).getOrDefault(pair.getQuote(), 0.0);
    }

    private double getHighestBid(Poloniex p, CurrencyPair pair) {
        TickerRequest tickerRequest = new TickerRequest(Arrays.asList(pair));
        MarketResponse r = processRequest(tickerRequest, p, 350, false);
        return ((TickerResponse) r).getTickers().get(pair).getHighestBid();
    }

    private void cancelAllOrders(Poloniex p, Map<String, Double> orders) {
        orders.forEach((id, f) -> {
            MarketResponse r = processRequest(new CancelRequest(id, CancelRequest.CancelType.TRADE), p , 200, true);
            System.out.println("Canceling order " + id + ": " + r.getJsonResponse());
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Interrupted in CandleCatcher...");
        }
    }

    private MarketResponse processRequest(MarketRequest request, Poloniex p, long delay, boolean printOutput) {
        MarketResponse r;
        do {
            r = p.processMarketRequest(request);
            if (printOutput) System.out.println(r.getJsonResponse());
            sleep(delay);
        } while (!r.isSuccess());
        return r;
    }
}
